package irrigation;

public class QuinoaCrop extends Crop {
    public QuinoaCrop(IrrigationStrategy irrigationStrategy) {
        super(irrigationStrategy);
    }

    @Override
    public IrrigationResult evaluateIrrigation(double moisture, double temperature) {
        // quinoa is more sensitive to fungal growth, so it requires an extra margin
        double adjusted = moisture + stage.getOffset() + 5;
        IrrigationResult result = irrigationStrategy.activate(adjusted, temperature);
        result.setMessage("[Quinoa, fungus-sensitive] " + result.getMessage());
        return result;
    }

    @Override
    public String getName() {
        return "Quinoa";
    }
}
