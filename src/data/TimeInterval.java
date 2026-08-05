package data;

import java.time.LocalDateTime;

public record TimeInterval(LocalDateTime from, LocalDateTime to) {
    public boolean overlaps(TimeInterval other) {
        // at least a minute has to pass between reservations
        return !(to.isBefore(other.from) || from.isAfter(other.to));
    }
}
