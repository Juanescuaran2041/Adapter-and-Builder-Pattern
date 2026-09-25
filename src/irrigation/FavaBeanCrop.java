package irrigation;

public class FavaBeanCrop extends Crop {
    public FavaBeanCrop(IrrigationStrategy irrigationStrategy) {
        super(irrigationStrategy);
    }

    @Override
    public IrrigationDecision evaluateIrrigation(double moisture, double temperature) {
        // fava beans drop their flowers under water stress, so flowering gets extra priority
        double adjusted = stageAdjusted(moisture);
        if (growthStage == GrowthStage.FLOWERING) {
            adjusted -= 5;
        }
        return irrigationStrategy.activate(adjusted, temperature).withPrefix("[Haba]");
    }

    @Override
    public String name() {
        return "Haba";
    }
}
