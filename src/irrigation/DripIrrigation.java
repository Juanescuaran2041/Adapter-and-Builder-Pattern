package irrigation;

public class DripIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationResult activate(double moisture, double temperature) {
        if (temperature <= 0) {
            return new IrrigationResult("FROST_HOLD", "Drip suspended, the hoses could freeze", 0);
        }
        if (moisture < 40) {
            return new IrrigationResult("ACTIVE", "Drip irrigation ACTIVATED (low flow, avoids wetting leaves)", 3);
        }
        return new IrrigationResult("STANDBY", "Drip irrigation on standby, moisture sufficient", 0);
    }

    @Override
    public String getName() {
        return "Drip";
    }

    @Override
    public double getEfficiency() {
        return 0.9;
    }
}
