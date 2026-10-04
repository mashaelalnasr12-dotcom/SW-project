package com.mycompany.javaproject2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;


class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/railway_db";
    private static final String USER = "root";

    // Change this if your MySQL password is different.
    private static final String PASSWORD = "Narimanna2011";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            ensureExtraTables(con);
            return con;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            return null;
        }
    }

    private static void ensureExtraTables(Connection con) {
        try {
            Statement st = con.createStatement();

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS car_cargo_bookings (" +
                            "cargo_id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "customer_id INT, " +
                            "departure_station VARCHAR(100), " +
                            "arrival_station VARCHAR(100), " +
                            "travel_date VARCHAR(30), " +
                            "departure_time VARCHAR(20), " +
                            "arrival_time VARCHAR(20), " +
                            "car_plate VARCHAR(30), " +
                            "amount DOUBLE, " +
                            "status VARCHAR(30), " +
                            "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            try {
                st.executeUpdate("ALTER TABLE employees ADD COLUMN full_name VARCHAR(100)");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("ALTER TABLE employees ADD COLUMN role VARCHAR(50) DEFAULT 'Staff'");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("ALTER TABLE car_cargo_bookings ADD COLUMN departure_time VARCHAR(20)");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("ALTER TABLE car_cargo_bookings ADD COLUMN arrival_time VARCHAR(20)");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("ALTER TABLE car_cargo_bookings ADD COLUMN car_brand VARCHAR(50)");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("ALTER TABLE car_cargo_bookings ADD COLUMN car_model VARCHAR(50)");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("ALTER TABLE car_cargo_bookings ADD COLUMN owner_name VARCHAR(100)");
            } catch (Exception ignored) {}

            try {
                st.executeUpdate("UPDATE employees SET role='Admin' WHERE username='admin'");
            } catch (Exception ignored) {}

        } catch (Exception ignored) {}
    }
}


class UI {
    // Unified modern railway color palette
    static Color navy = new Color(18, 44, 64);
    static Color teal = new Color(31, 118, 135);
    static Color tealDark = new Color(21, 88, 104);
    static Color blue = new Color(31, 118, 135);
    static Color light = new Color(246, 248, 250);
    static Color paleBlue = new Color(230, 245, 248);
    static Color gold = new Color(198, 153, 68);
    static Color softGray = new Color(242, 244, 246);
    static Color red = new Color(180, 58, 64);
    static Color green = new Color(67, 155, 96);
    static Color businessSeat = new Color(22, 65, 82);

    static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(blue);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        return b;
    }

    static JButton tealButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(teal);
        b.setForeground(blue);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        return b;
    }

    static JButton lightButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(tealDark);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        return b;
    }

    static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 24));
        l.setForeground(navy);
        return l;
    }

    static JLabel imageBox(String imageName, int width, int height) {
        JLabel l = new JLabel() {
            Image image;

            {
                try {
                    java.net.URL imgURL = UI.class.getResource("/com/mycompany/javaproject2/images/" + imageName);
                    if (imgURL != null) {
                        image = new ImageIcon(imgURL).getImage();
                    }
                } catch (Exception e) {
                    image = null;
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (image != null) {
                    // Fill the whole box while keeping the image aspect ratio.
                    int panelW = getWidth();
                    int panelH = getHeight();
                    int imgW = image.getWidth(this);
                    int imgH = image.getHeight(this);

                    if (imgW > 0 && imgH > 0) {
                        double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
                        int newW = (int) Math.round(imgW * scale);
                        int newH = (int) Math.round(imgH * scale);
                        int x = (panelW - newW) / 2;
                        int y = (panelH - newH) / 2;

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2.drawImage(image, x, y, newW, newH, this);
                        g2.dispose();
                    }
                } else {
                    g.setColor(new Color(226, 235, 238));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(UI.navy);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.drawString("Image not found: " + imageName, 15, 25);
                }
            }
        };

        l.setPreferredSize(new Dimension(width, height));
        l.setMinimumSize(new Dimension(width, height));
        l.setOpaque(true);
        l.setBackground(new Color(226, 235, 238));
        l.setBorder(BorderFactory.createLineBorder(new Color(205, 215, 220)));
        return l;
    }

    static void maximizeWindow(JFrame frame) {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1000, 650));
    }

    static JPanel whiteCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return p;
    }
}
