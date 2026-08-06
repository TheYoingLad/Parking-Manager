package data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeInterval")
class TimeIntervalTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 8, 6, 10, 0);

    private TimeInterval of(int fromHour, int toHour) {
        return new TimeInterval(BASE.withHour(fromHour), BASE.withHour(toHour));
    }

    // ------------------------------------------------------------------
    // overlaps()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("identical intervals overlap")
    void identical_overlap() {
        TimeInterval a = of(10, 12);
        assertTrue(a.overlaps(a));
    }

    @Test
    @DisplayName("fully contained interval overlaps")
    void contained_overlaps() {
        TimeInterval outer = of(8, 16);
        TimeInterval inner = of(10, 12);
        assertTrue(outer.overlaps(inner));
        assertTrue(inner.overlaps(outer));
    }

    @Test
    @DisplayName("partial overlap — second starts before first ends")
    void partial_overlap() {
        TimeInterval a = of(8, 12);
        TimeInterval b = of(11, 14);
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    @DisplayName("adjacent intervals (touching endpoints) overlap")
    void adjacent_endpoints_overlap() {
        // [8,10] and [10,12] — touching at 10 counts as overlap per the implementation
        TimeInterval a = of(8, 10);
        TimeInterval b = of(10, 12);
        assertTrue(a.overlaps(b));
    }

    @Test
    @DisplayName("non-overlapping intervals — b is entirely after a")
    void non_overlapping_after() {
        TimeInterval a = of(8, 10);
        TimeInterval b = of(11, 13);
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    @DisplayName("non-overlapping intervals — b is entirely before a")
    void non_overlapping_before() {
        TimeInterval a = of(14, 16);
        TimeInterval b = of(10, 12);
        assertFalse(a.overlaps(b));
    }
}
