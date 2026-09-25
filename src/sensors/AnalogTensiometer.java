package sensors;

import simulation.SoilEnvironment;

import java.util.Random;

// Adaptee: mechanical tensiometer with a dial. It does not measure moisture
// but soil water tension in centibars: 0 cb = saturated soil, ~85 cb = very dry.
public class AnalogTensiometer {

    public static final double MAX_CENTIBARS = 85.0;

    private final String tag;
    private final SoilEnvironment soil;
    private final Random noiseGenerator = new Random();

    public AnalogTensiometer(String tag, SoilEnvironment soil) {
        this.tag = tag;
        this.soil = soil;
    }

    public int readCentibars() {
        double tension = (100 - soil.moisture()) / 100.0 * MAX_CENTIBARS;
        int noise = noiseGenerator.nextInt(5) - 2;
        return (int) Math.max(0, Math.min(MAX_CENTIBARS, Math.round(tension) + noise));
    }

    public String getTag() {
        return tag;
    }
}
