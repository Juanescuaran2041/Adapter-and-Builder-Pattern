package irrigation;

//bridge pattern
public abstract class Crop {

    protected IrrigationStrategy irrigationStrategy;
    protected GrowthStage stage = GrowthStage.VEGETATIVE;

    protected Crop(IrrigationStrategy irrigationStrategy) {
        this.irrigationStrategy = irrigationStrategy;
    }

    public abstract IrrigationResult evaluateIrrigation(double moisture, double temperature);

    public abstract String getName();

    public void setIrrigationStrategy(IrrigationStrategy irrigationStrategy) {
        this.irrigationStrategy = irrigationStrategy;
    }

    public String getIrrigationName() {
        return irrigationStrategy.getName();
    }

    public double getIrrigationEfficiency() {
        return irrigationStrategy.getEfficiency();
    }

    public GrowthStage getStage() {
        return stage;
    }

    public void setStage(GrowthStage stage) {
        this.stage = stage;
    }
}
