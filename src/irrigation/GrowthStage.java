package irrigation;

// Phenological stage of the crop. The offset shifts the perceived moisture:
// negative = the plant needs more water, positive = excess water is harmful.
public enum GrowthStage {
    SOWING("Sowing", -3),
    VEGETATIVE("Vegetative growth", 0),
    FLOWERING("Flowering", -5),
    MATURATION("Maturation", 10);

    private final String label;
    private final double moistureOffset;

    GrowthStage(String label, double moistureOffset) {
        this.label = label;
        this.moistureOffset = moistureOffset;
    }

    public String label() {
        return label;
    }

    public double moistureOffset() {
        return moistureOffset;
    }
}
