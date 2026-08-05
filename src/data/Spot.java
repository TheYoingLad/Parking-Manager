package data;

import java.util.ArrayList;
import java.util.List;

public class Spot {
    private final Type type;
    private final List<Reservation> reservations = new ArrayList<>();

    public Spot(Type type) {
        this.type = type;
    }

    public boolean reserve(Reservation incomingReservation){
        // check whether type matches if the spot is exclusive
        if (type.isExcusive && incomingReservation.type() != type) return false;

        // check availability
        if (reservations.stream().anyMatch(registeredReservation -> registeredReservation.interval().overlaps(incomingReservation.interval()))) return false;

        // make reservation
        reservations.add(incomingReservation);
        return true;
    }
}
