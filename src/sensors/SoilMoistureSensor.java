package sensors;

import java.util.OptionalDouble;

// Target interface of the adapter pattern
public interface SoilMoistureSensor {
    double readSoilPercentage();

    // Only some devices also measure soil temperature
    default OptionalDouble readTemperature() {
        return OptionalDouble.empty();
    }

    String deviceInfo();
}
