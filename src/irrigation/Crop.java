package irrigation;

//bridge pattern
public abstract class Crop {

    protected IrrigationStrategy irrigationStrategy;
    protected GrowthStage growthStage = GrowthStage.VEGETATIVE;

    protected Crop(IrrigationStrategy irrigationStrategy) {
        this.irrigationStrategy = irrigationStrategy;
    }

    public abstract IrrigationDecision evaluateIrrigation(double moisture, double temperature);

    public abstract String name();

    // The implementor can be swapped at runtime without touching the crop hierarchy
    public void setIrrigationStrategy(IrrigationStrategy irrigationStrategy) {
        this.irrigationStrategy = irrigationStrategy;
    }

    // Clients only talk to the abstraction; it forwards to the implementor
    public double irrigationEfficiency() {
        return irrigationStrategy.efficiency();
    }

    public String irrigationMethodCode() {
        return irrigationStrategy.code();
    }

    public String irrigationMethodName() {
        return irrigationStrategy.name();
    }

    public GrowthStage getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(GrowthStage growthStage) {
        this.growthStage = growthStage;
    }

    protected double stageAdjusted(double moisture) {
        return moisture + growthStage.moistureOffset();
    }
}
