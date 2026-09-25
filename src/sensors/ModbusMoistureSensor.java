package sensors;

import simulation.SoilEnvironment;

import java.util.Random;

// Adaptee: industrial Modbus RTU probe, returns raw 10-bit counts (0..1023)
public class ModbusMoistureSensor {
    private boolean connected = false;
    private final int slaveAddress;
    private final SoilEnvironment soil;
    //Simulate noise in sensors
    private final Random noiseGenerator = new Random();

    //Factory calibration bias (datasheet): every raw reading is shifted by this amount
    private static final int CALIBRATION_OFFSET = -12;

    public ModbusMoistureSensor(int slaveAddress, SoilEnvironment soil) {
        this.slaveAddress = slaveAddress;
        this.soil = soil;
    }

    public void connect(){
        connected = true;
        System.out.println("Connected to ModbusMoistureSensor slave #" + slaveAddress);
    }

    public int readHoldingRegister(int registerAddress){
        if (!connected){
            throw new IllegalStateException("Not connected to ModbusMoistureSensor");
        }
        if (registerAddress != 0) {
            throw new IllegalArgumentException("Unsupported register: " + registerAddress);
        }

        int noise = noiseGenerator.nextInt(20) - 10;
        int raw = (int) Math.round(soil.moisture() / 100.0 * 1023) + noise + CALIBRATION_OFFSET;
        return clamp(raw, 0, 1023);
    }

    public void disconnect(){
        connected = false;
    }

    public boolean isConnected(){
        return connected;
    }

    public int getSlaveAddress() {
        return slaveAddress;
    }

    public int getCalibrationOffset() {
        return CALIBRATION_OFFSET;
    }

    public int clamp(int value, int min, int max){
        return Math.max(min, Math.min(max, value));
    }
}
