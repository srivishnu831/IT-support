import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

interface TicketActions {
    void createTicket(String title, String description, String category);
    void resolveTicket(int ticketId);
    String getTickets();
}

class TicketDatabaseManager implements TicketActions {
    Connection conn;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TicketDatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/it_support_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "root", "Sharavana@864"
            );
            System.out.println("✅ Connected to MySQL successfully!");
        } catch (Exception e) {
            System.out.println("❌ Connection Failed!");
            e.printStackTrace();
        }
    }

    @Override
    public void createTicket(String title, String description, String category) {
        try {
            String sql = "INSERT INTO tickets (title, description, category, created_time, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, category);
            stmt.setString(4, LocalDateTime.now().format(dtf));
            stmt.setString(5, "Open");
            stmt.executeUpdate();
            stmt.close();
            System.out.println("✅ Ticket Created Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resolveTicket(int ticketId) {
        try {
            String sql = "UPDATE tickets SET status = ?, resolved_time = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "Resolved");
            stmt.setString(2, LocalDateTime.now().format(dtf));
            stmt.setInt(3, ticketId);
            stmt.executeUpdate();
            stmt.close();
            System.out.println("✅ Ticket Resolved: ID " + ticketId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTickets() {
        StringBuilder log = new StringBuilder();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tickets ORDER BY id DESC");
            while (rs.next()) {
                log.append("ID: ").append(rs.getInt("id"))
                   .append(" | Title: ").append(rs.getString("title"))
                   .append(" | Status: ").append(rs.getString("status"))
                   .append(" | Created: ").append(rs.getString("created_time"))
                   .append(" | Resolved: ")
                   .append(rs.getString("resolved_time") == null ? "Pending" : rs.getString("resolved_time"))
                   .append("\n");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return log.toString();
    }
}

public class TicketManagerApp {
    private TicketDatabaseManager dbManager = new TicketDatabaseManager();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicketManagerApp().createGUI());
    }

    public void createGUI() {
        JFrame frame = new JFrame("IT Support Ticket Manager");
        frame.setSize(600, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField ticketIdField = new JTextField();

        JButton createBtn = new JButton("Create Ticket");
        JButton resolveBtn = new JButton("Resolve Ticket");

        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Ticket ID (for resolve):"));
        inputPanel.add(ticketIdField);
        inputPanel.add(createBtn);
        inputPanel.add(resolveBtn);

        JTextArea logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Ticket Logs"));

        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        createBtn.addActionListener(e -> {
            dbManager.createTicket(titleField.getText(), descField.getText(), categoryField.getText());
            logArea.setText(dbManager.getTickets());
        });

        resolveBtn.addActionListener(e -> {
            int id = Integer.parseInt(ticketIdField.getText());
            dbManager.resolveTicket(id);
            logArea.setText(dbManager.getTickets());
        });

        frame.setVisible(true);
    }
}
