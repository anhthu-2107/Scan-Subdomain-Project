package server;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private Server server;

    public ServerGUI() {
        setTitle("Server - Quét Subdomain");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JLabel statusLabel = new JLabel("Trạng thái: Sẵn sàng");

        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void appendLog(String log) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(log);
        });
    }
}
