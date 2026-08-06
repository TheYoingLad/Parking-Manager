# Parking Manager — User Manual

## Prerequisites

| Requirement | Minimum version |
|---|---|
| Java (JDK or JRE) | 17 |
| `gson-2.11.0.jar` | included in `lib/` |

---

## Running the Program

Compilation and launch are combined into a single command via the provided scripts.
Run from the **project root** — no separate compile step needed.

**Windows**
```bat
run.bat
```

**macOS / Linux**
```bash
chmod +x run.sh   # only needed once, to make the script executable
./run.sh
```

The script will print its progress before handing control to the application:
```
[1/2] Compiling...
[2/2] Starting Parking Manager...

=================================
      Parking Manager CLI        
=================================
```

If compilation fails, the script prints `[!] Compilation failed. Aborting.` and exits without launching the application.

<details>
<summary>Manual compile + run (advanced)</summary>

**Windows**
```powershell
New-Item -ItemType Directory -Force out\production\Parking-Manager | Out-Null
javac -cp "lib\gson-2.11.0.jar" -d out\production\Parking-Manager (Get-ChildItem src -Recurse -Filter "*.java").FullName
java -cp "out\production\Parking-Manager;lib\gson-2.11.0.jar" Main
```

**macOS / Linux**
```bash
mkdir -p out/production/Parking-Manager
find src -name "*.java" | xargs javac -cp "lib/gson-2.11.0.jar" -d out/production/Parking-Manager
java -cp "out/production/Parking-Manager:lib/gson-2.11.0.jar" Main
```
</details>

When the program starts you will see:

```
=================================
      Parking Manager CLI        
=================================

1. Help
2. Make Reservation
3. Delete Reservation
4. Show Parking Spot Details
5. Reset State
6. Exit
Enter your choice:
```

Type the number of the option you want and press **Enter**.

---

## Menu Options

### 1 · Help

Prints a short description of every menu option. No input required.

---

### 2 · Make Reservation

Books a parking spot for a vehicle.

**Step-by-step**

1. **Special spot?**
   - Enter `1` if you need a disabled or family spot.
     - Then choose `1` (Disabled) or `2` (Family).
   - Enter `2` for a standard spot.

2. **Arrival date/time** — format: `yyyy-MM-dd HH:mm`
   ```
   Enter arrival date (yyyy-MM-dd HH:mm): 2026-08-06 09:00
   ```

3. **Departure date/time** — must be strictly after arrival
   ```
   Enter departure date (yyyy-MM-dd HH:mm): 2026-08-06 11:30
   ```

4. **Licence plate** — any non-empty string, stored in upper-case
   ```
   Enter licence plate number: AB-123
   ```

**Possible outcomes**

| Outcome | Message shown |
|---|---|
| Booking confirmed | `[✓] Reservation successful! Details: …` |
| Your vehicle already has an overlapping booking | `[!] An existing reservation for this vehicle overlaps …` |
| All spots are full for that time range | `[!] All parking spots are occupied for the interval …` |

> **Spot assignment rules**
> - Disabled spots only accept DISABLED reservations.
> - Family spots only accept FAMILY reservations.
> - Standard spots accept any reservation type.
> - Spots are assigned automatically in order (spot 1 first).

---

### 3 · Delete Reservation

Cancels an existing reservation for a vehicle.

**Step-by-step**

1. **Licence plate**
   ```
   Enter licence plate number: AB-123
   ```

2. A numbered list of all reservations for that plate is shown. Enter the number of the one to cancel.
   ```
   1.
   Spot: #3
   Reservation Type: STANDARD
   From: 2026-08-06 09:00
   To:   2026-08-06 11:30

   Enter reservation number (1 - 1):
   ```

**Possible outcomes**

| Outcome | Message shown |
|---|---|
| Deletion confirmed | `[✓] Reservation successfully deleted.` |
| No booking found for that plate | `[-] No reservation found for this vehicle.` |

---

### 4 · Show Parking Spot Details

Displays the type and all current reservations for a single spot.

```
Enter parking spot number (1 - 10): 3
```

Example output:

```
[✓] Details of spot #3:
Parking Spot Type: STANDARD
Reservations:

Reservation Type: STANDARD
From: 2026-08-06 09:00
To:   2026-08-06 11:30
Licence Plate Number: AB-123
```

If the spot has no reservations, `Reservations: None` is shown.

---

### 5 · Reset State

Restores all reservations to the built-in default dataset (`default_state.txt`).

> [!WARNING]
> This will permanently overwrite the current save file (`parking_state.txt`). All reservations made since the last reset will be lost.

You will see:
```
[✓] State reset successful.
```

---

### 6 · Exit

Closes the application.

```
Goodbye!
```

---

## Input Format Reference

| Field | Format | Example |
|---|---|---|
| Date & time | `yyyy-MM-dd HH:mm` (24 h) | `2026-08-06 14:30` |
| Licence plate | Any non-empty text | `AB-123`, `TESTCAR` |
| Menu / list choice | Integer number | `2` |

---

## Save Files

| File | Purpose |
|---|---|
| `parking_state.txt` | Live save file — updated automatically after every reservation change |
| `default_state.txt` | Read-only default dataset — used on first run and on Reset |

Both files use Base64-encoded JSON and are stored in the **working directory** (wherever you ran the `java` command from).

**Load order on startup:**
1. `parking_state.txt` — if missing or corrupt, falls back to →
2. `default_state.txt` — if also missing or corrupt, falls back to →
3. Empty state (1 disabled spot + 9 standard spots, no reservations)

---

## Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| `Error: Main class not found` | Wrong working directory or classpath | Run from the project root; check the `out/` path |
| `[!] An error occurred during loading` | `parking_state.txt` is corrupted | Use option **5 Reset State** to restore defaults |
| `[!] All parking spots are occupied` | No spot is free for the chosen interval & type | Try a different time window or spot type |
| `[!] Invalid format` on date input | Wrong date format | Use exactly `yyyy-MM-dd HH:mm`, e.g. `2026-08-06 09:00` |
