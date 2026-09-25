package irrigation;

public class FurrowIrrigation implements IrrigationStrategy {
    @Override
    public IrrigationResult activate(double moisture, double temperature) {
        if (temperature <= 0) {
            return new IrrigationResult("FROST_HOLD", "Furrows closed, the water would freeze on the roots", 0);
        }
        if (moisture < 30) {
            return new IrrigationResult("ACTIVE", "Furrow irrigation OPEN (gravity, uses a lot of water)", 8);
        }
        return new IrrigationResult("STANDBY", "Furrows closed, moisture sufficient", 0);
    }

    @Override
    public String getName() {
        return "Furrow";
    }

    @Override
    public double getEfficiency() {
        return 0.55;
    }
}
