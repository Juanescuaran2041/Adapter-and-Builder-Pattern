import farm.FarmFactory;
import farm.IrrigationController;
import farm.Parcel;
import web.WebServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        IrrigationController controller = FarmFactory.createAndeanFarm();

        if (Arrays.asList(args).contains("--console")) {
            runConsoleDemo(controller);
            return;
        }

        Path webRoot = findWebFolder();
        if (webRoot == null) {
            System.err.println("Web folder not found. Run the program from the project root.");
            return;
        }

        new WebServer(controller, PORT, webRoot).start();
        System.out.println("SmartIrrigation dashboard running at http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop.");
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
            System.out.printf("%n--- Day %d, %02d:00 | reservoir %.0f L ---%n",
                    controller.getClock().day(), controller.getClock().hour(), controller.getReservoir().getLiters());
            for (Parcel parcel : controller.getParcels()) {
                System.out.printf("  %-12s %5.1f%% | %s%n", parcel.getName(),
                        parcel.getLastMoisture() == null ? Double.NaN : parcel.getLastMoisture(),
                        parcel.getLastDecision());
            }
        }
    }
}
