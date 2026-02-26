package com.jawa.showroom.swing;

import com.jawa.showroom.model.Bike;
import com.jawa.showroom.model.Booking;
import com.jawa.showroom.model.User;
import com.jawa.showroom.service.BookingService;
import com.jawa.showroom.service.DataStore;
import com.jawa.showroom.util.FormatUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BikeListPanel — displays the Jawa catalogue as interactive cards
 * with REAL bike images loaded asynchronously from the web.
 * Falls back to a beautiful painted silhouette if images cannot load.
 *
 * IMAGE STRATEGY:
 *   Images are fetched on a background thread pool so the UI never blocks.
 *   Each card's canvas repaints once its image is ready.
 */
public class BikeListPanel extends JPanel {

    private final DataStore       dataStore;
    private final BookingService  bookingService;
    private final User            currentUser;
    private final DashboardFrame  dashboard;

    /** Cache of loaded images, keyed by bikeId */
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    /** Thread pool for async image loading */
    private final ExecutorService imageLoader = Executors.newFixedThreadPool(3);

    /**
     * Local bike image file paths mapped by bikeId.
     * Place your image files in the "images/" folder next to the project root.
     * Supported formats: .jpg, .jpeg, .png
     *
     * Expected filenames:
     *   images/JW001.jpg  ->  Jawa 42 Standard
     *   images/JW002.jpg  ->  Jawa 42 ABS
     *   images/JW003.jpg  ->  Jawa Perak Bobber
     *   images/JW004.jpg  ->  Jawa 300 Scrambler
     *   images/JW005.jpg  ->  Jawa 42 FJ
     *   images/JW006.jpg  ->  Jawa 350
     */
    private static final Map<String, String> BIKE_IMAGE_PATHS = new HashMap<>();
    static {
        BIKE_IMAGE_PATHS.put("JW001", "images/JW001.jpg");
        BIKE_IMAGE_PATHS.put("JW002", "images/JW002.jpg");
        BIKE_IMAGE_PATHS.put("JW003", "images/JW003.jpg");
        BIKE_IMAGE_PATHS.put("JW004", "images/JW004.jpg");
        BIKE_IMAGE_PATHS.put("JW005", "images/JW005.jpg");
        BIKE_IMAGE_PATHS.put("JW006", "images/JW006.jpg");
    }

    public BikeListPanel(DataStore dataStore, BookingService bookingService,
                         User currentUser, DashboardFrame dashboard) {
        this.dataStore      = dataStore;
        this.bookingService = bookingService;
        this.currentUser    = currentUser;
        this.dashboard      = dashboard;
        buildUI();
    }

    // ── Safe parent-window helper ──────────────────────────────────────────────
    private Frame getParentFrame() {
        Window w = SwingUtilities.windowForComponent(this);
        if (w instanceof Frame f) return f;
        for (Frame f : Frame.getFrames()) { if (f.isVisible()) return f; }
        return null;
    }

    // ── Main UI ───────────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_DARK);

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(AppTheme.BG_DARK);
        header.setBorder(new EmptyBorder(28, 32, 16, 32));

        JLabel title = AppTheme.titleLabel("🏍  Jawa Bike Catalogue");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = AppTheme.subLabel("Click any model to view full specs, pricing, and book your ride.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));
        header.add(AppTheme.redSeparator());
        add(header, BorderLayout.NORTH);

        // Bike Grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));
        grid.setBackground(AppTheme.BG_DARK);
        grid.setBorder(new EmptyBorder(20, 32, 32, 32));

        List<Bike> bikes = dataStore.getAvailableBikes();
        for (Bike b : bikes) {
            grid.add(buildBikeCard(b));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Bike Card with Real Image ─────────────────────────────────────────────

    private JPanel buildBikeCard(Bike bike) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.BG_CARD);
        card.setBorder(new LineBorder(AppTheme.BORDER_SUBTLE, 1, true));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Colour accent stripe at top
        JPanel stripe = new JPanel();
        stripe.setBackground(modelColor(bike.getModelName()));
        stripe.setPreferredSize(new Dimension(0, 5));
        stripe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        card.add(stripe);

        // ── Image canvas — shows real photo or painted silhouette ──────────────
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,          RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,      RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                BufferedImage img = imageCache.get(bike.getBikeId());

                if (img != null) {
                    // ── Draw the real bike photo ──────────────────────────────
                    // Scale to fill canvas while maintaining aspect ratio (cover)
                    int iw = img.getWidth(), ih = img.getHeight();
                    int cw = getWidth(), ch = getHeight();
                    double scale = Math.max((double) cw / iw, (double) ch / ih);
                    int dw = (int) (iw * scale), dh = (int) (ih * scale);
                    int dx = (cw - dw) / 2,      dy = (ch - dh) / 2;

                    // Dark overlay for consistent text readability
                    g2.drawImage(img, dx, dy, dw, dh, null);
                    GradientPaint fade = new GradientPaint(
                            0, 0,  new Color(24, 24, 34, 40),
                            0, ch, new Color(24, 24, 34, 180));
                    g2.setPaint(fade);
                    g2.fillRect(0, 0, cw, ch);

                    // Model name watermark over image
                    g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g2.setColor(new Color(255, 255, 255, 140));
                    String tag = bike.getModelName().toUpperCase();
                    g2.drawString(tag, 10, ch - 10);

                } else {
                    // ── Fallback: painted silhouette while image loads ─────────
                    drawBikeSilhouette(g2, getWidth(), getHeight(), modelColor(bike.getModelName()));

                    // "Loading..." indicator
                    g2.setFont(AppTheme.FONT_SMALL);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.drawString("Loading image...", 10, getHeight() - 10);
                }
            }
        };
        canvas.setBackground(new Color(20, 20, 30));
        canvas.setPreferredSize(new Dimension(0, 145));
        canvas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        card.add(canvas);

        // Kick off async image loading for this bike
        loadImageAsync(bike.getBikeId(), canvas);

        // ── Info section ──────────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(AppTheme.BG_CARD);
        info.setBorder(new EmptyBorder(14, 18, 18, 18));

        JLabel modelName = new JLabel(bike.getModelName());
        modelName.setFont(AppTheme.FONT_HEADING);
        modelName.setForeground(AppTheme.TEXT_PRIMARY);
        modelName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel variant = new JLabel(bike.getVariant() + "  •  " + bike.getColor());
        variant.setFont(AppTheme.FONT_SMALL);
        variant.setForeground(AppTheme.TEXT_SECONDARY);
        variant.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.BORDER_SUBTLE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Price row
        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setBackground(AppTheme.BG_CARD);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel priceLeft = new JPanel();
        priceLeft.setLayout(new BoxLayout(priceLeft, BoxLayout.Y_AXIS));
        priceLeft.setBackground(AppTheme.BG_CARD);
        JLabel exLabel = AppTheme.subLabel("Ex-Showroom");
        exLabel.setFont(AppTheme.FONT_SMALL);
        JLabel exPrice = new JLabel(FormatUtil.formatINR(bike.getExShowroomPrice()));
        exPrice.setFont(AppTheme.FONT_SUBHEAD);
        exPrice.setForeground(AppTheme.TEXT_PRIMARY);
        priceLeft.add(exLabel);
        priceLeft.add(exPrice);

        JPanel priceRight = new JPanel();
        priceRight.setLayout(new BoxLayout(priceRight, BoxLayout.Y_AXIS));
        priceRight.setBackground(AppTheme.BG_CARD);
        JLabel onRoadLabel = AppTheme.subLabel("On-Road");
        onRoadLabel.setFont(AppTheme.FONT_SMALL);
        onRoadLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel onRoadPrice = new JLabel(FormatUtil.formatINR(bike.getOnRoadPrice()));
        onRoadPrice.setFont(new Font("SansSerif", Font.BOLD, 15));
        onRoadPrice.setForeground(AppTheme.ACCENT_GOLD);
        onRoadPrice.setAlignmentX(Component.RIGHT_ALIGNMENT);
        priceRight.add(onRoadLabel);
        priceRight.add(onRoadPrice);

        priceRow.add(priceLeft,  BorderLayout.WEST);
        priceRow.add(priceRight, BorderLayout.EAST);

        // Spec chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setBackground(AppTheme.BG_CARD);
        chips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        chips.add(chip(bike.getEngineCC()));
        chips.add(chip(bike.getTransmission()));
        chips.add(chip(bike.getMileage()));

        // CTA button
        JButton viewBtn = AppTheme.primaryButton("View Details & Book  →");
        viewBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        viewBtn.addActionListener(e -> openDetailDialog(bike));

        info.add(modelName);
        info.add(Box.createVerticalStrut(3));
        info.add(variant);
        info.add(Box.createVerticalStrut(10));
        info.add(sep);
        info.add(Box.createVerticalStrut(10));
        info.add(priceRow);
        info.add(Box.createVerticalStrut(8));
        info.add(chips);
        info.add(Box.createVerticalStrut(14));
        info.add(viewBtn);

        card.add(info);

        // Hover border effect
        MouseAdapter hover = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(new LineBorder(AppTheme.ACCENT_RED, 1, true));
            }
            public void mouseExited(MouseEvent e) {
                card.setBorder(new LineBorder(AppTheme.BORDER_SUBTLE, 1, true));
            }
            public void mouseClicked(MouseEvent e) { openDetailDialog(bike); }
        };
        card.addMouseListener(hover);
        canvas.addMouseListener(hover);

        return card;
    }

    // ── Async Image Loading ───────────────────────────────────────────────────

    /**
    /**
     * Loads the bike image from the local filesystem on a background thread.
     * Path is relative to the working directory (i.e., the project root folder).
     * If the file is missing, the painted silhouette fallback is shown -- no crash.
     */
    private void loadImageAsync(String bikeId, JPanel canvas) {
        if (imageCache.containsKey(bikeId)) {
            System.out.println("[IMG] Cache hit for: " + bikeId);
            return;
        }
        System.out.println("[IMG] Queuing load for: " + bikeId);

        imageLoader.submit(() -> {
            String path = BIKE_IMAGE_PATHS.get(bikeId);
            System.out.println("[IMG] Loading " + bikeId + " from path: " + path);
            BufferedImage img = tryLoadLocalImage(path);
            if (img != null) {
                imageCache.put(bikeId, img);
                System.out.println("[IMG] SUCCESS cached: " + bikeId);
                SwingUtilities.invokeLater(canvas::repaint);
            } else {
                System.out.println("[IMG] FAILED (silhouette shown): " + bikeId);
            }
        });
    }

    /**
     * Reads a BufferedImage from a local file path.
     *
     * Resolution order (first match wins):
     *   1. Absolute path as-is  (e.g. "C:/pics/JW001.jpg")
     *   2. Relative to the JAR / .class file location (Eclipse project root)
     *   3. Relative to System working directory (fallback)
     */
    /**
     * Resolves a file path (absolute or relative) and reads it as a BufferedImage.
     * Uses a robust JPEG-safe reader that handles:
     *   - CMYK colour profiles  (common in photos saved from Photoshop / phones)
     *   - ICC profile mismatches that make ImageIO.read() return null silently
     *   - Standard sRGB JPEGs, PNGs, BMPs, etc.
     *
     * Resolution order: absolute path → project root (bin/../) → user.dir
     */
    private BufferedImage tryLoadLocalImage(String filePath) {
        if (filePath == null) return null;
        try {
            java.io.File file = resolveFile(filePath);
            if (file == null) {
                System.out.println("[IMG] NOT FOUND: " + filePath);
                return null;
            }
            System.out.println("[IMG] Reading: " + file.getAbsolutePath());
            return readImageRobust(file);
        } catch (Exception e) {
            System.out.println("[IMG] ERROR loading " + filePath + ": " + e.getMessage());
        }
        return null;
    }

    /** Tries absolute → project-root-relative → cwd-relative. Returns null if not found. */
    private java.io.File resolveFile(String filePath) {
        // 1. Absolute
        java.io.File f = new java.io.File(filePath);
        if (f.isAbsolute()) return f.exists() ? f : null;

        // 2. Relative to project root (Eclipse: bin/../  or  JAR dir)
        try {
            java.net.URL loc = BikeListPanel.class.getProtectionDomain().getCodeSource().getLocation();
            java.io.File codeDir = new java.io.File(loc.toURI());
            java.io.File root = codeDir.isDirectory() ? codeDir.getParentFile() : codeDir.getParentFile();
            java.io.File fromRoot = new java.io.File(root, filePath);
            if (fromRoot.exists() && fromRoot.isFile()) return fromRoot;
        } catch (Exception ignored) {}

        // 3. Relative to working directory
        java.io.File fromCwd = new java.io.File(System.getProperty("user.dir"), filePath);
        if (fromCwd.exists() && fromCwd.isFile()) return fromCwd;

        return null;
    }

    /**
     * Robust image reader that handles CMYK JPEGs and ICC profile issues.
     * ImageIO.read() silently returns null for CMYK JPEGs — this method fixes that.
     */
    private BufferedImage readImageRobust(java.io.File file) throws Exception {
        // First try standard ImageIO (works for sRGB JPEG, PNG, BMP, GIF)
        BufferedImage img = ImageIO.read(file);
        if (img != null) {
            System.out.println("[IMG] SUCCESS (standard): " + file.getName());
            return img;
        }

        // ImageIO returned null — likely a CMYK JPEG or ICC profile issue.
        // Fall back to reading raw bytes via ImageInputStream and manual conversion.
        System.out.println("[IMG] Standard read returned null, trying robust JPEG reader for: " + file.getName());
        try (javax.imageio.stream.ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            if (iis == null) return null;
            java.util.Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) return null;

            javax.imageio.ImageReader reader = readers.next();
            reader.setInput(iis, true, true); // ignoreMetadata = true  ← key for bad ICC profiles

            javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
            img = reader.read(0, param);
            reader.dispose();

            if (img == null) return null;

            // If it came back as TYPE_BYTE (CMYK), convert to standard RGB
            if (img.getType() == BufferedImage.TYPE_BYTE_INDEXED
                    || img.getColorModel().getNumComponents() == 4) {
                System.out.println("[IMG] Converting CMYK->RGB for: " + file.getName());
                BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = rgb.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                img = rgb;
            }

            System.out.println("[IMG] SUCCESS (robust): " + file.getName());
            return img;
        }
    }

    // ── Detail Dialog ─────────────────────────────────────────────────────────

    private void openDetailDialog(Bike bike) {
        JDialog dialog = new JDialog(getParentFrame(), bike.getModelName() + " — Details", true);
        dialog.setSize(720, 740);
        dialog.setLocationRelativeTo(dashboard);
        dialog.getContentPane().setBackground(AppTheme.BG_DARK);
        dialog.setLayout(new BorderLayout());

        // ── Top image banner ──────────────────────────────────────────────────
        JPanel imageBanner = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                BufferedImage img = imageCache.get(bike.getBikeId());
                int w = getWidth(), h = getHeight();
                if (img != null) {
                    double scale = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
                    int dw = (int)(img.getWidth() * scale), dh = (int)(img.getHeight() * scale);
                    g2.drawImage(img, (w-dw)/2, (h-dh)/2, dw, dh, null);
                } else {
                    g2.setColor(new Color(20,20,30));
                    g2.fillRect(0,0,w,h);
                    drawBikeSilhouette(g2, w, h, modelColor(bike.getModelName()));
                }
                // Gradient overlay so text below reads cleanly
                GradientPaint gp = new GradientPaint(0,0,new Color(0,0,0,0),0,h,new Color(20,20,30,230));
                g2.setPaint(gp);
                g2.fillRect(0,0,w,h);

                // Model name printed large over the image
                g2.setFont(new Font("SansSerif", Font.BOLD, 28));
                g2.setColor(Color.WHITE);
                g2.drawString(bike.getModelName() + " " + bike.getVariant(), 24, h - 44);
                g2.setFont(AppTheme.FONT_BODY);
                g2.setColor(new Color(212, 175, 55));
                g2.drawString(bike.getColor() + "  ·  " + bike.getBikeId(), 24, h - 20);
            }
        };
        imageBanner.setBackground(new Color(20,20,30));
        imageBanner.setPreferredSize(new Dimension(0, 200));
        dialog.add(imageBanner, BorderLayout.NORTH);

        // ── Scrollable specs body ─────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_DARK);
        body.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Description
        JTextArea desc = new JTextArea(bike.getDescription());
        desc.setFont(AppTheme.FONT_BODY);
        desc.setForeground(AppTheme.TEXT_SECONDARY);
        desc.setBackground(AppTheme.BG_DARK);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(desc);
        body.add(Box.createVerticalStrut(14));

        body.add(specSection("ENGINE & PERFORMANCE", new String[][]{
            {"Displacement",       bike.getEngineCC()},
            {"Engine Type",        bike.getEngineType()},
            {"Max Power",          bike.getMaxPower()},
            {"Max Torque",         bike.getMaxTorque()},
            {"Transmission",       bike.getTransmission()},
            {"Fuel Type",          bike.getFuelType()},
            {"Fuel Tank",          bike.getFuelTankCapacity()},
            {"Mileage (approx.)",  bike.getMileage()}
        }));
        body.add(Box.createVerticalStrut(12));
        body.add(specSection("CHASSIS & DIMENSIONS", new String[][]{
            {"Kerb Weight",        bike.getKerbWeight()},
            {"Seat Height",        bike.getSeatHeight()},
            {"Wheelbase",          bike.getWheelbase()},
            {"Ground Clearance",   bike.getGroundClearance()},
            {"Front Brake",        bike.getFrontBrake()},
            {"Rear Brake",         bike.getRearBrake()},
            {"Front Suspension",   bike.getFrontSuspension()},
            {"Rear Suspension",    bike.getRearSuspension()}
        }));
        body.add(Box.createVerticalStrut(12));
        body.add(pricingSection(bike));
        body.add(Box.createVerticalStrut(16));

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setBackground(AppTheme.BG_DARK);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton bookBtn = AppTheme.primaryButton("🏍  Book This Bike");
        bookBtn.addActionListener(e -> { dialog.dispose(); openBookingDialog(bike); });
        JButton closeBtn = AppTheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        btnRow.add(bookBtn);
        btnRow.add(closeBtn);
        body.add(btnRow);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ── Booking Dialog ────────────────────────────────────────────────────────

    private void openBookingDialog(Bike bike) {
        JDialog dialog = new JDialog(getParentFrame(), "Book — " + bike.getModelName(), true);
        dialog.setSize(520, 580);
        dialog.setLocationRelativeTo(dashboard);
        dialog.getContentPane().setBackground(AppTheme.BG_DARK);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_DARK);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = AppTheme.titleLabel("Confirm Booking");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        JLabel bikeInfo = AppTheme.subLabel(
                bike.getModelName() + " " + bike.getVariant() + " — " + bike.getColor());
        bikeInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(bikeInfo);
        content.add(Box.createVerticalStrut(8));
        content.add(AppTheme.redSeparator());
        content.add(Box.createVerticalStrut(16));

        // Payment mode
        JLabel payLbl = AppTheme.accentLabel("PAYMENT MODE");
        payLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(payLbl);
        content.add(Box.createVerticalStrut(8));

        ButtonGroup payGroup = new ButtonGroup();
        JRadioButton cashBtn = styledRadio("Full Cash Payment");
        JRadioButton emiBtn  = styledRadio("EMI (Loan)");
        cashBtn.setSelected(true);
        payGroup.add(cashBtn);
        payGroup.add(emiBtn);
        content.add(cashBtn);
        content.add(Box.createVerticalStrut(4));
        content.add(emiBtn);
        content.add(Box.createVerticalStrut(16));

        JPanel emiFields = buildEMIFields(bike.getOnRoadPrice());
        emiFields.setAlignmentX(Component.LEFT_ALIGNMENT);
        emiFields.setVisible(false);
        content.add(emiFields);

        emiBtn.addActionListener(e  -> emiFields.setVisible(true));
        cashBtn.addActionListener(e -> emiFields.setVisible(false));

        JLabel status = new JLabel(" ");
        status.setFont(AppTheme.FONT_SMALL);
        status.setForeground(AppTheme.TEXT_SUCCESS);
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(status);
        content.add(Box.createVerticalStrut(12));

        JButton confirmBtn = AppTheme.primaryButton("✓  Confirm Booking");
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBtn.addActionListener(e -> {
            try {
                boolean isEmi = emiBtn.isSelected();
                double downPay = 0, rate = 0;
                int    tenure  = 0;
                if (isEmi) {
                    downPay = ((Number)((JSpinner) emiFields.getClientProperty("downPay")).getValue()).doubleValue();
                    rate    = ((Number)((JSpinner) emiFields.getClientProperty("rate")).getValue()).doubleValue();
                    tenure  = ((Number)((JSpinner) emiFields.getClientProperty("tenure")).getValue()).intValue();
                }
                Booking booking = bookingService.createBooking(
                        currentUser, bike, isEmi, downPay, rate, tenure);
                status.setText("✓  Booking confirmed! ID: " + booking.getBookingId());
                confirmBtn.setEnabled(false);
                String invoice = bookingService.generateInvoice(booking);
                SwingUtilities.invokeLater(() -> {
                    dialog.dispose();
                    showInvoiceDialog(invoice, booking.getBookingId());
                    dashboard.navigateToBookings();
                });
            } catch (Exception ex) {
                status.setForeground(AppTheme.TEXT_ERROR);
                status.setText("✗  Error: " + ex.getMessage());
            }
        });

        JButton cancelBtn = AppTheme.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        content.add(confirmBtn);
        content.add(Box.createVerticalStrut(8));
        content.add(cancelBtn);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        dialog.add(scroll);
        dialog.setVisible(true);
    }

    // ── Invoice Dialog — FIXED (same fix as BookingsPanel) ───────────────────

    void showInvoiceDialog(String invoice, String bookingId) {
        JDialog dialog = new JDialog(getParentFrame(), "Invoice — " + bookingId, false);
        dialog.setSize(660, 640);
        dialog.setLocationRelativeTo(dashboard);
        dialog.getContentPane().setBackground(AppTheme.BG_DARK);
        dialog.setLayout(new BorderLayout());

        JPanel dialogHeader = new JPanel(new BorderLayout());
        dialogHeader.setBackground(AppTheme.ACCENT_RED);
        dialogHeader.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel dTitle = new JLabel("GST Tax Invoice / Booking Confirmation");
        dTitle.setFont(AppTheme.FONT_SUBHEAD);
        dTitle.setForeground(Color.WHITE);
        JLabel dId = new JLabel(bookingId);
        dId.setFont(AppTheme.FONT_SMALL);
        dId.setForeground(new Color(255,220,220));
        dialogHeader.add(dTitle, BorderLayout.WEST);
        dialogHeader.add(dId,    BorderLayout.EAST);
        dialog.add(dialogHeader, BorderLayout.NORTH);

        JTextArea area = new JTextArea(invoice);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setForeground(AppTheme.TEXT_PRIMARY);
        area.setBackground(AppTheme.BG_CARD);
        area.setEditable(false);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setBackground(AppTheme.BG_DARK);
        bottom.setBorder(new MatteBorder(1, 0, 0, 0, AppTheme.BORDER_SUBTLE));

        JButton saveBtn = AppTheme.secondaryButton("💾  Save to File");
        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("Invoice_" + bookingId + ".txt"));
            if (fc.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
                    pw.print(invoice);
                    JOptionPane.showMessageDialog(dialog, "✓  Saved!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JButton closeBtn = AppTheme.primaryButton("  Close  ");
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(saveBtn);
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ── EMI Fields Panel ──────────────────────────────────────────────────────

    private JPanel buildEMIFields(double onRoad) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel heading = AppTheme.accentLabel("EMI DETAILS");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(heading);
        p.add(Box.createVerticalStrut(10));
        JLabel onRoadLbl = AppTheme.subLabel("On-Road Price: " + FormatUtil.formatINR(onRoad));
        onRoadLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(onRoadLbl);
        p.add(Box.createVerticalStrut(12));

        JSpinner downPaySpin = AppTheme.spinner(0, onRoad, 5000, Math.round(onRoad * 0.2));
        JSpinner rateSpin    = AppTheme.spinner(1, 36, 0.25, 9.0);
        JSpinner tenureSpin  = AppTheme.spinner(6, 360, 6, 36);

        addSpinnerRow(p, "Down Payment (₹)", downPaySpin);
        addSpinnerRow(p, "Annual Rate (%)",   rateSpin);
        addSpinnerRow(p, "Tenure (months)",   tenureSpin);

        p.putClientProperty("downPay", downPaySpin);
        p.putClientProperty("rate",    rateSpin);
        p.putClientProperty("tenure",  tenureSpin);

        JLabel emiPreview = new JLabel("Monthly EMI: —");
        emiPreview.setFont(AppTheme.FONT_SUBHEAD);
        emiPreview.setForeground(AppTheme.ACCENT_GOLD);
        emiPreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        javax.swing.event.ChangeListener cl = ev -> {
            double dp  = ((Number) downPaySpin.getValue()).doubleValue();
            double r   = ((Number) rateSpin.getValue()).doubleValue();
            int    t   = ((Number) tenureSpin.getValue()).intValue();
            double emi = com.jawa.showroom.service.EMICalculator.calculateEMI(onRoad - dp, r, t);
            emiPreview.setText("Monthly EMI: " + FormatUtil.formatINR(emi));
        };
        downPaySpin.addChangeListener(cl);
        rateSpin.addChangeListener(cl);
        tenureSpin.addChangeListener(cl);
        cl.stateChanged(null);

        p.add(Box.createVerticalStrut(10));
        p.add(emiPreview);
        return p;
    }

    private void addSpinnerRow(JPanel p, String label, JSpinner spinner) {
        JLabel lbl = AppTheme.accentLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        spinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(spinner);
        p.add(Box.createVerticalStrut(10));
    }

    // ── Spec / Pricing Section Builders ───────────────────────────────────────

    private JPanel specSection(String sectionTitle, String[][] rows) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(14, 18, 14, 18)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel h = AppTheme.accentLabel(sectionTitle);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 6));
        grid.setBackground(AppTheme.BG_CARD);
        for (String[] row : rows) {
            JLabel k = new JLabel(row[0] + ":"); k.setFont(AppTheme.FONT_SMALL); k.setForeground(AppTheme.TEXT_SECONDARY);
            JLabel v = new JLabel(row[1]);        v.setFont(AppTheme.FONT_SMALL); v.setForeground(AppTheme.TEXT_PRIMARY);
            grid.add(k); grid.add(v);
        }
        p.add(grid);
        return p;
    }

    private JPanel pricingSection(Bike b) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.ACCENT_RED, 1, true),
                new EmptyBorder(14, 18, 14, 18)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel h = AppTheme.accentLabel("PRICING BREAKDOWN (INR)");
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(10));

        addPriceRow(p, "Ex-Showroom Price",      FormatUtil.formatINR(b.getExShowroomPrice()), false);
        addPriceRow(p, "GST (28%)",               FormatUtil.formatINR(b.getGstAmount()),       false);
        addPriceRow(p, "RTO Registration",         FormatUtil.formatINR(b.getRtoCharges()),      false);
        addPriceRow(p, "Insurance Premium",        FormatUtil.formatINR(b.getInsurancePremium()),false);
        addPriceRow(p, "Handling Charges",         FormatUtil.formatINR(b.getHandlingCharges()), false);
        p.add(AppTheme.redSeparator());
        p.add(Box.createVerticalStrut(6));
        addPriceRow(p, "TOTAL ON-ROAD PRICE",      FormatUtil.formatINR(b.getOnRoadPrice()),     true);
        return p;
    }

    private void addPriceRow(JPanel p, String label, String value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(AppTheme.BG_CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel k = new JLabel(label); k.setFont(highlight ? AppTheme.FONT_SUBHEAD : AppTheme.FONT_SMALL);
        k.setForeground(highlight ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY);
        JLabel v = new JLabel(value); v.setFont(highlight ? AppTheme.FONT_SUBHEAD : AppTheme.FONT_SMALL);
        v.setForeground(highlight ? AppTheme.ACCENT_GOLD : AppTheme.TEXT_PRIMARY);
        row.add(k, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        p.add(row); p.add(Box.createVerticalStrut(4));
    }

    // ── Small Helpers ─────────────────────────────────────────────────────────

    private JLabel chip(String text) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(AppTheme.FONT_SMALL);
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setOpaque(true);
        l.setBackground(new Color(40, 40, 55));
        l.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(2, 0, 2, 0)));
        return l;
    }

    private JRadioButton styledRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(AppTheme.FONT_BODY);
        rb.setForeground(AppTheme.TEXT_PRIMARY);
        rb.setBackground(AppTheme.BG_DARK);
        rb.setFocusPainted(false);
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return rb;
    }

    private Color modelColor(String name) {
        return switch (name) {
            case "Jawa 42"            -> new Color(196, 30,  58);
            case "Jawa Perak"         -> new Color(139, 90,  43);
            case "Jawa 300 Scrambler" -> new Color(85,  130, 80);
            case "Jawa 42 FJ"         -> new Color(60,   60, 80);
            case "Jawa 350"           -> new Color(120,  40, 40);
            default                   -> AppTheme.ACCENT_RED;
        };
    }

    /** Painted silhouette fallback — shown while real image loads or if offline */
    private void drawBikeSilhouette(Graphics2D g2, int w, int h, Color accent) {
        int cx = w / 2, cy = h / 2;
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(cx - 70, cy + 30, 140, 14);
        g2.setColor(new Color(50, 50, 60));
        g2.fillOval(cx - 80, cy - 20, 52, 52);
        g2.setColor(accent); g2.setStroke(new BasicStroke(4));
        g2.drawOval(cx - 80, cy - 20, 52, 52);
        g2.setColor(new Color(80, 80, 90)); g2.fillOval(cx - 68, cy - 8, 28, 28);
        g2.setColor(new Color(50, 50, 60)); g2.fillOval(cx + 30, cy - 20, 52, 52);
        g2.setColor(accent); g2.drawOval(cx + 30, cy - 20, 52, 52);
        g2.setColor(new Color(80, 80, 90)); g2.fillOval(cx + 42, cy - 8, 28, 28);
        g2.setColor(new Color(60, 60, 75));
        int[] xp = {cx-54,cx-20,cx+10,cx+56,cx+44,cx+10,cx-10};
        int[] yp = {cy+16,cy-18,cy-30,cy-10,cy+16,cy+16,cy+16};
        g2.fillPolygon(xp, yp, xp.length);
        g2.setColor(accent); g2.fillRoundRect(cx - 22, cy - 36, 52, 22, 10, 10);
        g2.setColor(new Color(30, 30, 40)); g2.fillRoundRect(cx - 48, cy - 24, 40, 10, 6, 6);
        g2.setColor(new Color(100, 100, 120)); g2.setStroke(new BasicStroke(3));
        g2.drawLine(cx + 44, cy - 10, cx + 52, cy - 28);
        g2.drawLine(cx + 40, cy - 28, cx + 58, cy - 24);
        g2.setColor(new Color(90, 85, 75));
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(cx - 65, cy + 4, 28, 20, 180, -150);
        g2.setColor(new Color(255, 240, 180, 80)); g2.fillOval(cx + 48, cy - 18, 18, 14);
        g2.setColor(new Color(255, 240, 180));     g2.fillOval(cx + 52, cy - 14, 10, 8);
    }
}
