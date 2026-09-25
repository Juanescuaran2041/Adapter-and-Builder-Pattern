package irrigation;

public interface IrrigationStrategy {
    IrrigationResult activate(double moisture, double temperature);

    String getName();

    double getEfficiency();
}
