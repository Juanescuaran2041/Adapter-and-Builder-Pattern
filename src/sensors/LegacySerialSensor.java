package sensors;

import simulation.FarmClock;
import simulation.Soil;

import java.util.Locale;
import java.util.Random;

public class LegacySerialSensor {

    private boolean portOpen = false;
    private final String portName;
    private final Soil soil;
    private final FarmClock clock;
    private final Random noiseGenerator = new Random();

    public LegacySerialSensor(String portName, Soil soil, FarmClock clock) {
        this.portName = portName;
        this.soil = soil;
        this.clock = clock;
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
            throw new RuntimeException("Port not open");
        }

        double humidity = clamp(soil.getMoisture() + (noiseGenerator.nextDouble() * 3 - 1.5), 0, 100);
        double temperature = clock.getTemperature() + (noiseGenerator.nextDouble() - 0.5);

        //round to 1 decimal so the checksum is the same when the adapter reads it
        humidity = Math.round(humidity * 10) / 10.0;
        temperature = Math.round(temperature * 10) / 10.0;

        int checkSum = (int) Math.round(humidity + temperature);

        //sometimes the old cable has noise and the frame arrives wrong
        if (noiseGenerator.nextInt(100) < 4) {
            checkSum = checkSum + 3;
        }

        //Locale.US because with spanish locale it writes 12,5 instead of 12.5 and the parse fails
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
}
