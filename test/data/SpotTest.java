package data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Spot")
class SpotTest {

    private static final LocalDateTime T10 = LocalDateTime.of(2026, 8, 6, 10, 0);
    private static final LocalDateTime T12 = LocalDateTime.of(2026, 8, 6, 12, 0);
    private static final LocalDateTime T14 = LocalDateTime.of(2026, 8, 6, 14, 0);
    private static final LocalDateTime T16 = LocalDateTime.of(2026, 8, 6, 16, 0);

    private Reservation res(Type type, LocalDateTime from, LocalDateTime to) {
        return new Reservation(type, new TimeInterval(from, to), "AB-123");
    }

    // ------------------------------------------------------------------
    // makeReservation()
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("makeReservation — STANDARD spot (non-exclusive)")
    class StandardSpot {

        private Spot spot;

        @BeforeEach
        void setUp() { spot = new Spot(Type.STANDARD); }

        @Test
        @DisplayName("accepts a STANDARD reservation on an empty spot")
        void accepts_standard() {
            assertTrue(spot.makeReservation(res(Type.STANDARD, T10, T12)));
        }

        @Test
        @DisplayName("accepts a DISABLED reservation (non-exclusive spot)")
        void accepts_disabled_type_on_standard_spot() {
            assertTrue(spot.makeReservation(res(Type.DISABLED, T10, T12)));
        }

        @Test
        @DisplayName("accepts a FAMILY reservation (non-exclusive spot)")
        void accepts_family_type_on_standard_spot() {
            assertTrue(spot.makeReservation(res(Type.FAMILY, T10, T12)));
        }

        @Test
        @DisplayName("accepts a non-overlapping second reservation")
        void accepts_non_overlapping() {
            spot.makeReservation(res(Type.STANDARD, T10, T12));
            assertTrue(spot.makeReservation(res(Type.STANDARD, T14, T16)));
        }

        @Test
        @DisplayName("rejects an overlapping reservation")
        void rejects_overlapping() {
            spot.makeReservation(res(Type.STANDARD, T10, T14));
            assertFalse(spot.makeReservation(res(Type.STANDARD, T12, T16)));
        }

        @Test
        @DisplayName("rejects a fully contained reservation")
        void rejects_contained() {
            spot.makeReservation(res(Type.STANDARD, T10, T16));
            assertFalse(spot.makeReservation(res(Type.STANDARD, T12, T14)));
        }

        @Test
        @DisplayName("reservation is stored after successful make")
        void stored_after_make() {
            Reservation r = res(Type.STANDARD, T10, T12);
            spot.makeReservation(r);
            assertTrue(spot.reservations().contains(r));
        }

        @Test
        @DisplayName("failed reservation is not stored")
        void not_stored_after_fail() {
            Reservation first  = res(Type.STANDARD, T10, T14);
            Reservation second = res(Type.STANDARD, T12, T16);
            spot.makeReservation(first);
            spot.makeReservation(second);
            assertFalse(spot.reservations().contains(second));
        }
    }

    @Nested
    @DisplayName("makeReservation — DISABLED spot (exclusive)")
    class DisabledSpot {

        private Spot spot;

        @BeforeEach
        void setUp() { spot = new Spot(Type.DISABLED); }

        @Test
        @DisplayName("accepts a DISABLED reservation")
        void accepts_disabled() {
            assertTrue(spot.makeReservation(res(Type.DISABLED, T10, T12)));
        }

        @Test
        @DisplayName("rejects a STANDARD reservation (type mismatch)")
        void rejects_standard() {
            assertFalse(spot.makeReservation(res(Type.STANDARD, T10, T12)));
        }

        @Test
        @DisplayName("rejects a FAMILY reservation (type mismatch)")
        void rejects_family() {
            assertFalse(spot.makeReservation(res(Type.FAMILY, T10, T12)));
        }
    }

    @Nested
    @DisplayName("makeReservation — FAMILY spot (exclusive)")
    class FamilySpot {

        private Spot spot;

        @BeforeEach
        void setUp() { spot = new Spot(Type.FAMILY); }

        @Test
        @DisplayName("accepts a FAMILY reservation")
        void accepts_family() {
            assertTrue(spot.makeReservation(res(Type.FAMILY, T10, T12)));
        }

        @Test
        @DisplayName("rejects a STANDARD reservation (type mismatch)")
        void rejects_standard() {
            assertFalse(spot.makeReservation(res(Type.STANDARD, T10, T12)));
        }

        @Test
        @DisplayName("rejects a DISABLED reservation (type mismatch)")
        void rejects_disabled() {
            assertFalse(spot.makeReservation(res(Type.DISABLED, T10, T12)));
        }
    }

    // ------------------------------------------------------------------
    // deleteReservation()
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("deleteReservation")
    class Delete {

        @Test
        @DisplayName("removes reservation from the spot")
        void removes_reservation() {
            Spot spot = new Spot(Type.STANDARD);
            Reservation r = res(Type.STANDARD, T10, T12);
            spot.makeReservation(r);
            spot.deleteReservation(r);
            assertFalse(spot.reservations().contains(r));
        }

        @Test
        @DisplayName("slot becomes available again after deletion")
        void slot_free_after_delete() {
            Spot spot = new Spot(Type.STANDARD);
            Reservation r1 = res(Type.STANDARD, T10, T12);
            spot.makeReservation(r1);
            spot.deleteReservation(r1);
            assertTrue(spot.makeReservation(res(Type.STANDARD, T10, T12)));
        }

        @Test
        @DisplayName("deleting non-existent reservation is a no-op")
        void delete_nonexistent_noop() {
            Spot spot = new Spot(Type.STANDARD);
            Reservation r = res(Type.STANDARD, T10, T12);
            assertDoesNotThrow(() -> spot.deleteReservation(r));
        }
    }

    // ------------------------------------------------------------------
    // reservations() — immutability guard
    // ------------------------------------------------------------------

    @Test
    @DisplayName("reservations() returns an unmodifiable view")
    void reservations_unmodifiable() {
        Spot spot = new Spot(Type.STANDARD);
        assertThrows(UnsupportedOperationException.class,
                () -> spot.reservations().add(res(Type.STANDARD, T10, T12)));
    }
}
