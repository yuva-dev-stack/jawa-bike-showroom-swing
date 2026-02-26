package com.jawa.showroom.service;

import com.jawa.showroom.model.Bike;
import com.jawa.showroom.model.Booking;
import com.jawa.showroom.model.User;

import java.io.*;
import java.util.*;

/**
 * DataStore provides file-based persistence for Users, Bikes, and Bookings.
 * Data is stored in simple CSV-like flat files inside a "data/" directory.
 *
 * Format per entity:
 *  - users.dat   : field1|field2|...|fieldN (one user per line)
 *  - bookings.dat: field1|field2|...|fieldN (one booking per line)
 *
 * Bikes are seeded in-memory (can be extended to bikes.dat).
 */
public class DataStore {

    private static final String DATA_DIR    = "data" + File.separator;
    private static final String USERS_FILE  = DATA_DIR + "users.dat";
    private static final String BOOKINGS_FILE = DATA_DIR + "bookings.dat";
    private static final String DELIM       = "\\|";
    private static final String WRITE_DELIM = "|";

    // ── In-memory caches ───────────────────────────────────────────────────────
    private final Map<String, User>    users    = new LinkedHashMap<>();
    private final List<Booking>        bookings = new ArrayList<>();
    private final List<Bike>           bikes    = new ArrayList<>();

    // ── Singleton ──────────────────────────────────────────────────────────────
    private static DataStore instance;

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
            instance.init();
        }
        return instance;
    }

    private DataStore() {}

    // ── Initialization ─────────────────────────────────────────────────────────

    private void init() {
        ensureDataDir();
        seedBikes();
        loadUsers();
        loadBookings();
    }

    private void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    // ── Bike Seeding ───────────────────────────────────────────────────────────

    /**
     * Seeds the catalog with real Jawa motorcycle data.
     * Extend this method or load from bikes.dat for more dynamic data.
     */
    private void seedBikes() {
        bikes.add(buildBike("JW001", "Jawa 42",      "Standard",
                "Jasper Red",    199000, 28.0, 12000, 18000, 4000,
                "334 cc", "Single Cyl, Liquid Cooled",
                "30.64 PS @ 8000 rpm", "32.74 Nm @ 6500 rpm",
                "6-Speed", "Petrol", "14.7 Litres", "~38 kmpl",
                "170 kg", "765 mm", "1369 mm", "170 mm",
                "300 mm Disc", "240 mm Disc",
                "43 mm USD Forks", "Mono Shock",
                "A retro-modern roadster with classic round headlamp & modern mechanics."));

        bikes.add(buildBike("JW002", "Jawa 42",      "Dual-Channel ABS",
                "Comet Blue",   211000, 28.0, 12500, 18500, 4000,
                "334 cc", "Single Cyl, Liquid Cooled",
                "30.64 PS @ 8000 rpm", "32.74 Nm @ 6500 rpm",
                "6-Speed", "Petrol", "14.7 Litres", "~38 kmpl",
                "170 kg", "765 mm", "1369 mm", "170 mm",
                "300 mm Disc (ABS)", "240 mm Disc (ABS)",
                "43 mm USD Forks", "Mono Shock",
                "Same retro soul, added safety with dual-channel ABS."));

        bikes.add(buildBike("JW003", "Jawa Perak",   "Bobber",
                "Mystic Copper", 204000, 28.0, 13000, 19000, 4500,
                "334 cc", "Single Cyl, Liquid Cooled",
                "30.2 PS @ 7800 rpm", "31.5 Nm @ 6500 rpm",
                "6-Speed", "Petrol", "13 Litres", "~35 kmpl",
                "178 kg", "730 mm", "1365 mm", "145 mm",
                "280 mm Disc", "220 mm Disc",
                "41 mm Telescopic Forks", "Hidden Mono Shock",
                "Bold bobber silhouette with low-slung stance & fender-less tail."));

        bikes.add(buildBike("JW004", "Jawa 300 Scrambler", "Standard",
                "Dune Beige",   218000, 28.0, 13500, 19500, 4500,
                "334 cc", "Single Cyl, Liquid Cooled",
                "30.64 PS @ 8000 rpm", "32.74 Nm @ 6500 rpm",
                "6-Speed", "Petrol", "14.7 Litres", "~35 kmpl",
                "172 kg", "800 mm", "1380 mm", "200 mm",
                "300 mm Disc", "240 mm Disc",
                "43 mm USD Forks (Long Travel)", "Mono Shock (Long Travel)",
                "Adventure-ready scrambler with high ground clearance & knobby tires."));

        bikes.add(buildBike("JW005", "Jawa 42 FJ",   "Standard",
                "Gloss Black",  239000, 28.0, 14000, 20000, 5000,
                "334 cc", "Single Cyl, Liquid Cooled",
                "31.1 PS @ 8500 rpm", "33.0 Nm @ 6500 rpm",
                "6-Speed", "Petrol", "14.7 Litres", "~37 kmpl",
                "175 kg", "770 mm", "1369 mm", "170 mm",
                "300 mm Disc (ABS)", "240 mm Disc (ABS)",
                "43 mm USD Forks", "Mono Shock",
                "The all-new FJ edition – sportier tune with modern digital console."));

        bikes.add(buildBike("JW006", "Jawa 350",     "Standard",
                "Vintage Maroon", 196000, 28.0, 11500, 17500, 3500,
                "294 cc", "Single Cyl, Air Cooled",
                "27.33 PS @ 7300 rpm", "28 Nm @ 5000 rpm",
                "5-Speed", "Petrol", "14 Litres", "~40 kmpl",
                "165 kg", "758 mm", "1355 mm", "165 mm",
                "280 mm Disc", "220 mm Drum",
                "41 mm Telescopic Forks", "Twin Shock",
                "Entry-level retro commuter – lightweight, fuel-efficient, timeless style."));
    }

    /** Helper to construct a fully-populated Bike object. */
    private Bike buildBike(String id, String model, String variant, String color,
                           double exShowroom, double gst, double rto, double insurance,
                           double handling,
                           String cc, String engineType, String power, String torque,
                           String transmission, String fuelType, String tank, String mileage,
                           String weight, String seatH, String wheelbase, String gc,
                           String fb, String rb, String fs, String rs,
                           String desc) {
        Bike b = new Bike();
        b.setBikeId(id);
        b.setModelName(model);
        b.setVariant(variant);
        b.setColor(color);
        b.setAvailable(true);
        b.setExShowroomPrice(exShowroom);
        b.setGstRate(gst);
        b.setRtoCharges(rto);
        b.setInsurancePremium(insurance);
        b.setHandlingCharges(handling);
        b.setEngineCC(cc);
        b.setEngineType(engineType);
        b.setMaxPower(power);
        b.setMaxTorque(torque);
        b.setTransmission(transmission);
        b.setFuelType(fuelType);
        b.setFuelTankCapacity(tank);
        b.setMileage(mileage);
        b.setKerbWeight(weight);
        b.setSeatHeight(seatH);
        b.setWheelbase(wheelbase);
        b.setGroundClearance(gc);
        b.setFrontBrake(fb);
        b.setRearBrake(rb);
        b.setFrontSuspension(fs);
        b.setRearSuspension(rs);
        b.setDescription(desc);
        return b;
    }

    // ── User Persistence ───────────────────────────────────────────────────────

    public void saveUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
        persistUsers();
    }

    public User findUser(String username) {
        return users.get(username.toLowerCase());
    }

    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    private void persistUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users.values()) {
                pw.println(join(u.getUsername(), u.getPasswordHash(), u.getFullName(),
                        u.getEmail(), u.getPhone(), u.getAddress(), u.getCreatedAt()));
            }
        } catch (IOException e) {
            System.err.println("[DataStore] Error saving users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File f = new File(USERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(DELIM, -1);
                if (p.length < 7) continue;
                User u = new User(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
                users.put(u.getUsername().toLowerCase(), u);
            }
        } catch (IOException e) {
            System.err.println("[DataStore] Error loading users: " + e.getMessage());
        }
    }

    // ── Booking Persistence ────────────────────────────────────────────────────

    public void saveBooking(Booking bk) {
        bookings.add(bk);
        persistBookings();
    }

    public List<Booking> getBookingsByUser(String username) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getUsername().equalsIgnoreCase(username)) result.add(b);
        }
        return result;
    }

    public List<Booking> getAllBookings() {
        return Collections.unmodifiableList(bookings);
    }

    private void persistBookings() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking bk : bookings) {
                pw.println(join(
                        bk.getBookingId(), bk.getUsername(), bk.getBookingDate(), bk.getStatus(),
                        bk.getBikeId(), bk.getBikeModelName(), bk.getBikeVariant(), bk.getBikeColor(),
                        str(bk.getExShowroomPrice()), str(bk.getGstAmount()),
                        str(bk.getRtoCharges()), str(bk.getInsurancePremium()),
                        str(bk.getHandlingCharges()), str(bk.getTotalOnRoadPrice()),
                        bk.isEmiChosen() ? "Y" : "N",
                        str(bk.getDownPayment()), str(bk.getLoanAmount()),
                        str(bk.getInterestRate()), String.valueOf(bk.getTenureMonths()),
                        str(bk.getEmiAmount()),
                        bk.getCustomerName(), bk.getCustomerEmail(),
                        bk.getCustomerPhone(), escape(bk.getCustomerAddress())));
            }
        } catch (IOException e) {
            System.err.println("[DataStore] Error saving bookings: " + e.getMessage());
        }
    }

    private void loadBookings() {
        File f = new File(BOOKINGS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(DELIM, -1);
                if (p.length < 24) continue;
                Booking bk = new Booking();
                bk.setBookingId(p[0]);    bk.setUsername(p[1]);
                bk.setBookingDate(p[2]);  bk.setStatus(p[3]);
                bk.setBikeId(p[4]);       bk.setBikeModelName(p[5]);
                bk.setBikeVariant(p[6]);  bk.setBikeColor(p[7]);
                bk.setExShowroomPrice(dbl(p[8]));  bk.setGstAmount(dbl(p[9]));
                bk.setRtoCharges(dbl(p[10]));      bk.setInsurancePremium(dbl(p[11]));
                bk.setHandlingCharges(dbl(p[12])); bk.setTotalOnRoadPrice(dbl(p[13]));
                bk.setEmiChosen("Y".equals(p[14]));
                bk.setDownPayment(dbl(p[15]));     bk.setLoanAmount(dbl(p[16]));
                bk.setInterestRate(dbl(p[17]));    bk.setTenureMonths(Integer.parseInt(p[18]));
                bk.setEmiAmount(dbl(p[19]));
                bk.setCustomerName(p[20]);  bk.setCustomerEmail(p[21]);
                bk.setCustomerPhone(p[22]); bk.setCustomerAddress(unescape(p[23]));
                bookings.add(bk);
            }
        } catch (IOException e) {
            System.err.println("[DataStore] Error loading bookings: " + e.getMessage());
        }
    }

    // ── Bike Access ────────────────────────────────────────────────────────────

    public List<Bike> getAllBikes() {
        return Collections.unmodifiableList(bikes);
    }

    public List<Bike> getAvailableBikes() {
        List<Bike> result = new ArrayList<>();
        for (Bike b : bikes) if (b.isAvailable()) result.add(b);
        return result;
    }

    public Bike findBikeById(String bikeId) {
        for (Bike b : bikes) if (b.getBikeId().equalsIgnoreCase(bikeId)) return b;
        return null;
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private String join(String... parts) {
        return String.join(WRITE_DELIM, parts);
    }

    private String str(double d) { return String.valueOf(d); }

    private double dbl(String s) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0.0; }
    }

    private String escape(String s)   { return s == null ? "" : s.replace("|", "~PIPE~").replace("\n", "~NL~"); }
    private String unescape(String s) { return s == null ? "" : s.replace("~PIPE~", "|").replace("~NL~", "\n"); }
}
