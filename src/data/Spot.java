package data;

import java.util.ArrayList;
import java.util.List;

public class Spot {
    private final Type type;
    private final List<Reservation> reservations = new ArrayList<>();

    public Spot(Type type) {
        this.type = type;
    }
}
