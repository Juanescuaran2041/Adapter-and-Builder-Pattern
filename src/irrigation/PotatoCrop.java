package irrigation;

public class PotatoCrop extends Crop {
    public PotatoCrop(IrrigationStrategy irrigationStrategy) {
        super(irrigationStrategy);
    }

    // POLYMORPHISM: overrides the abstract method with crop-specific behavior.
    @Override
    public IrrigationResult evaluateIrrigation(double moisture, double temperature) {
        double adjusted = moisture + stage.getOffset();
        IrrigationResult result = irrigationStrategy.activate(adjusted, temperature);
        result.setMessage("[Potato] " + result.getMessage());
        return result;
    }

    @Override
    public String getName() {
        return "Potato";
    }
}
