# Parking Manager — API Description & System Design

## Operations

### `makeReservation`

**Preconditions**
- `type` ∈ `{STANDARD, DISABLED, FAMILY}`
- `from` < `to` (departure must be strictly after arrival)
- `licencePlate` is non-empty

**Algorithm**
1. Check `reservationMap` for an existing booking for this plate that overlaps the requested interval → reject with a conflict message if found.
2. Scan `spots[0..9]` in order; call `spot.makeReservation(reservation)` on each until one succeeds.
3. If no spot accepted the reservation → print "all spots occupied" and return.
4. Otherwise: assign `spotNumber`, update `reservationMap`, call `save()`.

**Outcomes**

| Condition | Result |
|---|---|
| Same plate already booked during that interval | Rejected — prints conflict details |
| No spot available for that interval & type | Rejected — prints "all spots occupied" |
| Success | Reservation stored, state saved, details printed |

---

### `deleteReservation`

**Preconditions**
- `licencePlate` is non-empty

**Algorithm**
1. Look up `licencePlate` in `reservationMap` → reject if not found.
2. Prompt the user to select which reservation (by 1-based index).
3. Remove from `reservationMap` and from the owning `Spot`.
4. Call `save()`.

**Outcomes**

| Condition | Result |
|---|---|
| Plate has no reservations | Prints "no reservation found" |
| Success | Reservation removed, state saved |

---

### `showParkingSpotDetails`

**Preconditions**
- Spot number ∈ `[1, 10]`

Prints the spot type and all its current reservations (plate + interval for each).

---

### `resetState`

Copies `default_state.txt` over `parking_state.txt`, then calls `load()` and `save()`.

---

## Persistence — `Serializer`

| Method | Signature | Description |
|---|---|---|
| `save` | `save(Spot[] state, String filePath) throws IOException` | Serialises spots to JSON, Base64-encodes, writes to file |
| `load` | `load(String filePath) throws IOException` | Reads file, Base64-decodes, deserialises JSON to `Spot[]`; assigns spot numbers |

**On-disk format**

```
<file>          ::= base64( <json> )
<json>          ::= "[" <spot>* "]"
<spot>          ::= { "type": <type>, "reservations": [ <reservation>* ] }
<reservation>   ::= { "type": <type>, "from": <iso8601>, "to": <iso8601>, "plate": <string> }
<type>          ::= "STANDARD" | "DISABLED" | "FAMILY"
<iso8601>       ::= e.g. "2026-08-06T10:00"
```

**Load fallback chain**

```
parking_state.txt  →(fail)→  default_state.txt  →(fail)→  empty state (1 DISABLED + 9 STANDARD)
```

---

## Constraints Summary

| Rule | Where enforced |
|---|---|
| Exclusive spots reject wrong-type reservations | `Spot.makeReservation()` |
| No two reservations on the same spot may overlap | `Spot.makeReservation()` |
| No two reservations for the same plate may overlap | `Manager.makeReservation()` |
| Departure must be strictly after arrival | `Manager.makeReservation()` |
| Licence plate must be non-empty | `Manager.makeReservation()` / `deleteReservation()` |
| Spot number must be in `[1, 10]` | `Manager.showParkingSpotDetails()` |
