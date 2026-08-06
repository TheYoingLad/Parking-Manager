# Parking Manager — System Design

## Overview

`Manager` is the top-level controller of the Parking Manager application. It owns all mutable state (parking spots and the reservation index), orchestrates the interactive CLI, and delegates persistence to [`Serializer`](file:///d:/Suli/Uni/Parking-Manager/src/logic/Serializer.java).

---

## Data Model

### `Type` — `data.Type`

Enum representing the category of a parking spot or reservation.

| Value | `isExclusive` | Meaning |
|---|---|---|
| `STANDARD` | `false` | Regular spot; accepts any reservation type |
| `DISABLED` | `true` | Reserved for disabled badge holders only |
| `FAMILY` | `true` | Reserved for families with ≥ 3 children only |

> Exclusive spots (`isExclusive = true`) reject any reservation whose type does not exactly match the spot's type.

---

### `TimeInterval` — `data.TimeInterval`

Immutable record holding a half-open arrival/departure window.

| Field | Type | Description |
|---|---|---|
| `from` | `LocalDateTime` | Arrival (inclusive) |
| `to` | `LocalDateTime` | Departure (inclusive in overlap check) |

#### `boolean overlaps(TimeInterval other)`
Returns `true` if the two intervals share any point in time.  
Two intervals that merely touch at an endpoint **do** overlap.

```
[08:00 – 10:00] overlaps [10:00 – 12:00] → true
[08:00 – 10:00] overlaps [11:00 – 12:00] → false
```

---

### `Reservation` — `data.Reservation`

Represents a single parking booking.

| Field | Type | Mutable | Description |
|---|---|---|---|
| `type` | `Type` | No | Reservation category |
| `interval` | `TimeInterval` | No | Arrival / departure window |
| `licencePlate` | `String` | No | Vehicle identifier (stored upper-case) |
| `spotNumber` | `int` | **Yes** | 1-based index of the assigned spot (0 until assigned) |

**Key methods**

| Method | Description |
|---|---|
| `getType()` | Returns the reservation type |
| `getInterval()` | Returns the time interval |
| `getLicencePlate()` | Returns the licence plate |
| `getSpotNumber()` | Returns the assigned spot number (0 = unassigned) |
| `setSpotNumber(int)` | Sets the spot number after assignment |
| `toString(boolean showSpot, boolean showPlate)` | Human-readable summary; flags control which fields to include |

---

### `Spot` — `data.Spot`

Represents a physical parking space.

| Field | Type | Description |
|---|---|---|
| `type` | `Type` | Spot category (determines exclusivity) |
| `reservations` | `List<Reservation>` | Current bookings (unmodifiable view via `reservations()`) |

**Key methods**

| Method | Returns | Description |
|---|---|---|
| `makeReservation(Reservation)` | `boolean` | Attempts to book the spot. Returns `false` if the type is incompatible or any existing reservation overlaps. |
| `deleteReservation(Reservation)` | `void` | Removes the reservation; no-op if not present. |
| `reservations()` | `List<Reservation>` (unmodifiable) | Read-only view of all current bookings. |
| `type()` | `Type` | Returns the spot's category. |

#### Reservation rules enforced by `makeReservation`

1. **Type exclusivity** — if `spot.type.isExclusive`, the incoming reservation's type must equal the spot's type.
2. **No time overlap** — the incoming interval must not overlap any existing reservation on this spot.

---

## State

`Manager` holds:

```
Spot[] spots               // fixed size 10; index 0 = DISABLED, 1-9 = STANDARD
Map<String, List<Reservation>> reservationMap  // plate → reservations (derived from spots)
```

The reservation map is always rebuilt from spot data on load and is never persisted independently.

### Default spot layout

| Spot # | Type |
|---|---|
| 1 | `DISABLED` |
| 2 – 10 | `STANDARD` |

---

## Lifecycle

```
Manager.start()
 ├─ load()          ← tries parking_state.txt, falls back to default_state.txt, then empty state
 ├─ save()          ← immediately persists the loaded state
 └─ menu loop
      ├─ showHelp()
      ├─ makeReservation(Scanner)
      ├─ deleteReservation(Scanner)
      ├─ showParkingSpotDetails(Scanner)
      ├─ resetState()
      └─ exit
```