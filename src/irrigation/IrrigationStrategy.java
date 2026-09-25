package irrigation;

// Implementor side of the bridge
public interface IrrigationStrategy {
    IrrigationDecision activate(double moisture, double temperature);

    String code();

    String name();

    // Fraction of the applied water that actually reaches the roots
    double efficiency();
}
