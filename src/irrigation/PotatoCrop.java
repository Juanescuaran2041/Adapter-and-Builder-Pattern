package irrigation;

public class PotatoCrop extends Crop {
    public PotatoCrop(IrrigationStrategy irrigationStrategy) {
        super(irrigationStrategy);
    }

    // POLYMORPHISM: overrides the abstract method with crop-specific behavior.
    @Override
    public IrrigationDecision evaluateIrrigation(double moisture, double temperature) {
        return irrigationStrategy.activate(stageAdjusted(moisture), temperature).withPrefix("[Potato]");
    }

    @Override
    public String name() {
        return "Potato";
    }
}
