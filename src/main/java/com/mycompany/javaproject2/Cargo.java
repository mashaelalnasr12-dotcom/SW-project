package com.mycompany.javaproject2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;

class CargoTripSearchFrame extends JFrame {
    int customerId;
    String fromStation;
    String toStation;
    String travelDate;

    JPanel tripsPanel = new JPanel();

    JComboBox<String> tripTimeFilter = new JComboBox<>(new String[]{
            "All Times",
            "Early Morning 00:00 - 06:00",
            "Morning 06:00 - 12:00",
            "Afternoon 12:00 - 18:00",
            "Evening 18:00 - 00:00"
    });

    JComboBox<String> sortFilter = new JComboBox<>(new String[]{
            "Nearest Time",
            "Lowest Price"
    });

    public CargoTripSearchFrame(int customerId, String fromStation, String toStation, String travelDate) {
        this.customerId = customerId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.travelDate = travelDate;

        setTitle("Select Car Cargo Trip");
        setSize(1250, 760);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UI.light);

        JPanel routeBanner = new JPanel(new BorderLayout());
        routeBanner.setPreferredSize(new Dimension(1250, 125));
        routeBanner.setBackground(UI.tealDark);

        JLabel routeTitle = new JLabel("  " + shortName(fromStation) + " to " + shortName(toStation));
        routeTitle.setFont(new Font("Arial", Font.BOLD, 34));
        routeTitle.setForeground(Color.WHITE);
        routeBanner.add(routeTitle, BorderLayout.WEST);

        JLabel routeImage = UI.imageBox("2.png", 260, 100);
        routeImage.setPreferredSize(new Dimension(260, 100));
        routeBanner.add(routeImage, BorderLayout.EAST);

        JPanel summary = new JPanel(new GridLayout(1, 5, 10, 0));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        summary.add(summaryItem("From", shortName(fromStation)));
        summary.add(summaryArrow());
        summary.add(summaryItem("To", shortName(toStation)));
        summary.add(summaryItem("Travel Date", travelDate));
        summary.add(summaryItem("Service", "Car Cargo"));

        JPanel filters = new JPanel(new BorderLayout());
        filters.setBackground(Color.WHITE);
        filters.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilter.setBackground(Color.WHITE);
        JLabel filterTitle = new JLabel("Filter Trips");
        filterTitle.setFont(new Font("Arial", Font.BOLD, 15));
        leftFilter.add(filterTitle);
        leftFilter.add(new JLabel("Trip Times"));
        tripTimeFilter.setPreferredSize(new Dimension(235, 32));
        leftFilter.add(tripTimeFilter);

        JPanel rightFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightFilter.setBackground(Color.WHITE);
        JLabel sortTitle = new JLabel("Filter By");
        sortTitle.setFont(new Font("Arial", Font.BOLD, 15));
        rightFilter.add(sortTitle);
        sortFilter.setPreferredSize(new Dimension(160, 32));
        rightFilter.add(sortFilter);

        filters.add(leftFilter, BorderLayout.WEST);
        filters.add(rightFilter, BorderLayout.EAST);

        tripTimeFilter.addActionListener(e -> loadCargoTrips());
        sortFilter.addActionListener(e -> loadCargoTrips());

        JPanel top = new JPanel(new BorderLayout());
        top.add(routeBanner, BorderLayout.NORTH);
        top.add(summary, BorderLayout.CENTER);
        top.add(filters, BorderLayout.SOUTH);

        JLabel title = new JLabel("  Select Car Cargo Trip");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(35, 35, 35));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        tripsPanel.setLayout(new BoxLayout(tripsPanel, BoxLayout.Y_AXIS));
        tripsPanel.setBackground(UI.light);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UI.light);
        center.add(title, BorderLayout.NORTH);
        center.add(new JScrollPane(tripsPanel), BorderLayout.CENTER);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        add(main);
        loadCargoTrips();
        UI.maximizeWindow(this);
        setVisible(true);
    }

    JLabel summaryItem(String label, String value) {
        JLabel l = new JLabel("<html><span style='color:gray;'>" + label + "</span><br><b>" + value + "</b></html>");
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        return l;
    }

    JLabel summaryArrow() {
        JLabel l = new JLabel("→", SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 26));
        l.setForeground(UI.teal);
        return l;
    }

    String shortName(String station) {
        return station.replace(" Station", "");
    }

    void loadCargoTrips() {
        tripsPanel.removeAll();

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String timeCondition = "";
            String selectedTime = tripTimeFilter.getSelectedItem().toString();

            if (selectedTime.startsWith("Early")) {
                timeCondition = " AND HOUR(t.departure_time) >= 0 AND HOUR(t.departure_time) < 6 ";
            } else if (selectedTime.startsWith("Morning")) {
                timeCondition = " AND HOUR(t.departure_time) >= 6 AND HOUR(t.departure_time) < 12 ";
            } else if (selectedTime.startsWith("Afternoon")) {
                timeCondition = " AND HOUR(t.departure_time) >= 12 AND HOUR(t.departure_time) < 18 ";
            } else if (selectedTime.startsWith("Evening")) {
                timeCondition = " AND HOUR(t.departure_time) >= 18 AND HOUR(t.departure_time) < 24 ";
            }

            String orderBy = sortFilter.getSelectedItem().toString().equals("Lowest Price")
                    ? " ORDER BY cargo_price ASC "
                    : " ORDER BY t.departure_time ASC ";

            String sql =
                    "SELECT t.trip_id, tr.train_name, TIME(t.departure_time) AS dep_time, TIME(t.arrival_time) AS arr_time, " +
                            "(t.price + 315) AS cargo_price " +
                            "FROM trips t " +
                            "JOIN trains tr ON t.train_id = tr.train_id " +
                            "JOIN stations s1 ON t.departure_station = s1.station_id " +
                            "JOIN stations s2 ON t.arrival_station = s2.station_id " +
                            "WHERE s1.station_name=? AND s2.station_name=? AND DATE(t.departure_time)=? " +
                            timeCondition +
                            orderBy;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, fromStation);
            ps.setString(2, toStation);
            ps.setString(3, travelDate);

            ResultSet rs = ps.executeQuery();

            int count = 0;

            while (rs.next()) {
                count++;

                int tripId = rs.getInt("trip_id");
                String trainName = rs.getString("train_name");
                String depTime = rs.getString("dep_time").substring(0, 5);
                String arrTime = rs.getString("arr_time").substring(0, 5);
                double price = rs.getDouble("cargo_price");

                tripsPanel.add(cargoTripCard(tripId, trainName, depTime, arrTime, price));
                tripsPanel.add(Box.createVerticalStrut(14));
            }

            if (count == 0) {
                JLabel noTrips = new JLabel("No vehicle slots are available on the selected trip. Please try another date.", SwingConstants.CENTER);
                noTrips.setFont(new Font("Arial", Font.BOLD, 20));
                noTrips.setBorder(BorderFactory.createEmptyBorder(80, 0, 80, 0));
                tripsPanel.add(noTrips);
            }

            tripsPanel.revalidate();
            tripsPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    JPanel cargoTripCard(int tripId, String trainName, String depTime, String arrTime, double price) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(1150, 135));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JPanel left = new JPanel(new GridLayout(1, 5, 14, 0));
        left.setBackground(Color.WHITE);

        left.add(bigTime(depTime, shortName(fromStation)));

        JLabel arrow = new JLabel("→", SwingConstants.CENTER);
        arrow.setFont(new Font("Arial", Font.BOLD, 28));
        arrow.setForeground(UI.teal);
        left.add(arrow);

        left.add(bigTime(arrTime, shortName(toStation)));

        JLabel train = new JLabel("<html><b>Vehicle Slot</b><br>" + trainName + "</html>");
        train.setFont(new Font("Arial", Font.BOLD, 16));
        train.setForeground(new Color(35, 35, 35));
        left.add(train);

        JLabel duration = new JLabel("<html><b>10 H 33 M</b><br>Car Cargo</html>");
        duration.setFont(new Font("Arial", Font.BOLD, 15));
        left.add(duration);

        JButton select = UI.tealButton("<html><center>Select<br>﷼ " + price + "</center></html>");
        select.setPreferredSize(new Dimension(220, 85));
        select.setFont(new Font("Arial", Font.BOLD, 16));
        select.addActionListener(e -> {
            new CargoBookingDetailsFrame(customerId, tripId, fromStation, toStation, travelDate, depTime, arrTime, price);
            dispose();
        });

        card.add(left, BorderLayout.CENTER);
        card.add(select, BorderLayout.EAST);

        return card;
    }

    JLabel bigTime(String time, String station) {
        JLabel l = new JLabel("<html><span style='font-size:28px;'><b>" + time + "</b></span><br>" + station + "</html>");
        l.setFont(new Font("Arial", Font.PLAIN, 15));
        l.setForeground(UI.teal);
        return l;
    }
}


class CargoBookingDetailsFrame extends JFrame {
    int customerId;
    int tripId;
    String fromStation;
    String toStation;
    String travelDate;
    String depTime;
    String arrTime;
    double amount;

    JTextField plate = new JTextField();
    JTextField owner = new JTextField();
    JTextField brand = new JTextField();
    JTextField model = new JTextField();

    public CargoBookingDetailsFrame(int customerId, int tripId, String fromStation, String toStation,
                                    String travelDate, String depTime, String arrTime, double amount) {
        this.customerId = customerId;
        this.tripId = tripId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.travelDate = travelDate;
        this.depTime = depTime;
        this.arrTime = arrTime;
        this.amount = amount;

        setTitle("Car Cargo Details");
        setSize(650, 520);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UI.light);

        JLabel header = new JLabel("  Enter Vehicle Information");
        header.setOpaque(true);
        header.setBackground(UI.teal);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setPreferredSize(new Dimension(650, 60));

        JPanel summary = new JPanel(new GridLayout(5, 1, 6, 6));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));
        summary.add(new JLabel("Route: " + fromStation + " → " + toStation));
        summary.add(new JLabel("Travel Date: " + travelDate));
        summary.add(new JLabel("Shipping Time: " + depTime));
        summary.add(new JLabel("Arrival Time: " + arrTime));
        summary.add(new JLabel("Amount: " + amount + " SAR"));

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 12));
        form.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        form.setBackground(UI.light);
        form.add(new JLabel("Car Plate Number:"));
        form.add(plate);
        form.add(new JLabel("Owner Name:"));
        form.add(owner);
        form.add(new JLabel("Car Brand:"));
        form.add(brand);
        form.add(new JLabel("Car Model:"));
        form.add(model);

        JButton book = UI.tealButton("Confirm Car Cargo Booking");
        book.addActionListener(e -> bookCargo());

        main.add(header, BorderLayout.NORTH);
        main.add(summary, BorderLayout.CENTER);
        main.add(form, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
        add(book, BorderLayout.SOUTH);
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void bookCargo() {
        if (plate.getText().trim().isEmpty() || owner.getText().trim().isEmpty()
                || brand.getText().trim().isEmpty() || model.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all vehicle details.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql = "INSERT INTO car_cargo_bookings(customer_id, departure_station, arrival_station, travel_date, departure_time, arrival_time, car_plate, car_brand, car_model, owner_name, amount, status) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,'Booked')";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, customerId);
            ps.setString(2, fromStation);
            ps.setString(3, toStation);
            ps.setString(4, travelDate);
            ps.setString(5, depTime);
            ps.setString(6, arrTime);
            ps.setString(7, plate.getText().trim());
            ps.setString(8, brand.getText().trim());
            ps.setString(9, model.getText().trim());
            ps.setString(10, owner.getText().trim());
            ps.setDouble(11, amount);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int cargoId = keys.getInt(1);

            JOptionPane.showMessageDialog(this,
                    "Car Cargo booked successfully!\n" +
                            "Cargo Booking ID: " + cargoId + "\n" +
                            "Route: " + fromStation + " → " + toStation + "\n" +
                            "Date: " + travelDate + "\n" +
                            "Car Plate: " + plate.getText().trim() + "\n" +
                            "Amount: " + amount + " SAR");

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class CargoBookingsFrame extends JFrame {
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);

    public CargoBookingsFrame() {
        setTitle("Car Cargo Bookings");
        setSize(1000, 500);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        model.setColumnIdentifiers(new String[]{
                "Cargo ID", "Customer ID", "From", "To", "Date", "Shipping Time", "Arrival Time", "Plate", "Brand", "Model", "Owner", "Amount", "Status"
        });

        JButton markShipped = UI.tealButton("Mark as Shipped");
        JButton markArrived = UI.button("Mark as Arrived");
        JButton cancel = UI.button("Cancel Cargo Booking");
        JButton refresh = UI.button("Refresh");

        JPanel buttons = new JPanel(new GridLayout(1, 4, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttons.add(markShipped);
        buttons.add(markArrived);
        buttons.add(cancel);
        buttons.add(refresh);

        markShipped.addActionListener(e -> updateStatus("Shipped"));
        markArrived.addActionListener(e -> updateStatus("Arrived"));
        cancel.addActionListener(e -> updateStatus("Cancelled"));
        refresh.addActionListener(e -> load());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        load();
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void load() {
        model.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT cargo_id, customer_id, departure_station, arrival_station, travel_date, " +
                            "departure_time, arrival_time, car_plate, car_brand, car_model, owner_name, amount, status FROM car_cargo_bookings ORDER BY cargo_id DESC"
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("cargo_id"),
                        rs.getInt("customer_id"),
                        rs.getString("departure_station"),
                        rs.getString("arrival_station"),
                        rs.getString("travel_date"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getString("car_plate"),
                        rs.getString("car_brand"),
                        rs.getString("car_model"),
                        rs.getString("owner_name"),
                        rs.getDouble("amount"),
                        rs.getString("status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void updateStatus(String status) {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a cargo booking first.");
            return;
        }

        int cargoId = Integer.parseInt(model.getValueAt(row, 0).toString());

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement("UPDATE car_cargo_bookings SET status=? WHERE cargo_id=?");
            ps.setString(1, status);
            ps.setInt(2, cargoId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cargo status updated to: " + status);
            load();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
