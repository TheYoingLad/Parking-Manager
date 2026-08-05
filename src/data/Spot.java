package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Spot(Type type, List<Reservation> reservations) {
    public Spot(Type type) {
        this(type, new ArrayList<>());
    }

    public Spot(Type type, List<Reservation> reservations) {
        this.type = type;
        this.reservations = new ArrayList<>(reservations);
    }

    /**
     * Returns an unmodifiable view of this spot's reservations (used for serialization).
     */
    @Override
    public List<Reservation> reservations() {
        return Collections.unmodifiableList(reservations);
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

    public void deleteReservation(Reservation reservation) {
        reservations.remove(reservation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parking Spot Type: ").append(type).append("\n").append("Reservations:");

        if (reservations.isEmpty()) sb.append(" None");
        reservations.forEach(res -> sb.append("\n\n").append(res.toString(false, true)));

        return sb.toString();
    }
}
