package irrigation;

public class SprinklerIrrigation implements IrrigationStrategy {

    private static final double FROST_THRESHOLD = 1.0;

    @Override
    public IrrigationDecision activate(double moisture, double temperature) {
        // Anti-frost technique: freezing water releases latent heat and protects the foliage
        if (temperature <= FROST_THRESHOLD) {
            return IrrigationDecision.frostProtection(
                    "Aspersión ANTIHELADA activada (el agua al congelarse libera calor y protege el follaje)", 2);
        }
        return moisture < 35
                ? IrrigationDecision.active("Aspersión ACTIVADA (amplia cobertura)", 5)
                : IrrigationDecision.standby("Aspersión en espera, humedad suficiente");
    }

    @Override
    public String code() {
        return "SPRINKLER";
    }

    @Override
    public String name() {
        return "Aspersión";
    }

    @Override
    public double efficiency() {
        return 0.75;
    }
}
