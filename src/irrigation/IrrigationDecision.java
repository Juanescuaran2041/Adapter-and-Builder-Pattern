package irrigation;

// Result of evaluating a parcel: what to do and how much water it needs
public record IrrigationDecision(Status status, String message, double litersPerM2) {

    public enum Status {
        ACTIVE,
        STANDBY,
        FROST_PROTECTION,
        FROST_HOLD,
        DENIED,
        SENSOR_ERROR
    }

    public static IrrigationDecision active(String message, double litersPerM2) {
        return new IrrigationDecision(Status.ACTIVE, message, litersPerM2);
    }

    public static IrrigationDecision standby(String message) {
        return new IrrigationDecision(Status.STANDBY, message, 0);
    }

    public static IrrigationDecision frostProtection(String message, double litersPerM2) {
        return new IrrigationDecision(Status.FROST_PROTECTION, message, litersPerM2);
    }

    public static IrrigationDecision frostHold(String message) {
        return new IrrigationDecision(Status.FROST_HOLD, message, 0);
    }

    public static IrrigationDecision sensorError(String message) {
        return new IrrigationDecision(Status.SENSOR_ERROR, message, 0);
    }

    public IrrigationDecision withPrefix(String prefix) {
        return new IrrigationDecision(status, prefix + " " + message, litersPerM2);
    }

    public IrrigationDecision denied(String reason) {
        return new IrrigationDecision(Status.DENIED, message + " → DENEGADO: " + reason, 0);
    }

    public boolean usesWater() {
        return litersPerM2 > 0;
    }

    @Override
    public String toString() {
        return message;
    }
}
