package com.mycompany.javaproject2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.YearMonth;

class SearchTripsFrame extends JFrame {
    int customerId;
    String fromStation;
    String toStation;
    String travelDate;
    String returnDate;
    int selectedReturnTripId = -1;
    int selectedOutboundTripId = -1;
    String selectedFareType = "";
    double selectedFarePrice = 0;
    ArrayList<String> outboundSeats = new ArrayList<>();
    boolean returnMode = false;
    int adults;
    int children;
    int infants;
    int passengerCount;
    String tripType;
    int tripMultiplier;

    JPanel tripsPanel = new JPanel();
    JPanel returnTripsPanel = new JPanel();
    JLabel pageTitle;

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

    public SearchTripsFrame(int customerId, String fromStation, String toStation, String travelDate, String returnDate,
                            int adults, int children, int infants, String tripType) {
        this.customerId = customerId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.travelDate = travelDate;
        this.returnDate = returnDate;
        this.adults = adults;
        this.children = children;
        this.infants = infants;
        this.passengerCount = adults + children + infants;
        this.tripType = tripType;
        this.tripMultiplier = tripType.equalsIgnoreCase("Round Trip") ? 2 : 1;

        setTitle("Select Trip");
        setSize(1250, 760);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

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

        JPanel summary = new JPanel(new GridLayout(1, 7, 10, 0));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        summary.add(summaryItem("From", shortName(fromStation)));
        summary.add(summaryArrow());
        summary.add(summaryItem("To", shortName(toStation)));
        summary.add(summaryItem("Travel Date", travelDate));
        summary.add(summaryItem("Return Date", tripType.equalsIgnoreCase("Round Trip") ? returnDate : "-"));
        summary.add(summaryItem("Passengers", passengerCount + " Passengers"));
        summary.add(summaryItem("Trip Type", tripType));

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

        tripTimeFilter.addActionListener(e -> {
            if (selectedOutboundTripId == -1) {
                loadTrips();
            } else {
                loadReturnTrips();
            }
        });
        sortFilter.addActionListener(e -> {
            if (selectedOutboundTripId == -1) {
                loadTrips();
            } else {
                loadReturnTrips();
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(routeBanner, BorderLayout.NORTH);
        top.add(summary, BorderLayout.CENTER);
        top.add(filters, BorderLayout.SOUTH);

        pageTitle = new JLabel("  Select Trip");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 30));
        pageTitle.setForeground(new Color(35, 35, 35));
        pageTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        tripsPanel.setLayout(new BoxLayout(tripsPanel, BoxLayout.Y_AXIS));
        tripsPanel.setBackground(UI.light);

        returnTripsPanel.setLayout(new BoxLayout(returnTripsPanel, BoxLayout.Y_AXIS));
        returnTripsPanel.setBackground(UI.light);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UI.light);
        center.add(pageTitle, BorderLayout.NORTH);
        center.add(new JScrollPane(tripsPanel), BorderLayout.CENTER);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        add(main);
        loadTrips();
        UI.maximizeWindow(this);
        setVisible(true);
    }


    public SearchTripsFrame(int customerId, String fromStation, String toStation, String travelDate, String returnDate,
                            int adults, int children, int infants, String tripType,
                            int selectedOutboundTripId, String selectedFareType, double selectedFarePrice,
                            ArrayList<String> outboundSeats) {
        this(customerId, fromStation, toStation, travelDate, returnDate, adults, children, infants, tripType);
        this.selectedOutboundTripId = selectedOutboundTripId;
        this.selectedFareType = selectedFareType;
        this.selectedFarePrice = selectedFarePrice;
        this.outboundSeats = outboundSeats;
        openReturnMode();
    }

    void openReturnMode() {
        returnMode = true;
        pageTitle.setText("  Select Return Trip");
        loadReturnTrips();
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

    void loadTrips() {
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
                    ? " ORDER BY t.price ASC "
                    : " ORDER BY t.departure_time ASC ";

            String sql =
                    "SELECT t.trip_id, tr.train_name, s1.station_name AS from_station, s2.station_name AS to_station, " +
                    "TIME(t.departure_time) AS dep_time, TIME(t.arrival_time) AS arr_time, " +
                    "t.departure_time, t.arrival_time, t.price, tr.capacity, t.status, " +
                    "(tr.capacity - COUNT(CASE WHEN tk.ticket_status='Booked' THEN 1 END)) AS seats_left " +
                    "FROM trips t " +
                    "JOIN trains tr ON t.train_id = tr.train_id " +
                    "JOIN stations s1 ON t.departure_station = s1.station_id " +
                    "JOIN stations s2 ON t.arrival_station = s2.station_id " +
                    "LEFT JOIN tickets tk ON t.trip_id = tk.trip_id " +
                    "WHERE s1.station_name=? AND s2.station_name=? AND DATE(t.departure_time)=? " +
                    timeCondition +
                    "GROUP BY t.trip_id, tr.train_name, s1.station_name, s2.station_name, " +
                    "t.departure_time, t.arrival_time, t.price, tr.capacity, t.status " +
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
                double economyPrice = rs.getDouble("price");
                double businessPrice = economyPrice + 105;
                int seatsLeft = rs.getInt("seats_left");
                String status = rs.getString("status");

                tripsPanel.add(tripCard(tripId, trainName, depTime, arrTime, economyPrice, businessPrice, seatsLeft, status));
                tripsPanel.add(Box.createVerticalStrut(14));
            }

            if (count == 0) {
                JLabel noTrips = new JLabel("No trips found for this filter.", SwingConstants.CENTER);
                noTrips.setFont(new Font("Arial", Font.BOLD, 22));
                noTrips.setBorder(BorderFactory.createEmptyBorder(80, 0, 80, 0));
                tripsPanel.add(noTrips);
            }

            tripsPanel.revalidate();
            tripsPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }


    void loadReturnTrips() {
        tripsPanel.removeAll();
        selectedReturnTripId = -1;

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
                    ? " ORDER BY t.price ASC "
                    : " ORDER BY t.departure_time ASC ";

            String sql =
                    "SELECT t.trip_id, tr.train_name, TIME(t.departure_time) AS dep_time, TIME(t.arrival_time) AS arr_time, " +
                    "t.price, tr.capacity, t.status, " +
                    "(tr.capacity - COUNT(CASE WHEN tk.ticket_status='Booked' THEN 1 END)) AS seats_left " +
                    "FROM trips t " +
                    "JOIN trains tr ON t.train_id = tr.train_id " +
                    "JOIN stations s1 ON t.departure_station = s1.station_id " +
                    "JOIN stations s2 ON t.arrival_station = s2.station_id " +
                    "LEFT JOIN tickets tk ON t.trip_id = tk.trip_id " +
                    "WHERE s1.station_name=? AND s2.station_name=? AND DATE(t.departure_time)=? " +
                    timeCondition +
                    "GROUP BY t.trip_id, tr.train_name, s1.station_name, s2.station_name, " +
                    "t.departure_time, t.arrival_time, t.price, tr.capacity, t.status " +
                    orderBy;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, toStation);
            ps.setString(2, fromStation);
            ps.setString(3, returnDate);

            ResultSet rs = ps.executeQuery();
            int count = 0;

            while (rs.next()) {
                count++;
                int tripId = rs.getInt("trip_id");
                String trainName = rs.getString("train_name");
                String depTime = rs.getString("dep_time").substring(0, 5);
                String arrTime = rs.getString("arr_time").substring(0, 5);
                double economyPrice = rs.getDouble("price");
                int seatsLeft = rs.getInt("seats_left");
                String status = rs.getString("status");

                tripsPanel.add(returnTripCard(tripId, trainName, depTime, arrTime, economyPrice, seatsLeft, status));
                tripsPanel.add(Box.createVerticalStrut(10));
            }

            if (count == 0) {
                JLabel noTrips = new JLabel("No return trips found for this return date.", SwingConstants.CENTER);
                noTrips.setFont(new Font("Arial", Font.BOLD, 20));
                noTrips.setBorder(BorderFactory.createEmptyBorder(60, 0, 60, 0));
                tripsPanel.add(noTrips);
            }

            tripsPanel.revalidate();
            tripsPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    JPanel returnTripCard(int tripId, String trainName, String depTime, String arrTime,
                          double economyPrice, int seatsLeft, String status) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(1150, 115));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel left = new JPanel(new GridLayout(1, 6, 14, 0));
        left.setBackground(Color.WHITE);

        left.add(bigTime(depTime, shortName(toStation)));

        JLabel arrow = new JLabel("→", SwingConstants.CENTER);
        arrow.setFont(new Font("Arial", Font.BOLD, 26));
        arrow.setForeground(UI.teal);
        left.add(arrow);

        left.add(bigTime(arrTime, shortName(fromStation)));

        JLabel train = new JLabel("<html><b>Return Trip</b><br>" + trainName + "</html>");
        train.setFont(new Font("Arial", Font.BOLD, 15));
        left.add(train);

        JLabel price = new JLabel("<html><b>Starting From</b><br>﷼ " + economyPrice + "</html>");
        price.setFont(new Font("Arial", Font.BOLD, 14));
        left.add(price);

        JLabel seats = new JLabel("<html><b>Seats Left</b><br>" + seatsLeft + "</html>");
        seats.setFont(new Font("Arial", Font.BOLD, 14));
        seats.setForeground(seatsLeft >= passengerCount ? UI.green : UI.red);
        left.add(seats);

        JButton select = UI.tealButton("Select Return");
        select.setPreferredSize(new Dimension(160, 65));

        if (!status.equalsIgnoreCase("Available") || seatsLeft < passengerCount) {
            select.setText("Not Available");
            select.setEnabled(false);
        } else {
            select.addActionListener(e -> {
                selectedReturnTripId = tripId;
                JOptionPane.showMessageDialog(this, "Return trip selected: " + depTime + " → " + arrTime);
                new SeatSelectionFrame(customerId, selectedReturnTripId, selectedFareType, selectedFarePrice,
                        passengerCount, adults, children, infants, tripType, returnDate, selectedReturnTripId,
                        fromStation, toStation, travelDate, outboundSeats, true, selectedOutboundTripId);
                dispose();
            });
        }

        card.add(left, BorderLayout.CENTER);
        card.add(select, BorderLayout.EAST);
        return card;
    }

    JPanel tripCard(int tripId, String trainName, String depTime, String arrTime,
                    double economyPrice, double businessPrice, int seatsLeft, String status) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(1150, 140));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JPanel left = new JPanel(new GridLayout(1, 6, 14, 0));
        left.setBackground(Color.WHITE);

        JLabel dep = bigTime(depTime, shortName(fromStation));
        JLabel arrow = new JLabel("→", SwingConstants.CENTER);
        arrow.setFont(new Font("Arial", Font.BOLD, 28));
        arrow.setForeground(UI.teal);

        JLabel arr = bigTime(arrTime, shortName(toStation));

        JLabel train = new JLabel("<html><b>4 H 16 M</b><br>" + trainName + "</html>");
        train.setFont(new Font("Arial", Font.BOLD, 16));
        train.setForeground(new Color(35, 35, 35));

/*/
        JLabel stops = new JLabel("  ⦿ 2 Stops  ");
        stops.setOpaque(true);
        stops.setBackground(UI.teal);
        stops.setForeground(Color.WHITE);
        stops.setFont(new Font("Arial", Font.BOLD, 13));
/*/


        int totalStops = calculateStops(fromStation, toStation); 
        String stopsText = (totalStops == 0) ? " Direct " : "  ⦿ " + totalStops + " Stops  ";
        
        JLabel stops = new JLabel(stopsText);
        stops.setOpaque(true);
        stops.setBackground(UI.teal);
        stops.setForeground(Color.WHITE);
        stops.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel seatInfo = new JLabel("<html><b>Seats Left</b><br>" + seatsLeft + "</html>");
        seatInfo.setFont(new Font("Arial", Font.BOLD, 15));
        seatInfo.setForeground(seatsLeft >= passengerCount ? UI.green : UI.red);

        left.add(dep);
        left.add(arrow);
        left.add(arr);
        left.add(train);
        left.add(stops);
        left.add(seatInfo);

        JPanel right = new JPanel(new GridLayout(1, 2, 0, 0));
        right.setBackground(Color.WHITE);
        right.setPreferredSize(new Dimension(310, 90));

        double economyFinal = economyPrice * tripMultiplier;
        double businessFinal = businessPrice * tripMultiplier;

        JButton economy = fareButton("Economy", economyFinal, seatsLeft, tripId);
        JButton business = fareButton("Business", businessFinal, seatsLeft, tripId);

        right.add(economy);
        right.add(business);

        if (!status.equalsIgnoreCase("Available") || seatsLeft < passengerCount) {
            JButton full = new JButton("<html><center>Sorry, no seats are available<br>for this trip.</center></html>");
            full.setEnabled(false);
            full.setFont(new Font("Arial", Font.BOLD, 15));
            right.removeAll();
            right.setLayout(new BorderLayout());
            right.add(full, BorderLayout.CENTER);
        }

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);

        return card;
    }
private int calculateStops(String from, String to) {
        String[] fullPath = {"Dammam", "Abqaiq", "Hofuf", "Al-Kharj", "Riyadh", "Majmaah", "Qassim", "Hail", "Jauf", "Qurayyat"};
        String cleanFrom = from.replace(" Station", "").trim();
        String cleanTo = to.replace(" Station", "").trim();

        int startIdx = -1, endIdx = -1;
        for (int i = 0; i < fullPath.length; i++) {
            if (fullPath[i].equalsIgnoreCase(cleanFrom)) startIdx = i;
            if (fullPath[i].equalsIgnoreCase(cleanTo)) endIdx = i;
        }
        if (startIdx == -1 || endIdx == -1) return 0;
        return Math.max(Math.abs(startIdx - endIdx) - 1, 0);
    }

    JLabel bigTime(String time, String station) {
        JLabel l = new JLabel("<html><span style='font-size:28px;'><b>" + time + "</b></span><br>" + station + "</html>");
        l.setFont(new Font("Arial", Font.PLAIN, 15));
        l.setForeground(UI.teal);
        return l;
    }

    JButton fareButton(String type, double price, int seatsLeft, int tripId) {
        JButton b = new JButton("<html><center>" + type + "<br><b>﷼ " + price + "</b><br><span style='font-size:9px;'>" + tripType + "</span></center></html>");
        b.setBackground(type.equals("Economy") ? Color.WHITE : UI.teal);
        b.setForeground(type.equals("Economy") ? UI.teal : Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.addActionListener(e -> {
            if (seatsLeft < passengerCount) {
                JOptionPane.showMessageDialog(this, "Not enough seats for selected passengers.");
                return;
            }

            if (tripType.equalsIgnoreCase("Round Trip") && !returnMode) {
                // First choose outbound seats, then come back to choose return trip
                new SeatSelectionFrame(customerId, tripId, type, price, passengerCount, adults, children, infants,
                        tripType, returnDate, -1, fromStation, toStation, travelDate, new ArrayList<String>(), false, tripId);
                dispose();
                return;
            }

            new SeatSelectionFrame(customerId, tripId, type, price, passengerCount, adults, children, infants,
                    tripType, returnDate, selectedReturnTripId, fromStation, toStation, travelDate, outboundSeats, returnMode, selectedOutboundTripId);
            dispose();
        });
        return b;
    }
}


class SeatSelectionFrame extends JFrame {
    int customerId;
    int tripId;
    String fareType;
    double unitPrice;
    int passengerCount;
    int adults;
    int children;
    int infants;
    String tripType;
    String returnDate;
    int selectedReturnTripId;
    int outboundTripId = -1;
    String fromStation;
    String toStation;
    String travelDate;
    ArrayList<String> outboundSeats = new ArrayList<>();
    boolean choosingReturnSeats = false;

    JPanel seatPanel = new JPanel();
    JLabel selectedLabel = new JLabel("Selected seats: 0");
    ArrayList<String> selectedSeats = new ArrayList<>();
    ArrayList<String> bookedSeats = new ArrayList<>();

    public SeatSelectionFrame(int customerId, int tripId, String fareType, double unitPrice,
                              int passengerCount, int adults, int children, int infants,
                              String tripType, String returnDate, int selectedReturnTripId,
                              String fromStation, String toStation, String travelDate,
                              ArrayList<String> outboundSeats, boolean choosingReturnSeats, int outboundTripId) {
        this.customerId = customerId;
        this.tripId = tripId;
        this.fareType = fareType;
        this.unitPrice = unitPrice;
        this.passengerCount = passengerCount;
        this.adults = adults;
        this.children = children;
        this.infants = infants;
        this.tripType = tripType;
        this.returnDate = returnDate;
        this.selectedReturnTripId = selectedReturnTripId;
        this.outboundTripId = outboundTripId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.travelDate = travelDate;
        this.outboundSeats = outboundSeats;
        this.choosingReturnSeats = choosingReturnSeats;

        setTitle("Seat Selection");
        setSize(1000, 680);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UI.light);

        JLabel header = new JLabel("  Select " + (choosingReturnSeats ? "Return" : "Outbound") + " Seats - " + fareType + " | Passengers: " + passengerCount);
        header.setOpaque(true);
        header.setBackground(UI.teal);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setPreferredSize(new Dimension(1000, 60));

        JPanel info = new JPanel(new GridLayout(1, 5, 15, 0));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        info.add(new JLabel("Adults: " + adults, SwingConstants.CENTER));
        info.add(new JLabel("Children: " + children, SwingConstants.CENTER));
        info.add(new JLabel("Infants: " + infants, SwingConstants.CENTER));
        info.add(new JLabel(tripType.equalsIgnoreCase("Round Trip") ? "Return: " + returnDate : "One Way", SwingConstants.CENTER));
        info.add(selectedLabel);

        JPanel trainArea = new JPanel(new BorderLayout());
        trainArea.setBackground(new Color(235, 235, 235));
        trainArea.setBorder(BorderFactory.createEmptyBorder(25, 80, 25, 80));

        JLabel front = new JLabel("Train Front", SwingConstants.CENTER);
        front.setOpaque(true);
        front.setBackground(new Color(210, 210, 210));
        front.setFont(new Font("Arial", Font.BOLD, 16));

        // 5 columns: 2 seats + aisle + 2 seats
        seatPanel.setLayout(new GridLayout(0, 5, 14, 14));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(25, 70, 25, 70)
        ));

        trainArea.add(front, BorderLayout.NORTH);
        trainArea.add(seatPanel, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        legend.setBackground(Color.WHITE);
        legend.add(legendItem("Economy Available", UI.green));
        legend.add(legendItem("Business Available", UI.businessSeat));
        legend.add(legendItem("Booked", UI.red));
        legend.add(legendItem("Selected", UI.teal));

        JButton continueBtn = UI.tealButton("Continue to Payment");
        continueBtn.addActionListener(e -> continuePayment());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(legend, BorderLayout.CENTER);
        bottom.add(continueBtn, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);
        main.add(info, BorderLayout.BEFORE_FIRST_LINE);
        main.add(trainArea, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        add(main);
        loadSeats();
        UI.maximizeWindow(this);
        setVisible(true);
    }

    JLabel legendItem(String text, Color color) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setOpaque(true);
        l.setBackground(color);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        return l;
    }

    void loadSeats() {
        seatPanel.removeAll();
        bookedSeats.clear();

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement capPs = con.prepareStatement(
                    "SELECT tr.capacity FROM trips t JOIN trains tr ON t.train_id=tr.train_id WHERE t.trip_id=?"
            );
            capPs.setInt(1, tripId);
            ResultSet capRs = capPs.executeQuery();

            if (!capRs.next()) {
                JOptionPane.showMessageDialog(this, "Trip not found.");
                return;
            }

            int capacity = capRs.getInt("capacity");
            int businessStartRow = Math.max(1, (capacity / 8) + 1); 
            // Example with 24 seats: rows 1-3 Economy, rows 4-6 Business.

            PreparedStatement bookedPs = con.prepareStatement(
                    "SELECT seat_number FROM tickets WHERE trip_id=? AND ticket_status='Booked'"
            );
            bookedPs.setInt(1, tripId);
            ResultSet bookedRs = bookedPs.executeQuery();

            while (bookedRs.next()) {
                bookedSeats.add(bookedRs.getString("seat_number"));
            }

            for (int i = 1; i <= capacity; i += 4) {
                addSeatButton(i, capacity, businessStartRow);
                addSeatButton(i + 1, capacity, businessStartRow);

                JLabel aisle = new JLabel("AISLE", SwingConstants.CENTER);
                aisle.setFont(new Font("Arial", Font.BOLD, 11));
                aisle.setForeground(new Color(120, 120, 120));
                aisle.setOpaque(true);
                aisle.setBackground(new Color(245, 245, 245));
                seatPanel.add(aisle);

                addSeatButton(i + 2, capacity, businessStartRow);
                addSeatButton(i + 3, capacity, businessStartRow);
            }

            seatPanel.revalidate();
            seatPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void addSeatButton(int seatNumber, int capacity, int businessStartRow) {
        if (seatNumber > capacity) {
            seatPanel.add(new JLabel(""));
            return;
        }

        String seat = seatName(seatNumber);
        int rowNumber = (seatNumber - 1) / 4 + 1;
        boolean isBusinessSeat = rowNumber >= businessStartRow;
        boolean selectedBusinessFare = fareType.equalsIgnoreCase("Business");
        boolean selectedEconomyFare = fareType.equalsIgnoreCase("Economy");

        JButton b = new JButton(seat);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);

        if (bookedSeats.contains(seat)) {
            b.setBackground(UI.red);
            b.setForeground(Color.WHITE);
            b.setText(seat + " Booked");
            b.setEnabled(false);
        } else if (selectedEconomyFare && isBusinessSeat) {
            b.setBackground(UI.businessSeat);
            b.setForeground(Color.WHITE);
            b.setText(seat + " Business");
            b.setEnabled(false);
        } else if (selectedBusinessFare && !isBusinessSeat) {
            b.setBackground(UI.green);
            b.setForeground(Color.WHITE);
            b.setText(seat + " Economy");
            b.setEnabled(false);
        } else {
            b.setBackground(isBusinessSeat ? UI.businessSeat : UI.green);
            b.setForeground(Color.WHITE);
            b.setText(seat);
            b.addActionListener(e -> toggleSeat(b, seat, isBusinessSeat));
        }

        seatPanel.add(b);
    }

    String seatName(int number) {
        int row = (number - 1) / 4 + 1;
        int col = (number - 1) % 4;
        char letter = (char) ('A' + col);
        return row + String.valueOf(letter);
    }

    void toggleSeat(JButton button, String seat, boolean isBusinessSeat) {
        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            button.setBackground(isBusinessSeat ? UI.businessSeat : UI.green);
        } else {
            if (selectedSeats.size() >= passengerCount) {
                JOptionPane.showMessageDialog(this, "You can select only " + passengerCount + " seats.");
                return;
            }
            selectedSeats.add(seat);
            button.setBackground(UI.teal);
        }

        selectedLabel.setText("Selected seats: " + selectedSeats.size() + " / " + passengerCount);
    }

    void continuePayment() {
        if (selectedSeats.size() != passengerCount) {
            JOptionPane.showMessageDialog(this, "Please select exactly " + passengerCount + " seats.");
            return;
        }

        if (tripType.equalsIgnoreCase("Round Trip") && !choosingReturnSeats) {
            JOptionPane.showMessageDialog(this, "Outbound seats selected. Now choose your return trip time.");
            new SearchTripsFrame(customerId, fromStation, toStation, travelDate, returnDate,
                    adults, children, infants, tripType,
                    tripId, fareType, unitPrice, new ArrayList<String>(selectedSeats));
            dispose();
            return;
        }

        new PaymentFrame(customerId, tripId, fareType, unitPrice, selectedSeats,
                tripType, returnDate, selectedReturnTripId, outboundSeats, outboundTripId);
        dispose();
    }
}


class SeatMapFrame extends JFrame {
    JTextField tripId = new JTextField();
    JPanel seatsPanel = new JPanel();
    JLabel info = new JLabel("Enter Trip ID to view seats.");

    public SeatMapFrame() {
        setTitle("Seat Map");
        setSize(850, 600);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton load = UI.tealButton("Load Seat Map");

        top.add(new JLabel("Trip ID:"), BorderLayout.WEST);
        top.add(tripId, BorderLayout.CENTER);
        top.add(load, BorderLayout.EAST);

        // 5 columns: 2 seats + aisle + 2 seats
        seatsPanel.setLayout(new GridLayout(0, 5, 12, 12));
        seatsPanel.setBorder(BorderFactory.createEmptyBorder(25, 80, 25, 80));
        seatsPanel.setBackground(UI.light);

        load.addActionListener(e -> loadSeats());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(seatsPanel), BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void loadSeats() {
        seatsPanel.removeAll();

        try {
            int id = Integer.parseInt(tripId.getText());

            Connection con = DBConnection.getConnection();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement(
                    "SELECT tr.capacity FROM trips t JOIN trains tr ON t.train_id=tr.train_id WHERE t.trip_id=?"
            );
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Trip not found.");
                return;
            }

            int capacity = rs.getInt("capacity");
            int businessStartRow = Math.max(1, (capacity / 8) + 1);

            ArrayList<String> booked = new ArrayList<>();

            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT seat_number FROM tickets WHERE trip_id=? AND ticket_status='Booked'"
            );
            ps2.setInt(1, id);

            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                booked.add(rs2.getString("seat_number"));
            }

            for (int i = 1; i <= capacity; i += 4) {
                addEmployeeSeatButton(i, capacity, businessStartRow, booked, id);
                addEmployeeSeatButton(i + 1, capacity, businessStartRow, booked, id);

                JLabel aisle = new JLabel("AISLE", SwingConstants.CENTER);
                aisle.setFont(new Font("Arial", Font.BOLD, 11));
                aisle.setForeground(new Color(120, 120, 120));
                aisle.setOpaque(true);
                aisle.setBackground(new Color(245, 245, 245));
                seatsPanel.add(aisle);

                addEmployeeSeatButton(i + 2, capacity, businessStartRow, booked, id);
                addEmployeeSeatButton(i + 3, capacity, businessStartRow, booked, id);
            }

            info.setText(
                    "Capacity: " + capacity +
                            " | Booked: " + booked.size() +
                            " | Available: " + (capacity - booked.size()) +
                            " | Dark seats = Business"
            );

            seatsPanel.revalidate();
            seatsPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    void addEmployeeSeatButton(int seatNumber, int capacity, int businessStartRow, ArrayList<String> booked, int tripIdValue) {
        if (seatNumber > capacity) {
            seatsPanel.add(new JLabel(""));
            return;
        }

        String seat = seatName(seatNumber);
        int rowNumber = (seatNumber - 1) / 4 + 1;
        boolean isBusinessSeat = rowNumber >= businessStartRow;

        JButton b = new JButton(seat);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setFocusPainted(false);

        if (booked.contains(seat)) {
            b.setBackground(UI.red);
            b.setForeground(Color.WHITE);
            b.setText(seat + " Booked");
            b.addActionListener(e -> showSeatInfo(tripIdValue, seat));
        } else {
            b.setBackground(isBusinessSeat ? UI.businessSeat : UI.green);
            b.setForeground(Color.WHITE);
            b.setText(seat + (isBusinessSeat ? " Business" : " Economy"));
        }

        seatsPanel.add(b);
    }

    String seatName(int number) {
        int row = (number - 1) / 4 + 1;
        int col = (number - 1) % 4;
        char letter = (char) ('A' + col);
        return row + String.valueOf(letter);
    }

    void showSeatInfo(int tripId, String seat) {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql =
                    "SELECT c.full_name, tk.ticket_id, p.payment_status " +
                    "FROM tickets tk JOIN customers c ON tk.customer_id=c.customer_id " +
                    "LEFT JOIN payments p ON tk.ticket_id=p.ticket_id " +
                    "WHERE tk.trip_id=? AND tk.seat_number=? AND tk.ticket_status='Booked'";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, tripId);
            ps.setString(2, seat);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Seat: " + seat +
                                "\nStatus: Booked" +
                                "\nPassenger: " + rs.getString("full_name") +
                                "\nTicket ID: " + rs.getInt("ticket_id") +
                                "\nPayment: " + rs.getString("payment_status"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}


class ViewTripsFrame extends JFrame {
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);

    public ViewTripsFrame(boolean employeeMode, int customerId) {
        setTitle("Available Trips");
        setSize(950, 450);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        model.setColumnIdentifiers(new String[]{
                "Trip ID", "Train", "From", "To", "Departure", "Arrival", "Price", "Capacity", "Seats Left", "Status"
        });

        loadTrips();

        add(new JScrollPane(table));
        UI.maximizeWindow(this);
        setVisible(true);
    }

    void loadTrips() {
        model.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            if (con == null) return;

            String sql =
                    "SELECT t.trip_id, tr.train_name, s1.station_name AS from_station, s2.station_name AS to_station, " +
                    "t.departure_time, t.arrival_time, t.price, tr.capacity, t.status, " +
                    "(tr.capacity - COUNT(CASE WHEN tk.ticket_status='Booked' THEN 1 END)) AS seats_left " +
                    "FROM trips t " +
                    "JOIN trains tr ON t.train_id = tr.train_id " +
                    "JOIN stations s1 ON t.departure_station = s1.station_id " +
                    "JOIN stations s2 ON t.arrival_station = s2.station_id " +
                    "LEFT JOIN tickets tk ON t.trip_id = tk.trip_id " +
                    "GROUP BY t.trip_id, tr.train_name, s1.station_name, s2.station_name, " +
                    "t.departure_time, t.arrival_time, t.price, tr.capacity, t.status";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("trip_id"),
                        rs.getString("train_name"),
                        rs.getString("from_station"),
                        rs.getString("to_station"),
                        rs.getString("departure_time"),
                        rs.getString("arrival_time"),
                        rs.getDouble("price"),
                        rs.getInt("capacity"),
                        rs.getInt("seats_left"),
                        rs.getString("status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
