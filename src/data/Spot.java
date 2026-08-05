package data;

import java.util.ArrayList;
import java.util.List;

public class Spot {
    private final Type type;
    private final List<Reservation> reservations = new ArrayList<>();

    public Spot(Type type) {
        this.type = type;
    }

    public boolean makeReservation(Reservation incomingReservation) {
        // check whether type matches if the spot is exclusive
        if (type.isExcusive && incomingReservation.getType() != type) return false;

        // check availability
        if (reservations.stream().anyMatch(registeredReservation -> registeredReservation.getInterval().overlaps(incomingReservation.getInterval())))
            return false;

        // make reservation
        reservations.add(incomingReservation);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parking Spot Type: ").append(type).append("\n").append("Reservations: ");

        if (reservations.isEmpty()) sb.append("None");
        reservations.forEach(res -> sb.append("\n").append(res.toString(false, true)));

        return sb.toString();
    }
}
