package irrigation;

//negative offset = the plant needs more water, positive = less water
public enum GrowthStage {
    SOWING(-3),
    VEGETATIVE(0),
    FLOWERING(-5),
    MATURATION(10);

    private final double offset;

    GrowthStage(double offset) {
        this.offset = offset;
    }

    public double getOffset() {
        return offset;
    }
}
