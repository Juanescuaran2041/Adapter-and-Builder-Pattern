package farm;

import irrigation.GrowthStage;
import irrigation.IrrigationDecision;
import irrigation.IrrigationStrategies;
import simulation.FarmClock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

// Works only against the SoilMoistureSensor and Crop abstractions:
// it does not know which physical device or irrigation method is behind them.
public class IrrigationController {

    private static final int LOG_SIZE = 150;
    private static final int COMMUNAL_TURN_HOUR = 6;

    private final FarmClock clock;
    private final WaterReservoir reservoir;
    private final List<Parcel> parcels;
    private final Deque<LogEntry> log = new ArrayDeque<>();

    public IrrigationController(FarmClock clock, WaterReservoir reservoir, List<Parcel> parcels) {
        this.clock = clock;
        this.reservoir = reservoir;
        this.parcels = List.copyOf(parcels);
        parcels.forEach(this::evaluate);
    }

    public synchronized void tick() {
        clock.advance();
        if (clock.hour() == COMMUNAL_TURN_HOUR) {
            double received = reservoir.receiveCommunalTurn();
            log(null, "INFO", String.format(Locale.US, "Turno de agua comunal: ingresan %.0f L al reservorio", received));
        }
        for (Parcel parcel : parcels) {
            parcel.getSoil().evaporate();
            evaluate(parcel);
        }
    }

    public synchronized void tick(int hours) {
        for (int i = 0; i < hours; i++) {
            tick();
        }
    }

    private void evaluate(Parcel parcel) {
        double moisture;
        try {
            moisture = parcel.getSensor().readSoilPercentage();
        } catch (RuntimeException e) {
            IrrigationDecision error = IrrigationDecision.sensorError("Lectura descartada: " + e.getMessage());
            parcel.recordDecision(error);
            log(parcel, "ERROR", error.message());
            return;
        }
        // Parcels without a temperature probe use the farm weather station
        double temperature = parcel.getSensor().readTemperature().orElse(clock.ambientTemperature());
        parcel.recordReading(moisture, temperature);

        IrrigationDecision decision = parcel.getCrop().evaluateIrrigation(moisture, temperature);

        if (decision.usesWater()) {
            double liters = decision.litersPerM2() * parcel.getAreaM2();
            if (reservoir.withdraw(liters)) {
                double efficiency = parcel.getCrop().getIrrigationStrategy().efficiency();
                parcel.getSoil().irrigate(decision.litersPerM2() * efficiency);
                parcel.addWaterUsed(liters);
                log(parcel, decision.status() == IrrigationDecision.Status.FROST_PROTECTION ? "FROST" : "WATER",
                        String.format(Locale.US, "%s — %.0f L", decision.message(), liters));
            } else {
                decision = decision.denied(String.format(Locale.US,
                        "reservorio insuficiente (%.0f L requeridos), esperar turno comunal", liters));
                log(parcel, "WARN", decision.message());
            }
        } else if (decision.status() == IrrigationDecision.Status.FROST_HOLD) {
            log(parcel, "FROST", decision.message());
        }
        parcel.recordDecision(decision);
    }

    public synchronized void changeStrategy(String parcelId, String strategyCode) {
        Parcel parcel = findParcel(parcelId);
        parcel.getCrop().setIrrigationStrategy(IrrigationStrategies.byCode(strategyCode));
        log(parcel, "INFO", "Método de riego cambiado a " + parcel.getCrop().getIrrigationStrategy().name());
        evaluate(parcel);
    }

    public synchronized void changeStage(String parcelId, String stageCode) {
        Parcel parcel = findParcel(parcelId);
        parcel.getCrop().setGrowthStage(GrowthStage.valueOf(stageCode));
        log(parcel, "INFO", "Etapa fenológica: " + parcel.getCrop().getGrowthStage().label());
        evaluate(parcel);
    }

    public synchronized void emergencyTurn() {
        double received = reservoir.receiveCommunalTurn();
        log(null, "INFO", String.format(Locale.US, "Turno de agua extraordinario: ingresan %.0f L", received));
    }

    private Parcel findParcel(String parcelId) {
        return parcels.stream()
                .filter(p -> p.getId().equals(parcelId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown parcel: " + parcelId));
    }

    private void log(Parcel parcel, String level, String message) {
        log.addFirst(new LogEntry(clock.day(), clock.hour(), parcel == null ? "Finca" : parcel.getName(), level, message));
        if (log.size() > LOG_SIZE) {
            log.removeLast();
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
