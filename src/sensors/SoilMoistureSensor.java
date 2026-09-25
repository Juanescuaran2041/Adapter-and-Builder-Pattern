package sensors;

public interface SoilMoistureSensor {
    double readSoilPercentage();

    String getDeviceName();
}
