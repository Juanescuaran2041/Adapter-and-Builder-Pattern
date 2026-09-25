package farm;

public class LogEntry {

    private final String time;
    private final String parcel;
    private final String type;
    private final String message;

    public LogEntry(String time, String parcel, String type, String message) {
        this.time = time;
        this.parcel = parcel;
        this.type = type;
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public String getParcel() {
        return parcel;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
