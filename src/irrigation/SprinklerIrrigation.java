package irrigation;

public class SprinklerIrrigation implements IrrigationStrategy {

    private static final double FROST_THRESHOLD = 1.0;

    @Override
    public IrrigationDecision activate(double moisture, double temperature) {
        // Anti-frost technique: freezing water releases latent heat and protects the foliage
        if (temperature <= FROST_THRESHOLD) {
            return IrrigationDecision.frostProtection(
                    "Sprinkler ANTI-FROST mode (freezing water releases heat and protects the foliage)", 2);
        }
        return moisture < 35
                ? IrrigationDecision.active("Sprinkler irrigation ACTIVATED (wide coverage)", 5)
                : IrrigationDecision.standby("Sprinkler irrigation on standby, moisture sufficient");
    }

    @Override
    public String code() {
        return "SPRINKLER";
    }

    @Override
    public String name() {
        return "Sprinkler";
    }

    @Override
    public double efficiency() {
        return 0.75;
    }
}
