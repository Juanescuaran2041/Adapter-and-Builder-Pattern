package farm;

import irrigation.Crop;
import irrigation.IrrigationDecision;
import sensors.SoilMoistureSensor;
import simulation.SoilEnvironment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Parcel {

    private static final int HISTORY_SIZE = 48;

    private final String id;
    private final String name;
    private final double areaM2;
    private final Crop crop;
    private final SoilMoistureSensor sensor;
    private final SoilEnvironment soil;

    private final Deque<Double> moistureHistory = new ArrayDeque<>();
    private Double lastMoisture;
    private Double lastTemperature;
    private IrrigationDecision lastDecision;
    private double waterUsedLiters;

    public Parcel(String id, String name, double areaM2, Crop crop, SoilMoistureSensor sensor, SoilEnvironment soil) {
        this.id = id;
        this.name = name;
        this.areaM2 = areaM2;
        this.crop = crop;
        this.sensor = sensor;
        this.soil = soil;
    }

    void recordReading(double moisture, double temperature) {
        lastMoisture = moisture;
        lastTemperature = temperature;
        moistureHistory.addLast(moisture);
        if (moistureHistory.size() > HISTORY_SIZE) {
            moistureHistory.removeFirst();
        }
    }

    void recordDecision(IrrigationDecision decision) {
        lastDecision = decision;
    }

    void addWaterUsed(double liters) {
        waterUsedLiters += liters;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAreaM2() {
        return areaM2;
    }

    public Crop getCrop() {
        return crop;
    }

    public SoilMoistureSensor getSensor() {
        return sensor;
    }

    SoilEnvironment getSoil() {
        return soil;
    }

    public Double getLastMoisture() {
        return lastMoisture;
    }

    public Double getLastTemperature() {
        return lastTemperature;
    }

    public IrrigationDecision getLastDecision() {
        return lastDecision;
    }

    public double getWaterUsedLiters() {
        return waterUsedLiters;
    }

    public List<Double> getMoistureHistory() {
        return new ArrayList<>(moistureHistory);
    }
}
