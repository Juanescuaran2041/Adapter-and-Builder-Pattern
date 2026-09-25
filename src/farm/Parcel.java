package farm;

import irrigation.Crop;
import irrigation.IrrigationResult;
import sensors.SoilMoistureSensor;
import simulation.Soil;

public class Parcel {

    private final String id;
    private final String name;
    private final double area;
    private final Crop crop;
    private final SoilMoistureSensor sensor;
    private final Soil soil;

    private double moisture;
    private double waterUsed;
    private IrrigationResult result;

    public Parcel(String id, String name, double area, Crop crop, SoilMoistureSensor sensor, Soil soil) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.crop = crop;
        this.sensor = sensor;
        this.soil = soil;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getArea() {
        return area;
    }

    public Crop getCrop() {
        return crop;
    }

    public SoilMoistureSensor getSensor() {
        return sensor;
    }

    public Soil getSoil() {
        return soil;
    }

    public double getMoisture() {
        return moisture;
    }

    public void setMoisture(double moisture) {
        this.moisture = moisture;
    }

    public double getWaterUsed() {
        return waterUsed;
    }

    public void setWaterUsed(double waterUsed) {
        this.waterUsed = waterUsed;
    }

    public IrrigationResult getResult() {
        return result;
    }

    public void setResult(IrrigationResult result) {
        this.result = result;
    }
}
