package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import farm.IrrigationController;
import farm.LogEntry;
import farm.Parcel;
import irrigation.GrowthStage;
import irrigation.IrrigationDecision;
import irrigation.IrrigationStrategies;
import irrigation.IrrigationStrategy;
import simulation.FarmClock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WebServer {

    private final IrrigationController controller;
    private final Path staticRoot;
    private final HttpServer server;

    public WebServer(IrrigationController controller, int port, Path staticRoot) throws IOException {
        this.controller = controller;
        this.staticRoot = staticRoot;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleStatic);
    }

    public void start() {
        server.start();
    }

    // ---------- API ----------

    private void handleApi(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            Map<String, String> query = parseQuery(exchange.getRequestURI());
            String[] parts = path.split("/");

            if (method.equals("GET") && path.equals("/api/state")) {
                sendJson(exchange, 200, stateJson());
            } else if (method.equals("POST") && path.equals("/api/tick")) {
                int hours = Integer.parseInt(query.getOrDefault("hours", "1"));
                controller.tick(Math.max(1, Math.min(hours, 168)));
                sendJson(exchange, 200, stateJson());
            } else if (method.equals("POST") && path.equals("/api/reservoir/turn")) {
                controller.emergencyTurn();
                sendJson(exchange, 200, stateJson());
            } else if (method.equals("POST") && parts.length == 5 && parts[2].equals("parcels")
                    && parts[4].equals("strategy")) {
                controller.changeStrategy(parts[3], required(query, "code"));
                sendJson(exchange, 200, stateJson());
            } else if (method.equals("POST") && parts.length == 5 && parts[2].equals("parcels")
                    && parts[4].equals("stage")) {
                controller.changeStage(parts[3], required(query, "code"));
                sendJson(exchange, 200, stateJson());
            } else {
                sendJson(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":" + Json.str(e.getMessage()) + "}");
        } catch (RuntimeException e) {
            sendJson(exchange, 500, "{\"error\":" + Json.str(e.toString()) + "}");
        }
    }

    private String stateJson() {
        synchronized (controller) {
            FarmClock clock = controller.getClock();
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"clock\":{")
                    .append("\"day\":").append(clock.day())
                    .append(",\"hour\":").append(clock.hour())
                    .append(",\"temperature\":").append(Json.num(clock.ambientTemperature()))
                    .append(",\"daytime\":").append(clock.isDaytime())
                    .append("},");
            sb.append("\"reservoir\":{")
                    .append("\"liters\":").append(Json.num(controller.getReservoir().getLiters()))
                    .append(",\"capacity\":").append(Json.num(controller.getReservoir().getCapacity()))
                    .append(",\"turnVolume\":").append(Json.num(controller.getReservoir().getTurnVolume()))
                    .append("},");
            sb.append("\"parcels\":[")
                    .append(controller.getParcels().stream().map(this::parcelJson).collect(Collectors.joining(",")))
                    .append("],");
            sb.append("\"strategies\":[")
                    .append(IrrigationStrategies.all().stream().map(this::strategyJson).collect(Collectors.joining(",")))
                    .append("],");
            sb.append("\"stages\":[");
            GrowthStage[] stages = GrowthStage.values();
            for (int i = 0; i < stages.length; i++) {
                if (i > 0) sb.append(',');
                sb.append("{\"code\":").append(Json.str(stages[i].name()))
                        .append(",\"label\":").append(Json.str(stages[i].label()))
                        .append(",\"offset\":").append(Json.num(stages[i].moistureOffset())).append('}');
            }
            sb.append("],");
            sb.append("\"log\":[")
                    .append(controller.getLog().stream().map(this::logJson).collect(Collectors.joining(",")))
                    .append("]}");
            return sb.toString();
        }
    }

    private String parcelJson(Parcel p) {
        IrrigationDecision d = p.getLastDecision();
        String history = p.getMoistureHistory().stream().map(Json::num).collect(Collectors.joining(","));
        return "{"
                + "\"id\":" + Json.str(p.getId())
                + ",\"name\":" + Json.str(p.getName())
                + ",\"area\":" + Json.num(p.getAreaM2())
                + ",\"crop\":" + Json.str(p.getCrop().name())
                + ",\"stage\":" + Json.str(p.getCrop().getGrowthStage().name())
                + ",\"strategy\":" + Json.str(p.getCrop().irrigationMethodCode())
                + ",\"device\":" + Json.str(p.getSensor().deviceInfo())
                + ",\"hasTemperatureProbe\":" + p.getSensor().readTemperature().isPresent()
                + ",\"moisture\":" + Json.num(p.getLastMoisture())
                + ",\"temperature\":" + Json.num(p.getLastTemperature())
                + ",\"waterUsed\":" + Json.num(p.getWaterUsedLiters())
                + ",\"status\":" + (d == null ? "null" : Json.str(d.status().name()))
                + ",\"message\":" + (d == null ? "null" : Json.str(d.message()))
                + ",\"history\":[" + history + "]"
                + "}";
    }

    private String strategyJson(IrrigationStrategy s) {
        return "{\"code\":" + Json.str(s.code())
                + ",\"name\":" + Json.str(s.name())
                + ",\"efficiency\":" + Json.num(s.efficiency()) + "}";
    }

    private String logJson(LogEntry e) {
        return "{\"day\":" + e.day()
                + ",\"hour\":" + e.hour()
                + ",\"parcel\":" + Json.str(e.parcel())
                + ",\"level\":" + Json.str(e.level())
                + ",\"message\":" + Json.str(e.message()) + "}";
    }

    // ---------- Static files ----------

    private void handleStatic(HttpExchange exchange) throws IOException {
        String requested = exchange.getRequestURI().getPath();
        if (requested.equals("/")) {
            requested = "/index.html";
        }
        Path file = staticRoot.resolve(requested.substring(1)).normalize();
        if (!file.startsWith(staticRoot) || !Files.isRegularFile(file)) {
            send(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        send(exchange, 200, contentType(file), Files.readAllBytes(file));
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    // ---------- Helpers ----------

    private static String required(Map<String, String> query, String key) {
        String value = query.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing parameter: " + key);
        }
        return value;
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String raw = uri.getRawQuery();
        if (raw == null) {
            return result;
        }
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        send(exchange, status, "application/json; charset=utf-8", body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        // allows opening index.html from IntelliJ's preview or the file system
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
