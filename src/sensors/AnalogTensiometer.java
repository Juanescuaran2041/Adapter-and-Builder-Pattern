package sensors;

import simulation.Soil;

import java.util.Random;

//it doesnt measure moisture, it measures the soil tension in centibars
//0 = soil full of water, 85 = very dry soil
public class AnalogTensiometer {

    private final String tag;
    private final Soil soil;
    private final Random noiseGenerator = new Random();

    public AnalogTensiometer(String tag, Soil soil) {
        this.tag = tag;
        this.soil = soil;
    }

    public int readCentibars() {
        int centibars = (int) ((100 - soil.getMoisture()) * 0.85);
        centibars = centibars + noiseGenerator.nextInt(5) - 2;
        if (centibars < 0) {
            centibars = 0;
        }
        if (centibars > 85) {
            centibars = 85;
        }
        return centibars;
    }

    public String getTag() {
        return tag;
    }
}
