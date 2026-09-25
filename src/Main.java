import farm.FarmFactory;
import farm.IrrigationController;
import farm.Parcel;
import ui.SwingDashboard;
import web.WebServer;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// Usage:
//   (no args)  -> web dashboard (http://localhost:8080) + Swing window, sharing the same farm
//   --web      -> only the web dashboard
//   --swing    -> only the Swing window
//   --console  -> 24-hour text simulation
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        List<String> options = Arrays.asList(args);
        IrrigationController controller = FarmFactory.createAndeanFarm();

        if (options.contains("--console")) {
            runConsoleDemo(controller);
            return;
        }

        boolean web = !options.contains("--swing");
        boolean swing = !options.contains("--web") && !GraphicsEnvironment.isHeadless();

        if (web) {
            startWebServer(controller);
        }
        if (swing) {
            SwingDashboard.open(controller);
        }
    }

    private static void startWebServer(IrrigationController controller) {
        Path webRoot = findWebFolder();
        if (webRoot == null) {
            System.err.println("Web folder not found; only the Swing window will be available.");
            return;
        }
        try {
            new WebServer(controller, PORT, webRoot).start();
            System.out.println("Web dashboard running at http://localhost:" + PORT);
        } catch (IOException e) {
            System.err.println("Could not start the web server on port " + PORT + ": " + e.getMessage());
        }
    }

    // Looks for the "web" folder in the working directory and its parents
    private static Path findWebFolder() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("web");
            if (Files.isRegularFile(candidate.resolve("index.html"))) {
                return candidate.normalize();
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static void runConsoleDemo(IrrigationController controller) {
        for (int i = 0; i < 24; i++) {
            controller.tick();
            System.out.printf(Locale.US, "%n--- Day %d, %02d:00 | reservoir %.0f L ---%n",
                    controller.getClock().day(), controller.getClock().hour(), controller.getReservoir().getLiters());
            for (Parcel parcel : controller.getParcels()) {
                System.out.printf(Locale.US, "  %-14s %5.1f%% | %s%n", parcel.getName(),
                        parcel.getLastMoisture() == null ? Double.NaN : parcel.getLastMoisture(),
                        parcel.getLastDecision());
            }
        }
    }
}
