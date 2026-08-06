package data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reservation")
class ReservationTest {

    private static final LocalDateTime FROM = LocalDateTime.of(2026, 8, 6, 10, 0);
    private static final LocalDateTime TO   = LocalDateTime.of(2026, 8, 6, 12, 0);

    private Reservation make() {
        return new Reservation(Type.STANDARD, new TimeInterval(FROM, TO), "XY-999");
    }

    @Test
    @DisplayName("getType returns the type passed to the constructor")
    void getType() {
        assertEquals(Type.STANDARD, make().getType());
    }

    @Test
    @DisplayName("getInterval returns the interval passed to the constructor")
    void getInterval() {
        Reservation r = make();
        assertEquals(FROM, r.getInterval().from());
        assertEquals(TO,   r.getInterval().to());
    }

    @Test
    @DisplayName("getLicencePlate returns the plate passed to the constructor")
    void getLicencePlate() {
        assertEquals("XY-999", make().getLicencePlate());
    }

    @Test
    @DisplayName("spotNumber defaults to 0 before being set")
    void defaultSpotNumber() {
        assertEquals(0, make().getSpotNumber());
    }

    @Test
    @DisplayName("setSpotNumber / getSpotNumber round-trip")
    void setSpotNumber() {
        Reservation r = make();
        r.setSpotNumber(5);
        assertEquals(5, r.getSpotNumber());
    }

    @Test
    @DisplayName("toString(true, true) contains spot, type, and plate")
    void toString_full() {
        Reservation r = make();
        r.setSpotNumber(3);
        String s = r.toString(true, true);
        assertTrue(s.contains("3"));
        assertTrue(s.contains("STANDARD"));
        assertTrue(s.contains("XY-999"));
    }

    @Test
    @DisplayName("toString(false, false) omits spot and plate")
    void toString_minimal() {
        Reservation r = make();
        r.setSpotNumber(3);
        String s = r.toString(false, false);
        assertFalse(s.contains("Spot"));
        assertFalse(s.contains("XY-999"));
    }
}
