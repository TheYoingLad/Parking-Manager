package data;

public class Reservation {
    private final Type type;
    private final TimeInterval interval;
    private final String licencePlate;
    private int spotNumber = 0;

    public Reservation(Type type, TimeInterval interval, String licencePlate) {
        this.type = type;
        this.interval = interval;
        this.licencePlate = licencePlate;
    }

    public Type getType() {
        return type;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public int getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(int spotNumber) {
        this.spotNumber = spotNumber;
    }

    public String toString(boolean showSpot, boolean showPlate) {
        StringBuilder sb = new StringBuilder();

        if (showSpot) sb.append("Spot: #").append(spotNumber).append("\n");

        sb.append("Reservation Type: ").append(type).append("\n");
        sb.append("From: ").append(interval.from().toString().replace("T", " ")).append("\n");
        sb.append("To: ").append(interval.to().toString().replace("T", " ")).append("\n");

        if (showPlate) sb.append("Licence Plate Number: ").append(licencePlate);

        return sb.toString();
    }
}
