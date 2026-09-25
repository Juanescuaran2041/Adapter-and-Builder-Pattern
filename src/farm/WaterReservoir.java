package farm;

// Community water tank ("qocha"). It is refilled only during the
// communal water turn, so every liter has to be rationed among parcels.
public class WaterReservoir {

    private final double capacity;
    private final double turnVolume;
    private double liters;

    public WaterReservoir(double capacity, double turnVolume, double initialLiters) {
        this.capacity = capacity;
        this.turnVolume = turnVolume;
        this.liters = Math.min(capacity, initialLiters);
    }

    public boolean withdraw(double amount) {
        if (amount > liters) {
            return false;
        }
        liters -= amount;
        return true;
    }

    // Returns the liters that actually entered the tank
    public double receiveCommunalTurn() {
        double before = liters;
        liters = Math.min(capacity, liters + turnVolume);
        return liters - before;
    }

    public double getLiters() {
        return liters;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getTurnVolume() {
        return turnVolume;
    }
}
