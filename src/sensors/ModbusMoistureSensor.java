package sensors;

import simulation.Soil;

import java.util.Random;

public class ModbusMoistureSensor {
    private boolean connected = false;
    private final int slaveAddress;
    private final Soil soil;
    //Simulate noise in sensors
    private final Random noiseGenerator = new Random();

    //Internal drift n calibration, the sensor always reads 12 less than the real value
    private final int calibrationOffSet = -12;

    public ModbusMoistureSensor(int slaveAddress, Soil soil) {
        this.slaveAddress = slaveAddress;
        this.soil = soil;
    }

    public void connect(){
        connected = true;
        System.out.println("Connected to ModbusMoistureSensor");
    }

    public int readHoldingRegister(int registerAddress){
        if (!connected){
            throw new IllegalStateException("Not connected to ModbusMoistureSensor");
        }
        if (registerAddress != 0) {
            throw new IllegalArgumentException("Unsupported register: " + registerAddress);
        }

        int noise = noiseGenerator.nextInt(20) - 10;
        int raw = (int) (soil.getMoisture() / 100 * 1023) + noise + calibrationOffSet;
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

    public int getCalibrationOffSet() {
        return calibrationOffSet;
    }

    public int clamp(int value, int min, int max){
        return Math.max(min, Math.min(max, value));
    }
}
