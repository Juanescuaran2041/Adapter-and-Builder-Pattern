package irrigation;

public class QuinoaCrop extends Crop {
    public QuinoaCrop(IrrigationStrategy irrigationStrategy) {
        super(irrigationStrategy);
    }

    @Override
    public IrrigationDecision evaluateIrrigation(double moisture, double temperature) {
        // quinoa is more sensitive to fungal growth, so it requires an extra margin
        return irrigationStrategy.activate(stageAdjusted(moisture) + 5, temperature)
                .withPrefix("[Quinoa, fungus-sensitive]");
    }

    @Override
    public String name() {
        return "Quinoa";
    }
}
