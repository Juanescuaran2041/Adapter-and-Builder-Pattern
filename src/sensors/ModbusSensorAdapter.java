package sensors;

//adapter pattern
public class ModbusSensorAdapter implements SoilMoistureSensor {

    private final ModbusMoistureSensor moistureSensor;

    public ModbusSensorAdapter(ModbusMoistureSensor moistureSensor) {
        this.moistureSensor = moistureSensor;
        this.moistureSensor.connect();
    }

    @Override
    public double readSoilPercentage() {
        int raw = moistureSensor.readHoldingRegister(0);
        raw = raw - moistureSensor.getCalibrationOffSet();
        double percentage = (raw / 1023.0) * 100;
        if (percentage > 100) {
            percentage = 100;
        }
        if (percentage < 0) {
            percentage = 0;
        }
        return percentage;
    }

    @Override
    public String getDeviceName() {
        return "Modbus sensor (slave " + moistureSensor.getSlaveAddress() + ") - ModbusSensorAdapter";
    }
}
