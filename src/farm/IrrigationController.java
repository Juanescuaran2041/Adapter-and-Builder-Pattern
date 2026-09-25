package farm;

import irrigation.DripIrrigation;
import irrigation.FurrowIrrigation;
import irrigation.GrowthStage;
import irrigation.IrrigationResult;
import irrigation.IrrigationStrategy;
import irrigation.SprinklerIrrigation;
import simulation.FarmClock;

import java.util.ArrayList;
import java.util.List;

public class IrrigationController {

    //liters that the community gives every day at 6am
    private static final double WATER_TURN = 18000;

    private final FarmClock clock;
    private final WaterReservoir reservoir;
    private final List<Parcel> parcels;
    private final List<LogEntry> log = new ArrayList<>();

    public IrrigationController(FarmClock clock, WaterReservoir reservoir, List<Parcel> parcels) {
        this.clock = clock;
        this.reservoir = reservoir;
        this.parcels = parcels;
        for (Parcel parcel : parcels) {
            evaluate(parcel);
        }
    }

    public synchronized void nextHour() {
        clock.nextHour();

        if (clock.getHour() == 6) {
            reservoir.addWater(WATER_TURN);
            addLog("Farm", "INFO", "Communal water turn, " + (int) WATER_TURN + " L added to the reservoir");
        }

        for (Parcel parcel : parcels) {
            parcel.getSoil().dry(clock.getTemperature());
            evaluate(parcel);
        }
    }

    public synchronized void advance(int hours) {
        for (int i = 0; i < hours; i++) {
            nextHour();
        }
    }

    private void evaluate(Parcel parcel) {
        double moisture;
        try {
            moisture = parcel.getSensor().readSoilPercentage();
        } catch (IllegalStateException e) {
            parcel.setResult(new IrrigationResult("ERROR", "Reading discarded: " + e.getMessage(), 0));
            addLog(parcel.getName(), "ERROR", "Reading discarded: " + e.getMessage());
            return;
        }
        parcel.setMoisture(moisture);

        IrrigationResult result = parcel.getCrop().evaluateIrrigation(moisture, clock.getTemperature());

        if (result.getLitersPerM2() > 0) {
            double liters = result.getLitersPerM2() * parcel.getArea();

            if (reservoir.useWater(liters)) {
                //not all the water reaches the roots, it depends on the irrigation method
                double realWater = result.getLitersPerM2() * parcel.getCrop().getIrrigationEfficiency();
                parcel.getSoil().addWater(realWater);
                parcel.setWaterUsed(parcel.getWaterUsed() + liters);

                String type = "WATER";
                if (result.getStatus().equals("ANTI_FROST")) {
                    type = "FROST";
                }
                addLog(parcel.getName(), type, result.getMessage() + " (" + (int) liters + " L)");
            } else {
                result.setStatus("DENIED");
                result.setMessage(result.getMessage() + " -> DENIED: not enough water in the reservoir");
                addLog(parcel.getName(), "WARN", result.getMessage());
            }
        } else if (result.getStatus().equals("FROST_HOLD")) {
            addLog(parcel.getName(), "FROST", result.getMessage());
        }

        parcel.setResult(result);
    }

    public synchronized void changeIrrigation(String parcelId, String method) {
        IrrigationStrategy strategy;
        if (method.equals("Drip")) {
            strategy = new DripIrrigation();
        } else if (method.equals("Sprinkler")) {
            strategy = new SprinklerIrrigation();
        } else if (method.equals("Furrow")) {
            strategy = new FurrowIrrigation();
        } else {
            throw new IllegalArgumentException("Unknown irrigation method: " + method);
        }

        Parcel parcel = findParcel(parcelId);
        parcel.getCrop().setIrrigationStrategy(strategy);
        addLog(parcel.getName(), "INFO", "Irrigation method changed to " + method);
        evaluate(parcel);
    }

    public synchronized void changeStage(String parcelId, String stage) {
        Parcel parcel = findParcel(parcelId);
        parcel.getCrop().setStage(GrowthStage.valueOf(stage));
        addLog(parcel.getName(), "INFO", "Growth stage changed to " + stage);
        evaluate(parcel);
    }

    public synchronized void extraWaterTurn() {
        reservoir.addWater(WATER_TURN);
        addLog("Farm", "INFO", "Extra water turn, " + (int) WATER_TURN + " L added to the reservoir");
    }

    private Parcel findParcel(String id) {
        for (Parcel parcel : parcels) {
            if (parcel.getId().equals(id)) {
                return parcel;
            }
        }
        throw new IllegalArgumentException("Parcel not found: " + id);
    }

    private void addLog(String parcel, String type, String message) {
        String time = "D" + clock.getDay() + " " + String.format("%02d", clock.getHour()) + ":00";
        log.add(0, new LogEntry(time, parcel, type, message));
        if (log.size() > 150) {
            log.remove(log.size() - 1);
        }
    }

    public FarmClock getClock() {
        return clock;
    }

    public WaterReservoir getReservoir() {
        return reservoir;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }

    public synchronized List<LogEntry> getLog() {
        return new ArrayList<>(log);
    }
}
