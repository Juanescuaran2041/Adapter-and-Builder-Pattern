package sensors;

//adapter pattern
public class TensiometerAdapter implements SoilMoistureSensor {

    private final AnalogTensiometer tensiometer;

    public TensiometerAdapter(AnalogTensiometer tensiometer) {
        this.tensiometer = tensiometer;
    }

    @Override
    public double readSoilPercentage() {
        int centibars = tensiometer.readCentibars();
        //more centibars means drier soil, so we invert it
        return 100 - (centibars / 85.0) * 100;
    }

    @Override
    public String getDeviceName() {
        return "Tensiometer " + tensiometer.getTag() + " (centibars) - TensiometerAdapter";
    }
}
