package farm;

public record LogEntry(int day, int hour, String parcel, String level, String message) {
}
