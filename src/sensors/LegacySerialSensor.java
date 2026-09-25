package sensors;

import simulation.SoilEnvironment;

import java.util.Locale;
import java.util.Random;

// Adaptee: old RS-232 probe that sends text frames "HUM:xx.x;TEMP:xx.x;CHK:nn"
public class LegacySerialSensor {

    // Old cables pick up electrical noise; some frames arrive corrupted
    private static final double CORRUPTION_PROBABILITY = 0.04;

    private boolean portOpen = false;
    private final String portName;
    private final SoilEnvironment soil;
    private final Random noiseGenerator = new Random();

    public LegacySerialSensor(String portName, SoilEnvironment soil) {
        this.portName = portName;
        this.soil = soil;
    }

    public void openPort(){
        portOpen = true;
        System.out.println("Opening port " + portName);
    }

    public void closePort(){
        portOpen = false;
    }

    public String readFrame(){
        if(!portOpen){
            throw new IllegalStateException("Port " + portName + " not open");
        }

        double humidity = round1(clamp(soil.moisture() + (noiseGenerator.nextDouble() * 3 - 1.5), 0, 100));
        double temperature = round1(clamp(soil.temperature() + (noiseGenerator.nextDouble() * 1 - 0.5), -15, 40));

        int checkSum = (int) Math.round(humidity + temperature);
        if (noiseGenerator.nextDouble() < CORRUPTION_PROBABILITY) {
            checkSum += 1 + noiseGenerator.nextInt(9);
        }

        // Locale.US: the frame protocol always uses '.' as decimal separator
        return String.format(Locale.US, "HUM:%.1f;TEMP:%.1f;CHK:%d", humidity, temperature, checkSum);
    }

    public boolean getPortOpen(){
        return portOpen;
    }

    public String getPortName() {
        return portName;
    }

    public double clamp(double value, double min, double max){
        return Math.max(min, Math.min(value, max));
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
