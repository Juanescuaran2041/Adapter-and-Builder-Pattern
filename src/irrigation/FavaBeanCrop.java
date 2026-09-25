package irrigation;

public class FavaBeanCrop extends Crop {
    public FavaBeanCrop(IrrigationStrategy irrigationStrategy) {
        super(irrigationStrategy);
    }

    @Override
    public IrrigationResult evaluateIrrigation(double moisture, double temperature) {
        double adjusted = moisture + stage.getOffset();
        //fava beans lose their flowers if they dont get water, so they need more in that stage
        if (stage == GrowthStage.FLOWERING) {
            adjusted = adjusted - 5;
        }
        IrrigationResult result = irrigationStrategy.activate(adjusted, temperature);
        result.setMessage("[Fava bean] " + result.getMessage());
        return result;
    }

    @Override
    public String getName() {
        return "Fava bean";
    }
}
