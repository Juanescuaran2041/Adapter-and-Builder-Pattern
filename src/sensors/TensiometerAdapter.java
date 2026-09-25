package sensors;

//adapter pattern
public class TensiometerAdapter implements SoilMoistureSensor {

    private final AnalogTensiometer tensiometer;

    public TensiometerAdapter(AnalogTensiometer tensiometer) {
        this.tensiometer = tensiometer;
    }

    // Higher tension means drier soil, so the scale is inverted
    @Override
    public double readSoilPercentage() {
        int centibars = tensiometer.readCentibars();
        return 100 - (centibars / AnalogTensiometer.MAX_CENTIBARS) * 100;
    }

    @Override
    public String deviceInfo() {
        return "Analog tensiometer " + tensiometer.getTag() + " (centibars) via TensiometerAdapter";
    }
}
