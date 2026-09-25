package sensors;

public class LegacySerialAdapter implements SoilMoistureSensor{

    private final LegacySerialSensor sensor;

    public LegacySerialAdapter(LegacySerialSensor sensor) {
        this.sensor = sensor;
        this.sensor.openPort();
    }

    @Override
    public double readSoilPercentage() {
        String frame = sensor.readFrame();
        String[] fields = frame.split(";");

        double humidity = Double.parseDouble(fields[0].split(":")[1]);
        double temperature = Double.parseDouble(fields[1].split(":")[1]);
        int checksum = Integer.parseInt(fields[2].split(":")[1]);

        int expectedChecksum = (int) Math.round(humidity + temperature);
        if (checksum != expectedChecksum) {
            throw new IllegalStateException("Corrupted serial frame: checksum mismatch.");
        }

        return humidity;
    }

    @Override
    public String getDeviceName() {
        return "RS-232 serial sensor (" + sensor.getPortName() + ") - LegacySerialAdapter";
    }
}
