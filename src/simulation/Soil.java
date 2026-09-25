package simulation;

public class Soil {

    private double moisture;

    public Soil(double moisture) {
        this.moisture = moisture;
    }

    public void dry(double temperature) {
        double loss = 0.3;
        if (temperature > 0) {
            loss = loss + temperature * 0.07;
        }
        moisture = moisture - loss;
        if (moisture < 0) {
            moisture = 0;
        }
    }

    public void addWater(double litersPerM2) {
        moisture = moisture + litersPerM2 * 2;
        if (moisture > 100) {
            moisture = 100;
        }
    }

    public double getMoisture() {
        return moisture;
    }
}
