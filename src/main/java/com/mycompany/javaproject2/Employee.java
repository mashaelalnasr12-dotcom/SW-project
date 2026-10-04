package com.mycompany.javaproject2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;

 class EmployeeLoginFrame extends JFrame {
    JTextField username = new JTextField();
    JPasswordField password = new JPasswordField();

    public EmployeeLoginFrame() {
        setTitle("Employee Login");
        setSize(400, 280);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        p.setBackground(UI.light);

        p.add(new JLabel("Username:")); p.add(username);
        p.add(new JLabel("Password:")); p.add(password);

        JButton login = UI.tealButton("Login");
        p.add(new JLabel(""));
        p.add(login);

        login.addActionListener(e -> login());

        add(p);
        setVisible(true);
    }

    void login() {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql = "SELECT * FROM employees WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, username.getText());
            ps.setString(2, new String(password.getPassword()));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String usernameValue = rs.getString("username");
                String roleValue = "Staff";
                try {
                    roleValue = rs.getString("role");
                } catch (Exception ignored) {}

                new EmployeeDashboardFrame(usernameValue, roleValue);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid employee account.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class EmployeeDashboardFrame extends JFrame {
    JPanel center = new JPanel(new GridLayout(2, 3, 18, 18));
    String username;
    String role;
    boolean isAdmin;

    public EmployeeDashboardFrame(String username, String role) {
        this.username = username;
        this.role = role == null ? "Staff" : role;
        this.isAdmin = this.role.equalsIgnoreCase("Admin");

        setTitle("Employee Dashboard");
        setSize(1080, 680);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        JPanel main = new JPanel(new BorderLayout());

        JLabel header = new JLabel("  Employee Dashboard - " + this.role + " | " + username);
        header.setOpaque(true);
        header.setBackground(UI.navy);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setPreferredSize(new Dimension(1080, 60));

        JPanel side = new JPanel(new GridLayout(isAdmin ? 12 : 11, 1, 10, 10));
        side.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(270, 680));

        JButton refresh = UI.tealButton("Refresh Dashboard");
        JButton assistBooking = UI.tealButton("Assist Customer Booking");
        JButton findBooking = UI.button("Find / Manage Booking");
        JButton trips = UI.button("View Trips");
        JButton seatMap = UI.button("Seat Map");
        JButton bookings = UI.button("All Bookings");
        JButton payments = UI.button("Payments");
        JButton passengers = UI.button("Passengers");
        JButton cargo = UI.button("Car Cargo Bookings");
        JButton reports = UI.button("Reports");
        JButton employees = UI.button("Manage Employees");
        JButton logout = UI.button("Logout");

        side.add(refresh);
        side.add(assistBooking);
        side.add(findBooking);
        side.add(trips);
        side.add(seatMap);
        side.add(bookings);
        side.add(payments);
        side.add(passengers);
        side.add(cargo);
        side.add(reports);

        if (isAdmin) {
            side.add(employees);
        }

        side.add(logout);

        center.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));
        center.setBackground(UI.light);
        refreshDashboard();

        refresh.addActionListener(e -> refreshDashboard());
        assistBooking.addActionListener(e -> new CounterBookingFrame());
        findBooking.addActionListener(e -> new EmployeeFindBookingFrame());
        trips.addActionListener(e -> new ViewTripsFrame(true, 0));
        seatMap.addActionListener(e -> new SeatMapFrame());
        bookings.addActionListener(e -> new AllBookingsFrame());
        payments.addActionListener(e -> new PaymentsFrame());
        passengers.addActionListener(e -> new PassengersFrame());
        cargo.addActionListener(e -> new CargoBookingsFrame());
        reports.addActionListener(e -> new ReportsFrame());

        employees.addActionListener(e -> {
            if (isAdmin) {
                new EmployeeManagementFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Only admin can manage employees.");
            }
        });

        logout.addActionListener(e -> {
            new RoleFrame();
            dispose();
        });

        main.add(header, BorderLayout.NORTH);
        main.add(side, BorderLayout.WEST);
        main.add(center, BorderLayout.CENTER);

        add(main);
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void refreshDashboard() {
        center.removeAll();
        center.add(statCard("Booked Tickets", getDashboardValue("SELECT COUNT(*) FROM tickets WHERE ticket_status='Booked'")));
        center.add(statCard("Available Trips", getDashboardValue("SELECT COUNT(*) FROM trips WHERE status='Available'")));
        center.add(statCard("Customers", getDashboardValue("SELECT COUNT(*) FROM customers")));
        center.add(statCard("Payments", getDashboardValue("SELECT COUNT(*) FROM payments")));
        center.add(statCard("Revenue", getDashboardValue("SELECT IFNULL(SUM(amount),0) FROM payments WHERE payment_status='Paid'") + " SAR"));
        center.add(statCard("Car Cargo", getDashboardValue("SELECT COUNT(*) FROM car_cargo_bookings")));
        center.revalidate();
        center.repaint();
    }

    JPanel statCard(String title, String value) {
        JPanel p = UI.whiteCard();
        p.setLayout(new BorderLayout());

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(new Font("Arial", Font.BOLD, 18));
        t.setForeground(UI.teal);

        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(new Font("Arial", Font.BOLD, 28));
        v.setForeground(UI.navy);

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    String getDashboardValue(String sql) {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return "0";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                return rs.getString(1);
            }

        } catch (Exception e) {
            return "0";
        }

        return "0";
    }
}


class EmployeeFindBookingFrame extends JFrame {
    JTextField searchField = new JTextField();
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);

    public EmployeeFindBookingFrame() {
        setTitle("Find / Manage Booking");
        setSize(950, 500);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        top.setBackground(UI.light);

        JButton search = UI.tealButton("Search");
        JButton cancel = UI.button("Cancel Selected Ticket");

        top.add(new JLabel("Ticket ID / Customer Name / Email:"), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(search, BorderLayout.EAST);

        model.setColumnIdentifiers(new String[]{
                "Ticket ID", "Trip ID", "Customer", "Email", "Route", "Departure → Arrival", "Seat", "Date", "Ticket Status", "Payment"
        });

        search.addActionListener(e -> load());
        cancel.addActionListener(e -> cancelSelected());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(cancel, BorderLayout.SOUTH);
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void load() {
        model.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String q = "%" + searchField.getText() + "%";

            String sql =
                    "SELECT tk.ticket_id, tk.trip_id, c.full_name, c.email, tk.seat_number, tk.booking_date, tk.ticket_status, p.payment_status, " +
                    "s1.station_name AS from_station, s2.station_name AS to_station, TIME(t.departure_time) AS dep_time, TIME(t.arrival_time) AS arr_time " +
                    "FROM tickets tk JOIN customers c ON tk.customer_id=c.customer_id " +
                    "JOIN trips t ON tk.trip_id=t.trip_id " +
                    "JOIN stations s1 ON t.departure_station=s1.station_id " +
                    "JOIN stations s2 ON t.arrival_station=s2.station_id " +
                    "LEFT JOIN payments p ON tk.ticket_id=p.ticket_id " +
                    "WHERE CAST(tk.ticket_id AS CHAR) LIKE ? OR c.full_name LIKE ? OR c.email LIKE ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String dep = rs.getString("dep_time");
                String arr = rs.getString("arr_time");
                if (dep != null && dep.length() >= 5) dep = dep.substring(0, 5);
                if (arr != null && arr.length() >= 5) arr = arr.substring(0, 5);

                model.addRow(new Object[]{
                        rs.getInt("ticket_id"),
                        rs.getInt("trip_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("from_station") + " → " + rs.getString("to_station"),
                        dep + " → " + arr,
                        rs.getString("seat_number"),
                        rs.getString("booking_date"),
                        rs.getString("ticket_status"),
                        rs.getString("payment_status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void cancelSelected() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a ticket first.");
            return;
        }

        int ticketId = Integer.parseInt(model.getValueAt(row, 0).toString());

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement("UPDATE tickets SET ticket_status='Cancelled' WHERE ticket_id=?");
            ps.setInt(1, ticketId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Ticket cancelled successfully.");
            load();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class EmployeeManagementFrame extends JFrame {
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);

    JTextField fullName = new JTextField();
    JTextField username = new JTextField();
    JPasswordField password = new JPasswordField();
    JComboBox<String> role = new JComboBox<>(new String[]{"Staff", "Admin"});

    public EmployeeManagementFrame() {
        setTitle("Manage Employees - Admin Only");
        setSize(950, 560);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UI.light);

        JLabel header = new JLabel("  Manage Employees");
        header.setOpaque(true);
        header.setBackground(UI.navy);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setPreferredSize(new Dimension(950, 55));

        model.setColumnIdentifiers(new String[]{"ID", "Full Name", "Username", "Password", "Role"});

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setBackground(UI.light);

        form.add(new JLabel("Full Name:"));
        form.add(fullName);
        form.add(new JLabel("Username:"));
        form.add(username);
        form.add(new JLabel("Password:"));
        form.add(password);
        form.add(new JLabel("Role:"));
        form.add(role);

        JPanel buttons = new JPanel(new GridLayout(1, 4, 10, 10));
        buttons.setBackground(UI.light);

        JButton add = UI.tealButton("Add");
        JButton update = UI.button("Update");
        JButton delete = UI.button("Delete");
        JButton refresh = UI.button("Refresh");

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);

        form.add(new JLabel(""));
        form.add(buttons);

        add.addActionListener(e -> addEmployee());
        update.addActionListener(e -> updateEmployee());
        delete.addActionListener(e -> deleteEmployee());
        refresh.addActionListener(e -> loadEmployees());

        table.getSelectionModel().addListSelectionListener(e -> fillFields());

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.WEST);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        add(main);
        loadEmployees();
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void loadEmployees() {
        model.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT employee_id, full_name, username, password, role FROM employees ORDER BY employee_id");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("employee_id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void fillFields() {
        int row = table.getSelectedRow();

        if (row == -1) return;

        fullName.setText(String.valueOf(model.getValueAt(row, 1)));
        username.setText(String.valueOf(model.getValueAt(row, 2)));
        password.setText(String.valueOf(model.getValueAt(row, 3)));
        role.setSelectedItem(String.valueOf(model.getValueAt(row, 4)));
    }

    void addEmployee() {
        try {
            if (fullName.getText().trim().isEmpty() || username.getText().trim().isEmpty() || new String(password.getPassword()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement("INSERT INTO employees(full_name, username, password, role) VALUES(?,?,?,?)");
            ps.setString(1, fullName.getText().trim());
            ps.setString(2, username.getText().trim());
            ps.setString(3, new String(password.getPassword()).trim());
            ps.setString(4, role.getSelectedItem().toString());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee added successfully.");
            loadEmployees();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void updateEmployee() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        try {
            if (id == 1 && !role.getSelectedItem().toString().equalsIgnoreCase("Admin")) {
                JOptionPane.showMessageDialog(this, "Main admin must stay Admin.");
                return;
            }

            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE employees SET full_name=?, username=?, password=?, role=? WHERE employee_id=?"
            );
            ps.setString(1, fullName.getText().trim());
            ps.setString(2, username.getText().trim());
            ps.setString(3, new String(password.getPassword()).trim());
            ps.setString(4, role.getSelectedItem().toString());
            ps.setInt(5, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee updated successfully.");
            loadEmployees();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void deleteEmployee() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String user = model.getValueAt(row, 2).toString();

        if (user.equalsIgnoreCase("admin") || id == 1) {
            JOptionPane.showMessageDialog(this, "You cannot delete the main admin account.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected employee?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement("DELETE FROM employees WHERE employee_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
            loadEmployees();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


 class CounterBookingFrame extends JFrame {
    JTextField email = new JTextField();
    JLabel customerInfo = new JLabel("Search customer by email.");
    int selectedCustomerId = -1;

    JComboBox<String> fromBox = new JComboBox<>();
    JComboBox<String> toBox = new JComboBox<>();
    JTextField dateField = new JTextField(LocalDate.now().toString());
    JSpinner adultSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
    JSpinner childSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
    JSpinner infantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));

    public CounterBookingFrame() {
        setTitle("Assist Customer Booking");
        setSize(780, 560);
        setLocationRelativeTo(null);
JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UI.light);

        JLabel header = new JLabel("  Assist Customer Booking");
        header.setOpaque(true);
        header.setBackground(UI.teal);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setPreferredSize(new Dimension(700, 55));

        JPanel p = new JPanel(new GridLayout(8, 2, 12, 12));
        p.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        p.setBackground(UI.light);

        JButton search = UI.button("Search Customer");
        JButton create = UI.lightButton("Create New Customer");
        JButton findTrips = UI.tealButton("Find Trips for Customer");

        loadStations();

        p.add(new JLabel("Customer Email:"));
        p.add(email);
        p.add(search);
        p.add(create);
        p.add(new JLabel("Customer:"));
        p.add(customerInfo);

        p.add(new JLabel("Departure:"));
        p.add(fromBox);
        p.add(new JLabel("Arrival:"));
        p.add(toBox);
        p.add(new JLabel("Travel Date:"));
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setBackground(UI.light);
        dateField.setEditable(false);
        JButton dateBtn = UI.lightButton("");
        dateBtn.setPreferredSize(new Dimension(55, 32));
        dateBtn.addActionListener(e -> showEmployeeCalendar(dateField));
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(dateBtn, BorderLayout.EAST);
        p.add(datePanel);

        p.add(new JLabel("Passengers:"));
        JPanel spinners = new JPanel(new GridLayout(3, 2, 6, 6));
        spinners.add(new JLabel("Adult:"));
        spinners.add(adultSpinner);
        spinners.add(new JLabel("Child:"));
        spinners.add(childSpinner);
        spinners.add(new JLabel("Infant:"));
        spinners.add(infantSpinner);
        p.add(spinners);

        p.add(new JLabel(""));
        p.add(findTrips);

        search.addActionListener(e -> searchCustomer());
        create.addActionListener(e -> new CustomerSignupFrame());
        findTrips.addActionListener(e -> openTrips());

        main.add(header, BorderLayout.NORTH);
        main.add(p, BorderLayout.CENTER);

        add(main);
        setVisible(true);
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

    void searchCustomer() {
        try {
            if (!email.getText().trim().toLowerCase().endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Customer email must be Gmail, for example: name@gmail.com");
                return;
            }

            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement("SELECT customer_id, full_name, phone FROM customers WHERE email=?");
            ps.setString(1, email.getText());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                selectedCustomerId = rs.getInt("customer_id");
                customerInfo.setText(rs.getString("full_name") + " | " + rs.getString("phone"));
            } else {
                selectedCustomerId = -1;
                customerInfo.setText("Customer not found.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void openTrips() {
        if (selectedCustomerId == -1) {
            JOptionPane.showMessageDialog(this, "Search and select a customer first.");
            return;
        }

        String from = fromBox.getSelectedItem().toString();
        String to = toBox.getSelectedItem().toString();

        if (from.equals(to)) {
            JOptionPane.showMessageDialog(this, "Departure and arrival stations cannot be the same.");
            return;
        }

        try {
            LocalDate selectedDate = LocalDate.parse(dateField.getText());
            if (selectedDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Travel date must be today or later.");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please select a valid travel date.");
            return;
        }

        int adult = (Integer) adultSpinner.getValue();
        int child = (Integer) childSpinner.getValue();
        int infant = (Integer) infantSpinner.getValue();

        new SearchTripsFrame(selectedCustomerId, from, to, dateField.getText(), "", adult, child, infant, "One Way");
    }

    void showEmployeeCalendar(JTextField targetField) {
        JDialog dialog = new JDialog(this, "Select Travel Date", true);
        dialog.setSize(360, 390);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.from(today);

        JLabel month = new JLabel(ym.getMonth().toString() + " " + ym.getYear(), SwingConstants.CENTER);
        month.setFont(new Font("Arial", Font.BOLD, 16));
        month.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JPanel daysHeader = new JPanel(new GridLayout(1, 7));
        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String d : days) {
            JLabel day = new JLabel(d, SwingConstants.CENTER);
            day.setFont(new Font("Arial", Font.BOLD, 13));
            daysHeader.add(day);
        }

        JPanel calendar = new JPanel(new GridLayout(6, 7, 6, 6));
        calendar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        calendar.setBackground(Color.WHITE);

        LocalDate first = ym.atDay(1);
        int firstDayIndex = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = ym.lengthOfMonth();

        for (int i = 0; i < firstDayIndex; i++) {
            calendar.add(new JLabel(""));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = ym.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFocusPainted(false);
            dayBtn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            if (current.isBefore(today)) {
                dayBtn.setEnabled(false);
                dayBtn.setBackground(new Color(235, 235, 235));
                dayBtn.setForeground(Color.GRAY);
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(new Color(70, 70, 70));

                if (current.equals(today)) {
                    dayBtn.setBackground(UI.teal);
                    dayBtn.setForeground(Color.WHITE);
                }

                dayBtn.addActionListener(e -> {
                    targetField.setText(current.toString());
                    dialog.dispose();
                });
            }

            calendar.add(dayBtn);
        }

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(10, 18, 18, 18));
        footer.setBackground(Color.WHITE);

        JLabel todayLabel = new JLabel("Today: " + today, SwingConstants.CENTER);
        todayLabel.setFont(new Font("Arial", Font.BOLD, 14));
        todayLabel.setForeground(UI.tealDark);

        JButton cancel = UI.lightButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        footer.add(todayLabel, BorderLayout.CENTER);
        footer.add(cancel, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(month, BorderLayout.NORTH);
        top.add(daysHeader, BorderLayout.SOUTH);

        dialog.add(top, BorderLayout.NORTH);
        dialog.add(calendar, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

}


class PassengersFrame extends JFrame {
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);

    public PassengersFrame() {
        setTitle("Passengers");
        setSize(850, 450);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        model.setColumnIdentifiers(new String[]{"ID", "Name", "National ID", "Phone", "Email"});

        load();

        add(new JScrollPane(table));
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void load() {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT customer_id, full_name, national_id, phone, email FROM customers");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("national_id"),
                        rs.getString("phone"),
                        rs.getString("email")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class AllBookingsFrame extends JFrame {
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);

    public AllBookingsFrame() {
        setTitle("All Bookings");
        setSize(900, 450);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        model.setColumnIdentifiers(new String[]{
                "Ticket ID", "Trip ID", "Customer", "Route", "Departure → Arrival", "Seat", "Date", "Ticket Status", "Payment"
        });

        load();

        add(new JScrollPane(table));
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void load() {
        model.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql =
                    "SELECT tk.ticket_id, tk.trip_id, c.full_name, tk.seat_number, tk.booking_date, tk.ticket_status, p.payment_status, " +
                    "s1.station_name AS from_station, s2.station_name AS to_station, TIME(t.departure_time) AS dep_time, TIME(t.arrival_time) AS arr_time " +
                    "FROM tickets tk JOIN customers c ON tk.customer_id=c.customer_id " +
                    "JOIN trips t ON tk.trip_id=t.trip_id " +
                    "JOIN stations s1 ON t.departure_station=s1.station_id " +
                    "JOIN stations s2 ON t.arrival_station=s2.station_id " +
                    "LEFT JOIN payments p ON tk.ticket_id=p.ticket_id";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                String dep = rs.getString("dep_time");
                String arr = rs.getString("arr_time");
                if (dep != null && dep.length() >= 5) dep = dep.substring(0, 5);
                if (arr != null && arr.length() >= 5) arr = arr.substring(0, 5);

                model.addRow(new Object[]{
                        rs.getInt("ticket_id"),
                        rs.getInt("trip_id"),
                        rs.getString("full_name"),
                        rs.getString("from_station") + " → " + rs.getString("to_station"),
                        dep + " → " + arr,
                        rs.getString("seat_number"),
                        rs.getString("booking_date"),
                        rs.getString("ticket_status"),
                        rs.getString("payment_status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class ReportsFrame extends JFrame {
    public ReportsFrame() {
        setTitle("Reports");
        setSize(500, 350);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(4, 1, 15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        p.setBackground(UI.light);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            int bookings = getInt(con, "SELECT COUNT(*) FROM tickets WHERE ticket_status='Booked'");
            double revenue = getDouble(con, "SELECT IFNULL(SUM(amount),0) FROM payments WHERE payment_status='Paid'");
            int customers = getInt(con, "SELECT COUNT(*) FROM customers");
            int trips = getInt(con, "SELECT COUNT(*) FROM trips");

            p.add(reportCard("Total Bookings: " + bookings));
            p.add(reportCard("Total Revenue: " + revenue + " SAR"));
            p.add(reportCard("Customers: " + customers));
            p.add(reportCard("Trips: " + trips));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        add(p);
        UI.maximizeWindow(this);
        setVisible(true);
    }

    JLabel reportCard(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(Color.WHITE);
        l.setForeground(UI.navy);
        l.setFont(new Font("Arial", Font.BOLD, 20));
        return l;
    }

    int getInt(Connection con, String sql) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        return rs.getInt(1);
    }

    double getDouble(Connection con, String sql) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        rs.next();
        return rs.getDouble(1);
    }
}
