package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import data.Reservation;
import data.Spot;
import data.TimeInterval;
import data.Type;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Serializes and deserializes the Manager's state using Gson.
 * The resulting JSON is Base64-encoded and written to a plain .txt file.
 *
 * <p>Because {@link Spot} and {@link Reservation} contain non-serializable
 * types ({@link LocalDateTime}, circular references via spot number), custom
 * type adapters handle the conversion explicitly.
 *
 * <p>The reservation map is <em>not</em> persisted — it is rebuilt from the spot
 * data on load to keep a single source of truth.
 */
public class Serializer {

    private static final Gson GSON = buildGson();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Serializes {@code state} to JSON, Base64-encodes the result, and writes
     * it to {@code filePath}.
     *
     * @param state    the current state
     * @param filePath path to the output .txt file
     * @throws IOException if the file cannot be written
     */
    public static void save(Spot[] state, String filePath) throws IOException {
        String json = GSON.toJson(state, Spot[].class);
        String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        Files.writeString(Path.of(filePath), encoded, StandardCharsets.UTF_8);
    }

    /**
     * Reads the Base64-encoded file at {@code filePath}, decodes and parses
     * the JSON, and returns the reconstructed state.
     *
     * @param filePath path to the saved .txt file
     * @return the reconstructed state
     * @throws IOException           if the file cannot be read
     * @throws IllegalStateException if the JSON is malformed
     */
    public static Spot[] load(String filePath) throws IOException {
        String encoded = Files.readString(Path.of(filePath), StandardCharsets.UTF_8).trim();
        String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);

        Spot[] spots;
        try {
            spots = GSON.fromJson(json, Spot[].class);
        } catch (JsonSyntaxException e) {
            throw new IllegalStateException("Malformed save file: " + e.getMessage(), e);
        }

        // set spot numbers for each reservation
        for (int i = 0; i < spots.length; i++) {
            int number = i + 1;
            spots[i].reservations().forEach(res -> res.setSpotNumber(number));
        }

        return spots;
    }

    // -------------------------------------------------------------------------
    // Gson configuration
    // -------------------------------------------------------------------------

    private static Gson buildGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Spot.class, new SpotAdapter())
                .registerTypeAdapter(Reservation.class, new ReservationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // -------------------------------------------------------------------------
    // Type adapters
    // -------------------------------------------------------------------------

    /**
     * Serializes/deserializes {@link Spot}: type + reservation list.
     */
    private static class SpotAdapter
            implements JsonDeserializer<Spot> {

        @Override
        public Spot deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Type type = Type.valueOf(obj.get("type").getAsString());
            List<Reservation> reservations = new ArrayList<>();

            JsonArray reservationsJson = obj.getAsJsonArray("reservations");
            for (JsonElement el : reservationsJson)
                reservations.add(ctx.deserialize(el, Reservation.class));

            return new Spot(type, reservations);
        }
    }


    /**
     * Serializes/deserializes {@link Reservation}: all fields explicitly.
     */
    private static class ReservationAdapter
            implements JsonSerializer<Reservation>, JsonDeserializer<Reservation> {

        @Override
        public JsonElement serialize(Reservation r, java.lang.reflect.Type typeOfSrc, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", r.getType().name());
            obj.add("from", ctx.serialize(r.getInterval().from(), LocalDateTime.class));
            obj.add("to", ctx.serialize(r.getInterval().to(), LocalDateTime.class));
            obj.addProperty("plate", r.getLicencePlate());
            return obj;
        }

        @Override
        public Reservation deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            data.Type type = data.Type.valueOf(obj.get("type").getAsString());
            LocalDateTime from = ctx.deserialize(obj.get("from"), LocalDateTime.class);
            LocalDateTime to = ctx.deserialize(obj.get("to"), LocalDateTime.class);
            String plate = obj.get("plate").getAsString();

            return new Reservation(type, new TimeInterval(from, to), plate);
        }
    }

    /**
     * Serializes {@link LocalDateTime} as an ISO-8601 string (e.g. {@code "2026-08-06T10:00"}).
     */
    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime dt, java.lang.reflect.Type typeOfSrc, JsonSerializationContext ctx) {
            return new JsonPrimitive(dt.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}
