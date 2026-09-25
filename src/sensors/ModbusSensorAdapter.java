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
        // compensate the factory bias before converting counts to percentage
        int corrected = raw - moistureSensor.getCalibrationOffset();
        double percentage = (corrected / 1023.0) * 100;
        return Math.max(0, Math.min(100, percentage));
    }

    @Override
    public String deviceInfo() {
        return "Modbus RTU probe (slave #" + moistureSensor.getSlaveAddress() + ") via ModbusSensorAdapter";
    }
}
