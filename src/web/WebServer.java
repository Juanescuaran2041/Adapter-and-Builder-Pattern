package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import farm.IrrigationController;
import farm.LogEntry;
import farm.Parcel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public class WebServer {

    private final IrrigationController controller;
    private final File webFolder;

    public WebServer(IrrigationController controller, File webFolder) {
        this.controller = controller;
        this.webFolder = webFolder;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleFile);
        server.start();
    }

    private void handleApi(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getRawQuery();

        try {
            if (path.equals("/api/state")) {
                //only returns the state
            } else if (path.equals("/api/advance")) {
                int hours = Integer.parseInt(getParam(query, "hours"));
                controller.advance(hours);
            } else if (path.equals("/api/extra-turn")) {
                controller.extraWaterTurn();
            } else if (path.equals("/api/irrigation")) {
                controller.changeIrrigation(getParam(query, "parcel"), getParam(query, "method"));
            } else if (path.equals("/api/stage")) {
                controller.changeStage(getParam(query, "parcel"), getParam(query, "stage"));
            } else {
                send(exchange, 404, "{\"error\":\"not found\"}", "application/json");
                return;
            }
            send(exchange, 200, stateToJson(), "application/json");
        } catch (Exception e) {
            send(exchange, 400, "{\"error\":" + quote(e.getMessage()) + "}", "application/json");
        }
    }

    private String stateToJson() {
        synchronized (controller) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"day\":").append(controller.getClock().getDay()).append(",");
            json.append("\"hour\":").append(controller.getClock().getHour()).append(",");
            json.append("\"isDay\":").append(controller.getClock().isDay()).append(",");
            json.append("\"temperature\":").append(number(controller.getClock().getTemperature())).append(",");
            json.append("\"reservoir\":").append(number(controller.getReservoir().getLiters())).append(",");
            json.append("\"capacity\":").append(number(controller.getReservoir().getCapacity())).append(",");

            json.append("\"parcels\":[");
            List<Parcel> parcels = controller.getParcels();
            for (int i = 0; i < parcels.size(); i++) {
                Parcel p = parcels.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{");
                json.append("\"id\":").append(quote(p.getId())).append(",");
                json.append("\"name\":").append(quote(p.getName())).append(",");
                json.append("\"area\":").append(number(p.getArea())).append(",");
                json.append("\"crop\":").append(quote(p.getCrop().getName())).append(",");
                json.append("\"stage\":").append(quote(p.getCrop().getStage().name())).append(",");
                json.append("\"irrigation\":").append(quote(p.getCrop().getIrrigationName())).append(",");
                json.append("\"device\":").append(quote(p.getSensor().getDeviceName())).append(",");
                json.append("\"moisture\":").append(number(p.getMoisture())).append(",");
                json.append("\"waterUsed\":").append(number(p.getWaterUsed())).append(",");
                json.append("\"status\":").append(quote(p.getResult().getStatus())).append(",");
                json.append("\"message\":").append(quote(p.getResult().getMessage()));
                json.append("}");
            }
            json.append("],");

            json.append("\"log\":[");
            List<LogEntry> log = controller.getLog();
            for (int i = 0; i < log.size(); i++) {
                LogEntry entry = log.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{");
                json.append("\"time\":").append(quote(entry.getTime())).append(",");
                json.append("\"parcel\":").append(quote(entry.getParcel())).append(",");
                json.append("\"type\":").append(quote(entry.getType())).append(",");
                json.append("\"message\":").append(quote(entry.getMessage()));
                json.append("}");
            }
            json.append("]");

            json.append("}");
            return json.toString();
        }
    }

    private void handleFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File(webFolder, path);
        if (path.contains("..") || !file.isFile()) {
            send(exchange, 404, "Not found", "text/plain");
            return;
        }

        String type = "text/plain";
        if (path.endsWith(".html")) {
            type = "text/html";
        } else if (path.endsWith(".js")) {
            type = "application/javascript";
        } else if (path.endsWith(".css")) {
            type = "text/css";
        }

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        send(exchange, 200, content, type);
    }

    private void send(HttpExchange exchange, int code, String text, String type) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        //so the page also works if it is opened from intellij
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream out = exchange.getResponseBody();
        out.write(bytes);
        out.close();
    }

    private String getParam(String query, String name) {
        if (query == null) {
            throw new IllegalArgumentException("Missing parameter: " + name);
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2 && parts[0].equals(name)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        throw new IllegalArgumentException("Missing parameter: " + name);
    }

    private String quote(String text) {
        if (text == null) {
            return "null";
        }
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String number(double value) {
        //Locale.US so the decimals use . and the json is valid
        return String.format(Locale.US, "%.2f", value);
    }
}
