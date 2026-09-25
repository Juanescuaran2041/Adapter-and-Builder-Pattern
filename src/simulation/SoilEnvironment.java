package simulation;

// Physical soil of one parcel. The simulated devices sample it,
// and irrigation modifies it, closing the control loop.
public class SoilEnvironment {

    private final FarmClock clock;
    private double moisture;

    public SoilEnvironment(FarmClock clock, double initialMoisture) {
        this.clock = clock;
        this.moisture = clamp(initialMoisture);
    }

    // Evapotranspiration per hour grows with temperature
    public void evaporate() {
        double temperature = clock.ambientTemperature();
        double loss = 0.3 + Math.max(0, temperature) * 0.07;
        moisture = clamp(moisture - loss);
    }

    public void irrigate(double effectiveLitersPerM2) {
        moisture = clamp(moisture + effectiveLitersPerM2 * 2.0);
    }

    public double moisture() {
        return moisture;
    }

    public double temperature() {
        return clock.ambientTemperature();
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }
}
