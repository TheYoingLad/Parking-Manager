package logic;

import data.Reservation;
import data.Spot;
import data.TimeInterval;
import data.Type;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link Serializer}.
 *
 * <p>These tests exercise the full save → load round-trip, verifying that the
 * Base64-encoded JSON file is readable and that all fields survive serialization.
 * A temporary directory is used so tests never touch real save files.
 */
@DisplayName("Serializer")
class SerializerTest {

    @TempDir
    Path tempDir;

    private static final LocalDateTime T10 = LocalDateTime.of(2026, 8, 6, 10, 0);
    private static final LocalDateTime T12 = LocalDateTime.of(2026, 8, 6, 12, 0);
    private static final LocalDateTime T14 = LocalDateTime.of(2026, 8, 6, 14, 0);
    private static final LocalDateTime T16 = LocalDateTime.of(2026, 8, 6, 16, 0);

    private String tempFile(String name) {
        return tempDir.resolve(name).toString();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Spot[] twoSpots() {
        Spot disabled = new Spot(Type.DISABLED);
        Reservation r1 = new Reservation(Type.DISABLED, new TimeInterval(T10, T12), "AA-001");
        r1.setSpotNumber(1);
        disabled.makeReservation(r1);

        Spot standard = new Spot(Type.STANDARD);
        Reservation r2 = new Reservation(Type.STANDARD, new TimeInterval(T14, T16), "BB-002");
        r2.setSpotNumber(2);
        standard.makeReservation(r2);

        return new Spot[]{disabled, standard};
    }

    // ------------------------------------------------------------------
    // save()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("save() creates a non-empty file")
    void save_creates_file() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(new Spot[]{new Spot(Type.STANDARD)}, path);
        assertTrue(Files.exists(Path.of(path)));
        assertTrue(Files.size(Path.of(path)) > 0);
    }

    @Test
    @DisplayName("save() writes valid Base64 content")
    void save_writes_valid_base64() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(new Spot[]{new Spot(Type.STANDARD)}, path);
        String encoded = Files.readString(Path.of(path), StandardCharsets.UTF_8).trim();
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encoded));
    }

    @Test
    @DisplayName("save() Base64 decodes to valid JSON array")
    void save_base64_decodes_to_json_array() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(new Spot[]{new Spot(Type.STANDARD)}, path);
        String encoded = Files.readString(Path.of(path), StandardCharsets.UTF_8).trim();
        String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8).trim();
        assertTrue(json.startsWith("["), "Expected a JSON array, got: " + json.substring(0, Math.min(40, json.length())));
    }

    // ------------------------------------------------------------------
    // Round-trip: save() → load()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("round-trip preserves number of spots")
    void roundtrip_spot_count() throws IOException {
        String path = tempFile("state.txt");
        Spot[] original = twoSpots();
        Serializer.save(original, path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(original.length, loaded.length);
    }

    @Test
    @DisplayName("round-trip preserves spot types")
    void roundtrip_spot_types() throws IOException {
        String path = tempFile("state.txt");
        Spot[] original = twoSpots();
        Serializer.save(original, path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(Type.DISABLED,  loaded[0].type());
        assertEquals(Type.STANDARD,  loaded[1].type());
    }

    @Test
    @DisplayName("round-trip preserves reservation count per spot")
    void roundtrip_reservation_count() throws IOException {
        String path = tempFile("state.txt");
        Spot[] original = twoSpots();
        Serializer.save(original, path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(1, loaded[0].reservations().size());
        assertEquals(1, loaded[1].reservations().size());
    }

    @Test
    @DisplayName("round-trip preserves reservation type")
    void roundtrip_reservation_type() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(twoSpots(), path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(Type.DISABLED, loaded[0].reservations().getFirst().getType());
        assertEquals(Type.STANDARD, loaded[1].reservations().getFirst().getType());
    }

    @Test
    @DisplayName("round-trip preserves licence plate")
    void roundtrip_plate() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(twoSpots(), path);
        Spot[] loaded = Serializer.load(path);
        assertEquals("AA-001", loaded[0].reservations().getFirst().getLicencePlate());
        assertEquals("BB-002", loaded[1].reservations().getFirst().getLicencePlate());
    }

    @Test
    @DisplayName("round-trip preserves arrival time")
    void roundtrip_from_time() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(twoSpots(), path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(T10, loaded[0].reservations().getFirst().getInterval().from());
        assertEquals(T14, loaded[1].reservations().getFirst().getInterval().from());
    }

    @Test
    @DisplayName("round-trip preserves departure time")
    void roundtrip_to_time() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(twoSpots(), path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(T12, loaded[0].reservations().getFirst().getInterval().to());
        assertEquals(T16, loaded[1].reservations().getFirst().getInterval().to());
    }

    @Test
    @DisplayName("round-trip restores spot number via post-load assignment")
    void roundtrip_spot_number() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(twoSpots(), path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(1, loaded[0].reservations().getFirst().getSpotNumber());
        assertEquals(2, loaded[1].reservations().getFirst().getSpotNumber());
    }

    @Test
    @DisplayName("round-trip with an empty spot array")
    void roundtrip_empty() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(new Spot[0], path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(0, loaded.length);
    }

    @Test
    @DisplayName("round-trip with spots that have no reservations")
    void roundtrip_spots_with_no_reservations() throws IOException {
        String path = tempFile("state.txt");
        Serializer.save(new Spot[]{new Spot(Type.STANDARD), new Spot(Type.DISABLED)}, path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(0, loaded[0].reservations().size());
        assertEquals(0, loaded[1].reservations().size());
    }

    @Test
    @DisplayName("round-trip with multiple reservations on a single spot")
    void roundtrip_multiple_reservations() throws IOException {
        String path = tempFile("state.txt");
        Spot spot = new Spot(Type.STANDARD);
        Reservation r1 = new Reservation(Type.STANDARD, new TimeInterval(T10, T12), "P1");
        Reservation r2 = new Reservation(Type.FAMILY,   new TimeInterval(T14, T16), "P2");
        spot.makeReservation(r1);
        spot.makeReservation(r2);
        Serializer.save(new Spot[]{spot}, path);
        Spot[] loaded = Serializer.load(path);
        assertEquals(2, loaded[0].reservations().size());
    }

    // ------------------------------------------------------------------
    // load() — error handling
    // ------------------------------------------------------------------

    @Test
    @DisplayName("load() throws IOException for a missing file")
    void load_missing_file() {
        assertThrows(IOException.class,
                () -> Serializer.load(tempFile("does_not_exist.txt")));
    }

    @Test
    @DisplayName("load() throws IllegalStateException for corrupted Base64")
    void load_corrupted_base64() throws IOException {
        String path = tempFile("bad.txt");
        Files.writeString(Path.of(path), "!!!not_valid_base64!!!", StandardCharsets.UTF_8);
        assertThrows(Exception.class, () -> Serializer.load(path));
    }

    @Test
    @DisplayName("load() throws IllegalStateException for valid Base64 but invalid JSON")
    void load_invalid_json() throws IOException {
        String path = tempFile("bad_json.txt");
        String encoded = Base64.getEncoder().encodeToString("{ not json at all ]]]".getBytes(StandardCharsets.UTF_8));
        Files.writeString(Path.of(path), encoded, StandardCharsets.UTF_8);
        assertThrows(IllegalStateException.class, () -> Serializer.load(path));
    }
}
