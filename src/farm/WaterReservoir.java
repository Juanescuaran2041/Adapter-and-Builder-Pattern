package farm;

public class WaterReservoir {

    private final double capacity;
    private double liters;

    public WaterReservoir(double capacity, double liters) {
        this.capacity = capacity;
        this.liters = liters;
    }

    public boolean useWater(double amount) {
        if (amount > liters) {
            return false;
        }
        liters = liters - amount;
        return true;
    }

    public void addWater(double amount) {
        liters = liters + amount;
        if (liters > capacity) {
            liters = capacity;
        }
    }

    public double getLiters() {
        return liters;
    }

    public double getCapacity() {
        return capacity;
    }
}
