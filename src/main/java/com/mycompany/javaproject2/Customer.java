package com.mycompany.javaproject2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

import java.time.LocalDate;
import java.time.YearMonth;

class CustomerStartFrame extends JFrame {
    public CustomerStartFrame() {
        setTitle("Customer Portal");
        setSize(500, 350);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 70, 50, 70));
        panel.setBackground(UI.light);

        JLabel title = UI.title("Customer Portal");
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton login = UI.tealButton("Login");
        JButton signup = UI.button("Create Account");
        JButton back = UI.button("Back");

        panel.add(title);
        panel.add(login);
        panel.add(signup);
        panel.add(back);

        login.addActionListener(e -> new CustomerLoginFrame());
        signup.addActionListener(e -> new CustomerSignupFrame());
        back.addActionListener(e -> {
            new RoleFrame();
            dispose();
        });

        add(panel);
        setVisible(true);
    }
}


class CustomerSignupFrame extends JFrame {
    JTextField name = new JTextField();
    JTextField nid = new JTextField();
    JTextField phone = new JTextField("+966");
    JTextField email = new JTextField();
    JPasswordField pass = new JPasswordField();

    public CustomerSignupFrame() {
        setTitle("Create Customer Account");
        setSize(450, 430);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        p.setBackground(UI.light);

        p.add(new JLabel("Full Name:")); p.add(name);
        p.add(new JLabel("National ID:")); p.add(nid);
        p.add(new JLabel("Phone:")); p.add(phone);
        p.add(new JLabel("Email:")); p.add(email);
        p.add(new JLabel("Password:")); p.add(pass);

        JButton create = UI.tealButton("Create Account");
        p.add(new JLabel(""));
        p.add(create);

        create.addActionListener(e -> createAccount());

        add(p);
        setVisible(true);
    }

    void createAccount() {
        try {
            if (!email.getText().trim().toLowerCase().endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Email must be a Gmail address, for example: name@gmail.com");
                return;
            }

            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql = "INSERT INTO customers(full_name,national_id,phone,email,password) VALUES(?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name.getText());
            ps.setString(2, nid.getText());
            ps.setString(3, phone.getText());
            ps.setString(4, email.getText());
            ps.setString(5, new String(pass.getPassword()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully.");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}


class CustomerLoginFrame extends JFrame {
    JTextField email = new JTextField();
    JPasswordField pass = new JPasswordField();

    public CustomerLoginFrame() {
        setTitle("Customer Login");
        setSize(400, 280);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        p.setBackground(UI.light);

        p.add(new JLabel("Email:")); p.add(email);
        p.add(new JLabel("Password:")); p.add(pass);

        JButton login = UI.tealButton("Login");
        p.add(new JLabel(""));
        p.add(login);

        login.addActionListener(e -> login());

        add(p);
        setVisible(true);
    }

    void login() {
        try {
            if (!email.getText().trim().toLowerCase().endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Email must be a Gmail address.");
                return;
            }

            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql = "SELECT * FROM customers WHERE email=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, email.getText());
            ps.setString(2, new String(pass.getPassword()));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("customer_id");
                String name = rs.getString("full_name");
                new CustomerHomeFrame(id, name);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}


class CustomerHomeFrame extends JFrame {
    int customerId;
    String customerName;

    JComboBox<String> fromBox = new JComboBox<>();
    JComboBox<String> toBox = new JComboBox<>();
    JTextField dateField = new JTextField(LocalDate.now().toString());
    JTextField returnDateField = new JTextField("");
    JComboBox<String> tripTypeBox = new JComboBox<>(new String[]{"One Way", "Round Trip"});

    JSpinner adultSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
    JSpinner childSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
    JSpinner infantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));

    CardLayout formCards = new CardLayout();
    JPanel formContainer = new JPanel(formCards);
    JButton trainTab;
    JButton cargoTab;
    JButton find;

    public CustomerHomeFrame(int customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;

        setTitle("Railway Booking - Customer");
        setSize(1250, 780);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(246, 248, 250));

// ── SAR-style Navigation Bar ──────────────────────────────────────
JPanel nav = new JPanel(new BorderLayout());
nav.setBackground(new Color(18, 40, 58));
nav.setPreferredSize(new Dimension(1250, 50));
nav.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
logoPanel.setOpaque(false);

JLabel sarLabel = new JLabel("SAR") {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString("SAR", 0, 24);
        g2.dispose();
    }
};
sarLabel.setPreferredSize(new Dimension(50, 30));

JPanel navDivider = new JPanel();
navDivider.setBackground(new Color(80, 110, 130));
navDivider.setPreferredSize(new Dimension(1, 28));

JPanel arabicPanel = new JPanel(new BorderLayout());
arabicPanel.setOpaque(false);
JLabel arabicLine1 = new JLabel("الخطوط الحديدية السعودية");
arabicLine1.setFont(new Font("Arial", Font.BOLD, 9));
arabicLine1.setForeground(Color.WHITE);
arabicLine1.setHorizontalAlignment(SwingConstants.CENTER);
JLabel arabicLine2 = new JLabel("SAUDI ARABIA RAILWAYS");
arabicLine2.setFont(new Font("Arial", Font.PLAIN, 7));
arabicLine2.setForeground(new Color(180, 200, 215));
arabicLine2.setHorizontalAlignment(SwingConstants.CENTER);
arabicPanel.add(arabicLine1, BorderLayout.NORTH);
arabicPanel.add(arabicLine2, BorderLayout.SOUTH);

logoPanel.add(sarLabel);
logoPanel.add(navDivider);
logoPanel.add(arabicPanel);

String[] navItems = {"About SAR", "Passenger Services", "Companies Services", "Help & Support"};
JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
navRight.setOpaque(false);

for (String item : navItems) {
    JLabel link = new JLabel(item);
    link.setFont(new Font("Arial", Font.PLAIN, 13));
    link.setForeground(new Color(210, 225, 235));
    link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    link.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent e) { link.setForeground(Color.WHITE); }
        public void mouseExited(java.awt.event.MouseEvent e) { link.setForeground(new Color(210, 225, 235)); }
        public void mouseClicked(java.awt.event.MouseEvent e) {
            new InfoFrame(item);
        }
    });
    navRight.add(link);
    JLabel sep = new JLabel("|");
    sep.setForeground(new Color(80, 110, 130));
    sep.setFont(new Font("Arial", Font.PLAIN, 12));
    navRight.add(sep);
}

JLabel welcome = new JLabel("Welcome, " + customerName + "   ");
welcome.setForeground(Color.WHITE);
welcome.setFont(new Font("Arial", Font.BOLD, 13));

JButton logout = UI.button("Logout");
logout.addActionListener(e -> { new RoleFrame(); dispose(); });

navRight.add(welcome);
navRight.add(logout);

nav.add(logoPanel, BorderLayout.WEST);
nav.add(navRight, BorderLayout.EAST);
// ── End Navbar ───────────────────────────────────────────────────
        JLayeredPane hero = new JLayeredPane();
        hero.setPreferredSize(new Dimension(1250, 260));

        JLabel heroImage = UI.imageBox("3.jpg", 1250, 260);
        heroImage.setBounds(0, 0, 1250, 260);

        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, 1250, 260);

        JLabel heroText = new JLabel(
                "<html>"
                        + "<h1 style='color:white;'>Plan your next journey</h1>"
                        + "<p style='color:white;'>Search, book, pay, and manage your train tickets easily.</p>"
                        + "</html>"
        );
        heroText.setFont(new Font("Arial", Font.BOLD, 24));
        heroText.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 50));
        textPanel.setBounds(0, 0, 1250, 260);
        textPanel.add(heroText, BorderLayout.WEST);

        hero.add(heroImage, Integer.valueOf(0));
        hero.add(overlay, Integer.valueOf(1));
        hero.add(textPanel, Integer.valueOf(2));

        hero.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = hero.getWidth();
                int h = hero.getHeight();
                heroImage.setBounds(0, 0, w, h);
                overlay.setBounds(0, 0, w, h);
                textPanel.setBounds(0, 0, w, h);
            }
        });

        JPanel cardOuter = new JPanel(new BorderLayout());
        cardOuter.setBackground(Color.WHITE);
        cardOuter.setBorder(BorderFactory.createEmptyBorder(25, 55, 25, 55));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225)));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(Color.WHITE);

        trainTab = new JButton("   Train Tickets  ");
        trainTab.setFocusPainted(false);
        trainTab.setFont(new Font("Arial", Font.BOLD, 15));

        cargoTab = new JButton("   Car Cargo  ");
        cargoTab.setFocusPainted(false);
        cargoTab.setFont(new Font("Arial", Font.BOLD, 15));

        tabs.add(trainTab);
        tabs.add(cargoTab);

        JPanel trainForm = buildTrainForm();
        JPanel cargoForm = buildCargoForm();

        formContainer.add(trainForm, "TRAIN");
        formContainer.add(cargoForm, "CARGO");

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(UI.paleBlue);
        bottom.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));

        JButton manage = new JButton("Manage Bookings");
        manage.setHorizontalAlignment(SwingConstants.LEFT);
        manage.setBorderPainted(false);
        manage.setContentAreaFilled(false);
        manage.setFont(new Font("Arial", Font.BOLD, 17));
        manage.setForeground(new Color(20, 85, 100));
        manage.addActionListener(e -> new MyBookingsFrame(customerId));

        bottom.add(manage, BorderLayout.CENTER);

        card.add(tabs, BorderLayout.NORTH);
        card.add(formContainer, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        cardOuter.add(card, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(246, 248, 250));
        content.add(hero, BorderLayout.NORTH);
        content.add(cardOuter, BorderLayout.CENTER);

        trainTab.addActionListener(e -> showTrainTab());
        cargoTab.addActionListener(e -> showCargoTab());

        showTrainTab();

        main.add(nav, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);

        add(main);
        UI.maximizeWindow(this);
        setVisible(true);
    }

    JPanel buildTrainForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        JLabel journey = new JLabel("Journey Details");
        journey.setFont(new Font("Arial", Font.BOLD, 24));
        journey.setBorder(BorderFactory.createEmptyBorder(18, 25, 0, 25));

        JPanel form = new JPanel(new GridLayout(4, 4, 18, 14));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        loadStations();

        form.add(label("Departure"));
        form.add(label("Arrival"));
        form.add(label("Travel Date"));
        form.add(label("Trip Type"));

        dateField.setEditable(false);
        returnDateField.setEditable(false);
        JButton mainDateButton = new JButton("📅");
        mainDateButton.setFocusPainted(false);
        mainDateButton.setBackground(Color.WHITE);
        mainDateButton.addActionListener(e -> {
        String type = tripTypeBox.getSelectedItem().toString();

    if (type.equals("Round Trip")) {
        showDualCalendarDialog(dateField, returnDateField);
    } else {
        showCalendarDialog(dateField);
    }
});

        form.add(fromBox);
        form.add(toBox);
        form.add(dateInputPanel(dateField, mainDateButton));
        form.add(tripTypeBox);

        form.add(label("Adult  +12 Years"));
        form.add(label("Child  2~11 Years"));
        form.add(label("Infant  0~1 Years"));
        form.add(label(""));

        form.add(adultSpinner);
        form.add(childSpinner);
        form.add(infantSpinner);

        find = UI.button("Find Trains  →");
        find.setFont(new Font("Arial", Font.BOLD, 17));
        form.add(find);

        find.addActionListener(e -> {
            String from = fromBox.getSelectedItem().toString();
            String to = toBox.getSelectedItem().toString();

            if (from.equals(to)) {
                JOptionPane.showMessageDialog(this, "Departure and arrival stations cannot be the same.");
                return;
            }

            int adult = (Integer) adultSpinner.getValue();
            int child = (Integer) childSpinner.getValue();
            int infant = (Integer) infantSpinner.getValue();
            String tripType = tripTypeBox.getSelectedItem().toString();
if (tripType.equals("Round Trip") && returnDateField.getText().trim().isEmpty()) {
    JOptionPane.showMessageDialog(this, "Please select return date.");
    return;
}

            new SearchTripsFrame(customerId, from, to, dateField.getText(), returnDateField.getText(), adult, child, infant, tripType);
        });

        wrapper.add(journey, BorderLayout.NORTH);
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    JPanel buildCargoForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JPanel topLine = new JPanel(new BorderLayout());
        topLine.setBackground(Color.WHITE);

        JLabel journey = new JLabel("Journey Details");
        journey.setFont(new Font("Arial", Font.BOLD, 24));
        journey.setForeground(new Color(35, 35, 35));

        JLabel oneWay = new JLabel("One Way", SwingConstants.CENTER);
        oneWay.setOpaque(true);
        oneWay.setBackground(Color.WHITE);
        oneWay.setForeground(UI.tealDark);
        oneWay.setFont(new Font("Arial", Font.BOLD, 14));
        oneWay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        topLine.add(journey, BorderLayout.WEST);
        topLine.add(oneWay, BorderLayout.EAST);

        JComboBox<String> cargoFrom = new JComboBox<>(new String[]{"Riyadh Station", "Qurayyat Station"});
        JComboBox<String> cargoTo = new JComboBox<>(new String[]{"Qurayyat Station", "Riyadh Station"});
        JTextField cargoDate = new JTextField("Day/Month/Year");
        cargoDate.setEditable(false);

        JButton dateButton = new JButton("📅");
        dateButton.setFocusPainted(false);
        dateButton.setBackground(Color.WHITE);
        dateButton.setBorderPainted(false);
        dateButton.addActionListener(e -> showCalendarDialog(cargoDate));

        JPanel row = new JPanel(new GridLayout(1, 3, 35, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(1600, 95));
        row.add(cargoFieldWithLabel("Departure", "", cargoFrom));
        row.add(cargoFieldWithLabel("Arrival", "", cargoTo));
        row.add(cargoDateFieldWithLabel("Travel Dates", cargoDate, dateButton));

        JButton findCargoTrips = UI.tealButton("Find Trains  →");
        findCargoTrips.setPreferredSize(new Dimension(190, 45));
        findCargoTrips.setFont(new Font("Arial", Font.BOLD, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(findCargoTrips);

        findCargoTrips.addActionListener(e -> {
            String from = cargoFrom.getSelectedItem().toString();
            String to = cargoTo.getSelectedItem().toString();

            if (from.equals(to)) {
                JOptionPane.showMessageDialog(this, "Departure and arrival stations cannot be the same.");
                return;
            }

            if (cargoDate.getText().equals("Day/Month/Year") || cargoDate.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select travel date.");
                return;
            }

            if (!((from.equals("Riyadh Station") && to.equals("Qurayyat Station")) ||
                    (from.equals("Qurayyat Station") && to.equals("Riyadh Station")))) {
                JOptionPane.showMessageDialog(this, "Car Cargo is only available between Riyadh and Qurayyat.");
                return;
            }

            new CargoTripSearchFrame(customerId, from, to, cargoDate.getText());
        });

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.add(row);
        center.add(Box.createVerticalStrut(35));
        center.add(buttonPanel);

        content.add(topLine, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);

        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    JPanel cargoFieldWithLabel(String labelText, String icon, JComponent input) {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setBackground(Color.WHITE);
        outer.setPreferredSize(new Dimension(420, 90));
        outer.setMaximumSize(new Dimension(520, 90));

        JLabel label = label(labelText);

        JPanel field = new JPanel(new BorderLayout(10, 0));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(420, 45));
        field.setMaximumSize(new Dimension(520, 45));
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(185, 185, 185)));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        iconLabel.setForeground(new Color(60, 60, 60));

        input.setFont(new Font("Arial", Font.PLAIN, 15));
        input.setBorder(null);
        input.setPreferredSize(new Dimension(360, 38));

        field.add(iconLabel, BorderLayout.WEST);
        field.add(input, BorderLayout.CENTER);

        outer.add(label, BorderLayout.NORTH);
        outer.add(field, BorderLayout.CENTER);

        return outer;
    }

    JPanel cargoDateFieldWithLabel(String labelText, JTextField fieldText, JButton button) {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setBackground(Color.WHITE);
        outer.setPreferredSize(new Dimension(420, 90));
        outer.setMaximumSize(new Dimension(520, 90));

        JLabel label = label(labelText);

        JPanel field = new JPanel(new BorderLayout(10, 0));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(420, 45));
        field.setMaximumSize(new Dimension(520, 45));
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(185, 185, 185)));

        button.setPreferredSize(new Dimension(40, 38));
        fieldText.setFont(new Font("Arial", Font.PLAIN, 15));
        fieldText.setBorder(null);
        fieldText.setPreferredSize(new Dimension(360, 38));

        field.add(button, BorderLayout.WEST);
        field.add(fieldText, BorderLayout.CENTER);

        outer.add(label, BorderLayout.NORTH);
        outer.add(field, BorderLayout.CENTER);

        return outer;
    }


    JPanel cargoInputPanel(String icon, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 190, 190)));

        JLabel i = new JLabel(icon);
        i.setFont(new Font("Arial", Font.PLAIN, 20));
        i.setForeground(new Color(70, 70, 70));

        input.setBorder(null);
        input.setFont(new Font("Arial", Font.PLAIN, 15));

        p.add(i, BorderLayout.WEST);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    JPanel dateInputPanel(JTextField field, JButton button) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 190, 190)));

        p.add(button, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

void showCalendarDialog(JTextField targetField) {
 
    JDialog dialog = new JDialog(this, "Select Date", true);
    dialog.setSize(360, 420);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
 
    final YearMonth[] ym = {YearMonth.now()};
 
    JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
    monthLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
 
    JPanel daysHeader = new JPanel(new GridLayout(1, 7));
    String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
 
    for (String d : days) {
        JLabel day = new JLabel(d, SwingConstants.CENTER);
        day.setFont(new Font("Arial", Font.BOLD, 13));
        daysHeader.add(day);
    }
 
    JPanel calendar = new JPanel(new GridLayout(6, 7));
    calendar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
 
  
    Runnable buildCalendar = () -> {
 
        calendar.removeAll();
 
        monthLabel.setText(ym[0].getMonth() + " " + ym[0].getYear());
 
        LocalDate first = ym[0].atDay(1);
        int firstDayIndex = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym[0].lengthOfMonth();
 
        for (int i = 0; i < firstDayIndex; i++) {
            calendar.add(new JLabel(""));
        }
 
        for (int day = 1; day <= daysInMonth; day++) {
 
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFocusPainted(false);
            dayBtn.setBackground(Color.WHITE);
            dayBtn.setForeground(new Color(90, 90, 90));
            dayBtn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
 
            int selectedDay = day;
 
            dayBtn.addActionListener(e ->
                    targetField.setText(
                            ym[0].getYear() + "-" +
                            String.format("%02d", ym[0].getMonthValue()) + "-" +
                            String.format("%02d", selectedDay)
                    )
            );
 
            calendar.add(dayBtn);
        }
 
        int totalCells = 6 * 7;
        int usedCells = firstDayIndex + daysInMonth;
 
        for (int i = usedCells; i < totalCells; i++) {
            calendar.add(new JLabel(""));
        }
 
        calendar.revalidate();
        calendar.repaint();
    };
 
 
    JButton prev = new JButton("<");
    JButton next = new JButton(">");
 
    prev.addActionListener(e -> {
        ym[0] = ym[0].minusMonths(1);
        buildCalendar.run();
    });
 
    next.addActionListener(e -> {
        ym[0] = ym[0].plusMonths(1);
        buildCalendar.run();
    });
 
    JPanel nav = new JPanel(new BorderLayout());
    nav.add(prev, BorderLayout.WEST);
    nav.add(monthLabel, BorderLayout.CENTER);
    nav.add(next, BorderLayout.EAST);
 
    JPanel top = new JPanel(new BorderLayout());
    top.add(nav, BorderLayout.NORTH);
    top.add(daysHeader, BorderLayout.SOUTH);
 
    JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
    buttons.setBorder(BorderFactory.createEmptyBorder(10, 18, 18, 18));
 
    JButton apply = UI.tealButton("Apply");
    JButton cancel = UI.lightButton("Cancel");
 
    cancel.addActionListener(e -> dialog.dispose());
    apply.addActionListener(e -> dialog.dispose());
 
    buttons.add(apply);
    buttons.add(cancel);
 
    dialog.add(top, BorderLayout.NORTH);
    dialog.add(calendar, BorderLayout.CENTER);
    dialog.add(buttons, BorderLayout.SOUTH);
 
    buildCalendar.run();
 
    dialog.setVisible(true);
}
void showDualCalendarDialog(JTextField departField, JTextField returnField) {

    JDialog dialog = new JDialog(this, "Select Travel Dates", true);
    dialog.setSize(900, 520);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    final YearMonth[] leftMonth = {YearMonth.now()};
    final LocalDate[] selectedDepart = {null};
    final LocalDate[] selectedReturn = {null};

    JPanel calendarsPanel = new JPanel(new GridLayout(1, 2));
    calendarsPanel.setBackground(Color.WHITE);

    JPanel leftCalendar = new JPanel(new BorderLayout());
    leftCalendar.setBackground(Color.WHITE);
    leftCalendar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

    JPanel rightCalendar = new JPanel(new BorderLayout());
    rightCalendar.setBackground(Color.WHITE);

    JLabel leftTitle = new JLabel("Outbound Date", SwingConstants.CENTER);
    leftTitle.setFont(new Font("Arial", Font.PLAIN, 18));

    JLabel rightTitle = new JLabel("Return Date", SwingConstants.CENTER);
    rightTitle.setFont(new Font("Arial", Font.PLAIN, 18));

    JLabel leftMonthLabel = new JLabel("", SwingConstants.CENTER);
    leftMonthLabel.setFont(new Font("Arial", Font.BOLD, 20));

    JLabel rightMonthLabel = new JLabel("", SwingConstants.CENTER);
    rightMonthLabel.setFont(new Font("Arial", Font.BOLD, 20));

    JPanel leftDaysHeader = daysHeaderPanel();
    JPanel rightDaysHeader = daysHeaderPanel();

    JPanel leftGrid = new JPanel(new GridLayout(6, 7, 8, 8));
    leftGrid.setBackground(Color.WHITE);
    leftGrid.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

    JPanel rightGrid = new JPanel(new GridLayout(6, 7, 8, 8));
    rightGrid.setBackground(Color.WHITE);
    rightGrid.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

    JButton prev = new JButton("<");
    JButton next = new JButton(">");

    prev.setFont(new Font("Arial", Font.BOLD, 24));
    next.setFont(new Font("Arial", Font.BOLD, 24));
    prev.setFocusPainted(false);
    next.setFocusPainted(false);
    prev.setBorderPainted(false);
    next.setBorderPainted(false);
    prev.setBackground(Color.WHITE);
    next.setBackground(Color.WHITE);

    Runnable[] refresh = new Runnable[1];

    refresh[0] = () -> {
        YearMonth rightMonth = leftMonth[0];

        leftMonthLabel.setText(leftMonth[0].getMonth() + " " + leftMonth[0].getYear());
        rightMonthLabel.setText(rightMonth.getMonth() + " " + rightMonth.getYear());

        buildRoundTripMonth(leftGrid, leftMonth[0], selectedDepart, selectedReturn, true, refresh[0]);
        buildRoundTripMonth(rightGrid, rightMonth, selectedDepart, selectedReturn, false, refresh[0]);

        leftGrid.revalidate();
        leftGrid.repaint();
        rightGrid.revalidate();
        rightGrid.repaint();
    };

    prev.addActionListener(e -> {
        leftMonth[0] = leftMonth[0].minusMonths(1);
        refresh[0].run();
    });

    next.addActionListener(e -> {
        leftMonth[0] = leftMonth[0].plusMonths(1);
        refresh[0].run();
    });

    JPanel leftTop = new JPanel(new BorderLayout());
    leftTop.setBackground(Color.WHITE);
    leftTop.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
    leftTop.add(leftTitle, BorderLayout.NORTH);
    leftTop.add(leftMonthLabel, BorderLayout.CENTER);
    leftTop.add(prev, BorderLayout.WEST);

    JPanel rightTop = new JPanel(new BorderLayout());
    rightTop.setBackground(Color.WHITE);
    rightTop.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
    rightTop.add(rightTitle, BorderLayout.NORTH);
    rightTop.add(rightMonthLabel, BorderLayout.CENTER);
    rightTop.add(next, BorderLayout.EAST);

    leftCalendar.add(leftTop, BorderLayout.NORTH);
    leftCalendar.add(leftDaysHeader, BorderLayout.CENTER);
    leftCalendar.add(leftGrid, BorderLayout.SOUTH);

    rightCalendar.add(rightTop, BorderLayout.NORTH);
    rightCalendar.add(rightDaysHeader, BorderLayout.CENTER);
    rightCalendar.add(rightGrid, BorderLayout.SOUTH);

    calendarsPanel.add(leftCalendar);
    calendarsPanel.add(rightCalendar);

    JTextField departPreview = new JTextField();
    JTextField returnPreview = new JTextField();

    departPreview.setEditable(false);
    returnPreview.setEditable(false);

    departPreview.setText(departField.getText());
    returnPreview.setText(returnField.getText());

    JPanel bottom = new JPanel(new BorderLayout());
    bottom.setBackground(Color.WHITE);
    bottom.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JPanel previews = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
    previews.setBackground(Color.WHITE);

    departPreview.setPreferredSize(new Dimension(150, 42));
    returnPreview.setPreferredSize(new Dimension(150, 42));

    JLabel dash = new JLabel("-");
    dash.setFont(new Font("Arial", Font.BOLD, 18));

    previews.add(departPreview);
    previews.add(dash);
    previews.add(returnPreview);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
    buttons.setBackground(Color.WHITE);

    JButton apply = UI.tealButton("Apply");
    JButton cancel = UI.lightButton("Cancel");

    apply.setPreferredSize(new Dimension(140, 45));
    cancel.setPreferredSize(new Dimension(140, 45));

    cancel.addActionListener(e -> dialog.dispose());

    apply.addActionListener(e -> {
        if (selectedDepart[0] == null || selectedReturn[0] == null) {
            JOptionPane.showMessageDialog(dialog, "Please select outbound and return dates.");
            return;
        }

        if (selectedReturn[0].isBefore(selectedDepart[0])) {
            JOptionPane.showMessageDialog(dialog, "Return date cannot be before outbound date.");
            return;
        }

        departField.setText(selectedDepart[0].toString());
        returnField.setText(selectedReturn[0].toString());

        dialog.dispose();
    });

    buttons.add(apply);
    buttons.add(cancel);

    bottom.add(previews, BorderLayout.WEST);
    bottom.add(buttons, BorderLayout.EAST);

    dialog.add(calendarsPanel, BorderLayout.CENTER);
    dialog.add(bottom, BorderLayout.SOUTH);

    refresh[0].run();
    dialog.setVisible(true);
}

JPanel daysHeaderPanel() {
    JPanel header = new JPanel(new GridLayout(1, 7));
    header.setBackground(Color.WHITE);
    header.setBorder(BorderFactory.createEmptyBorder(15, 25, 0, 25));

    String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

    for (String d : days) {
        JLabel day = new JLabel(d, SwingConstants.CENTER);
        day.setFont(new Font("Arial", Font.BOLD, 15));
        header.add(day);
    }

    return header;
}

void buildRoundTripMonth(JPanel panel, YearMonth ym,
                         LocalDate[] selectedDepart,
                         LocalDate[] selectedReturn,
                         boolean isDepartCalendar,
                         Runnable refresh) {

    panel.removeAll();

    LocalDate first = ym.atDay(1);
    int firstDayIndex = first.getDayOfWeek().getValue() % 7;
    int daysInMonth = ym.lengthOfMonth();

    for (int i = 0; i < firstDayIndex; i++) {
        panel.add(new JLabel(""));
    }

    for (int day = 1; day <= daysInMonth; day++) {
        LocalDate currentDate = ym.atDay(day);

        JButton dayBtn = new JButton(String.valueOf(day));
        dayBtn.setFocusPainted(false);
        dayBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        dayBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dayBtn.setBackground(Color.WHITE);
        dayBtn.setForeground(new Color(60, 60, 60));

        boolean isDepart = selectedDepart[0] != null && currentDate.equals(selectedDepart[0]);
        boolean isReturn = selectedReturn[0] != null && currentDate.equals(selectedReturn[0]);

        if (isDepart || isReturn) {
            dayBtn.setBackground(UI.teal);
            dayBtn.setForeground(Color.WHITE);
            dayBtn.setFont(new Font("Arial", Font.BOLD, 16));
        }

        dayBtn.addActionListener(e -> {
            if (isDepartCalendar) {
                selectedDepart[0] = currentDate;

                if (selectedReturn[0] != null && selectedReturn[0].isBefore(selectedDepart[0])) {
                    selectedReturn[0] = null;
                }
            } else {
                selectedReturn[0] = currentDate;
            }

            refresh.run();
        });

        panel.add(dayBtn);
    }

    int usedCells = firstDayIndex + daysInMonth;
    for (int i = usedCells; i < 42; i++) {
        panel.add(new JLabel(""));
    }
}
    void showTrainTab() {
        trainTab.setBackground(UI.teal);
        trainTab.setForeground(Color.WHITE);
        cargoTab.setBackground(new Color(235, 235, 235));
        cargoTab.setForeground(new Color(90, 90, 90));
        formCards.show(formContainer, "TRAIN");
    }

    void showCargoTab() {
        cargoTab.setBackground(UI.teal);
        cargoTab.setForeground(Color.WHITE);
        trainTab.setBackground(new Color(235, 235, 235));
        trainTab.setForeground(new Color(90, 90, 90));
        formCards.show(formContainer, "CARGO");
    }

    JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        l.setForeground(new Color(40, 40, 40));
        return l;
    }

    void loadStations() {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT station_name FROM stations");

            while (rs.next()) {
                String station = rs.getString("station_name");
                fromBox.addItem(station);
                toBox.addItem(station);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class MyBookingsFrame extends JFrame {
    DefaultTableModel ticketModel = new DefaultTableModel();
    DefaultTableModel cargoModel = new DefaultTableModel();

    JTable ticketTable = new JTable(ticketModel);
    JTable cargoTable = new JTable(cargoModel);

    int customerId;

    public MyBookingsFrame(int customerId) {
        this.customerId = customerId;

        setTitle("My Bookings");
        setSize(1150, 620);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(2, 1, 0, 14));
        main.setBackground(UI.light);
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        ticketModel.setColumnIdentifiers(new String[]{
                "Ticket ID", "Route", "Departure → Arrival Time", "Seat", "Booking Date", "Status", "Payment Amount"
        });

        cargoModel.setColumnIdentifiers(new String[]{
                "Cargo ID", "Route", "Shipping → Arrival Time", "Car Plate", "Travel Date", "Status", "Amount"
        });

        JPanel ticketSection = sectionPanel("Train Ticket Bookings", ticketTable);
        JPanel cargoSection = sectionPanel("Car Cargo Bookings", cargoTable);

        main.add(ticketSection);
        main.add(cargoSection);

        JButton cancelTicket = UI.button("Cancel Selected Train Ticket");
        JButton cancelCargo = UI.tealButton("Cancel Selected Car Cargo");

        cancelTicket.addActionListener(e -> cancelSelectedTicket());
        cancelCargo.addActionListener(e -> cancelSelectedCargo());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setBackground(UI.light);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        buttons.add(cancelTicket);
        buttons.add(cancelCargo);

        add(main, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        load();
        UI.maximizeWindow(this);
        setVisible(true);
    }

    JPanel sectionPanel(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel header = new JLabel("  " + title);
        header.setOpaque(true);
        header.setBackground(UI.teal);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setPreferredSize(new Dimension(1000, 35));

        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        p.add(header, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        return p;
    }

    void load() {
        ticketModel.setRowCount(0);
        cargoModel.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String ticketSql =
                    "SELECT tk.ticket_id, tk.seat_number, tk.booking_date, tk.ticket_status, " +
                            "IFNULL(p.amount, 0) AS amount, IFNULL(p.payment_status, 'Not Paid') AS payment_status, " +
                            "s1.station_name AS from_station, s2.station_name AS to_station, " +
                            "TIME(t.departure_time) AS dep_time, TIME(t.arrival_time) AS arr_time " +
                            "FROM tickets tk " +
                            "JOIN trips t ON tk.trip_id = t.trip_id " +
                            "JOIN stations s1 ON t.departure_station = s1.station_id " +
                            "JOIN stations s2 ON t.arrival_station = s2.station_id " +
                            "LEFT JOIN payments p ON tk.ticket_id = p.ticket_id " +
                            "WHERE tk.customer_id=? " +
                            "ORDER BY tk.ticket_id DESC";

            PreparedStatement ticketPs = con.prepareStatement(ticketSql);
            ticketPs.setInt(1, customerId);
            ResultSet ticketRs = ticketPs.executeQuery();

            while (ticketRs.next()) {
                String dep = ticketRs.getString("dep_time");
                String arr = ticketRs.getString("arr_time");

                if (dep != null && dep.length() >= 5) dep = dep.substring(0, 5);
                if (arr != null && arr.length() >= 5) arr = arr.substring(0, 5);

                ticketModel.addRow(new Object[]{
                        ticketRs.getInt("ticket_id"),
                        ticketRs.getString("from_station") + " → " + ticketRs.getString("to_station"),
                        dep + " → " + arr,
                        ticketRs.getString("seat_number"),
                        ticketRs.getString("booking_date"),
                        ticketRs.getString("ticket_status"),
                        ticketRs.getDouble("amount") + " SAR (" + ticketRs.getString("payment_status") + ")"
                });
            }

            String cargoSql =
                    "SELECT cargo_id, departure_station, arrival_station, travel_date, departure_time, arrival_time, " +
                            "car_plate, amount, status " +
                            "FROM car_cargo_bookings WHERE customer_id=? ORDER BY cargo_id DESC";

            PreparedStatement cargoPs = con.prepareStatement(cargoSql);
            cargoPs.setInt(1, customerId);
            ResultSet cargoRs = cargoPs.executeQuery();

            while (cargoRs.next()) {
                String dep = cargoRs.getString("departure_time");
                String arr = cargoRs.getString("arrival_time");

                cargoModel.addRow(new Object[]{
                        cargoRs.getInt("cargo_id"),
                        cargoRs.getString("departure_station") + " → " + cargoRs.getString("arrival_station"),
                        dep + " → " + arr,
                        cargoRs.getString("car_plate"),
                        cargoRs.getString("travel_date"),
                        cargoRs.getString("status"),
                        cargoRs.getDouble("amount") + " SAR"
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void cancelSelectedTicket() {
        int row = ticketTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a train ticket first.");
            return;
        }

        int ticketId = Integer.parseInt(ticketModel.getValueAt(row, 0).toString());

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE tickets SET ticket_status='Cancelled' WHERE ticket_id=?"
            );
            ps.setInt(1, ticketId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Train ticket cancelled successfully.");
            load();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void cancelSelectedCargo() {
        int row = cargoTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a car cargo booking first.");
            return;
        }

        int cargoId = Integer.parseInt(cargoModel.getValueAt(row, 0).toString());

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE car_cargo_bookings SET status='Cancelled' WHERE cargo_id=?"
            );
            ps.setInt(1, cargoId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Car cargo booking cancelled successfully.");
            load();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
