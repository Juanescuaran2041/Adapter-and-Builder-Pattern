package sensors;

import java.util.OptionalDouble;

//adapter pattern
public class LegacySerialAdapter implements SoilMoistureSensor{

    private final LegacySerialSensor sensor;
    private Double lastTemperature;

    public LegacySerialAdapter(LegacySerialSensor sensor) {
        this.sensor = sensor;
        this.sensor.openPort();
    }

    @Override
    public double readSoilPercentage() {
        String frame = sensor.readFrame();
        String[] fields = frame.split(";");
        if (fields.length != 3) {
            throw new IllegalStateException("Malformed serial frame: " + frame);
        }

        double humidity;
        double temperature;
        int checksum;
        try {
            humidity = Double.parseDouble(fields[0].split(":")[1]);
            temperature = Double.parseDouble(fields[1].split(":")[1]);
            checksum = Integer.parseInt(fields[2].split(":")[1]);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Malformed serial frame: " + frame, e);
        }

        int expectedChecksum = (int) Math.round(humidity + temperature);
        if (checksum != expectedChecksum) {
            throw new IllegalStateException("Corrupted serial frame (checksum mismatch): " + frame);
        }

        lastTemperature = temperature;
        return humidity;
    }

    // Temperature travels in the same frame, so the last valid one is reused
    @Override
    public OptionalDouble readTemperature() {
        return lastTemperature == null ? OptionalDouble.empty() : OptionalDouble.of(lastTemperature);
    }

    @Override
    public String deviceInfo() {
        return "Sonda serial RS-232 (" + sensor.getPortName() + ") vía LegacySerialAdapter";
    }
}
