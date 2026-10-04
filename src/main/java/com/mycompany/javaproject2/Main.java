package com.mycompany.javaproject2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.*;


public class Main {
    public static void main(String[] args) {
       SwingUtilities.invokeLater(new Runnable() {

    @Override
    public void run() {

        new RoleFrame();
    }
});
    }
}
class RoleFrame extends JFrame {
    public RoleFrame() {
        setTitle("Railway Management System");
        setSize(900, 590);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setLayout(new BorderLayout());
// ── SAR-style Navigation Bar ──────────────────────────────────────
JPanel navbar = new JPanel(new BorderLayout());
navbar.setBackground(new Color(18, 40, 58));
navbar.setPreferredSize(new Dimension(900, 55));
navbar.setMinimumSize(new Dimension(100, 55));
navbar.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

// Logo section (left side)
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

JPanel divider = new JPanel();
divider.setBackground(new Color(80, 110, 130));
divider.setPreferredSize(new Dimension(1, 28));

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
logoPanel.add(divider);
logoPanel.add(arabicPanel);

// Nav links (right side)
String[] navItems = {"About SAR", "Passenger Services", "Companies Services", "Help & Support", "More"};
JPanel navLinks = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
navLinks.setOpaque(false);

for (String item : navItems) {
    JLabel link = new JLabel(item);
    link.setFont(new Font("Arial", Font.PLAIN, 13));
    link.setForeground(new Color(210, 225, 235));
    link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    link.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent e) {
            link.setForeground(Color.WHITE);
        }
        public void mouseExited(java.awt.event.MouseEvent e) {
            link.setForeground(new Color(210, 225, 235));
        }
        public void mouseClicked(java.awt.event.MouseEvent e) {
            new InfoFrame(item);
        }
    });
    navLinks.add(link);
    if (!item.equals("More")) {
        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(80, 110, 130));
        sep.setFont(new Font("Arial", Font.PLAIN, 12));
        navLinks.add(sep);
    }
}

navbar.add(logoPanel, BorderLayout.WEST);
navbar.add(navLinks, BorderLayout.EAST);
// ── End Navbar ───────────────────────────────────────────────────
        JPanel main = new JPanel(new GridLayout(1, 2));

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(UI.navy);

        JLabel imagePlace = UI.imageBox("1.jpg", 380, 430);
        imagePlace.setBackground(new Color(28, 65, 88));
        imagePlace.setForeground(Color.WHITE);
        left.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));
        left.add(imagePlace, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UI.light);

        JPanel card = new JPanel(new GridLayout(5, 1, 10, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(45, 45, 45, 45));

        JLabel title = UI.title("Choose Portal");
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton customerBtn = UI.tealButton("Customer Portal");
        JButton employeeBtn = UI.button("Employee Portal");

        card.add(title);
        card.add(new JLabel("Welcome! Please choose your role.", SwingConstants.CENTER));
        card.add(customerBtn);
        card.add(employeeBtn);

        right.add(card);

        customerBtn.addActionListener(e -> {
            new CustomerStartFrame();
            setVisible(false);
        });

        employeeBtn.addActionListener(e -> {
            new EmployeeLoginFrame();
            setVisible(false);
        });

        main.add(left);
        main.add(right);

        add(main);
        setVisible(true);
    }
}


class InfoFrame extends JFrame {
    public InfoFrame(String pageName) {
        setTitle(pageName);
        setSize(430, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        JLabel title = new JLabel(pageName, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(UI.navy);

        JLabel text = new JLabel("", SwingConstants.CENTER);
        text.setFont(new Font("Arial", Font.PLAIN, 16));
        text.setForeground(new Color(50, 50, 50));

        if (pageName.equals("About SAR")) {
            text.setText("<html><center>Saudi Arabia Railways provides safe, modern, and reliable railway services for passengers and cargo.</center></html>");
        } else if (pageName.equals("Passenger Services")) {
            text.setText("<html><center>Passengers can search for trips, book tickets, select seats, pay, and manage bookings easily.</center></html>");
        } else if (pageName.equals("Companies Services")) {
            text.setText("<html><center>Company services include cargo transportation and business railway solutions.</center></html>");
        } else if (pageName.equals("Help & Support")) {
            text.setText("<html><center>For help and support, please contact customer service or visit the nearest railway station.</center></html>");
        } else {
            text.setText("<html><center>This section is coming soon.</center></html>");
        }

        JButton close = UI.tealButton("Close");
        close.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(close);

        panel.add(title, BorderLayout.NORTH);
        panel.add(text, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
setVisible(true);
    }}
