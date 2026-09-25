package irrigation;

public class IrrigationResult {

    //ACTIVE, STANDBY, ANTI_FROST, FROST_HOLD, DENIED or ERROR
    private String status;
    private String message;
    private double litersPerM2;

    public IrrigationResult(String status, String message, double litersPerM2) {
        this.status = status;
        this.message = message;
        this.litersPerM2 = litersPerM2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getLitersPerM2() {
        return litersPerM2;
    }

    @Override
    public String toString() {
        return message;
    }
}
