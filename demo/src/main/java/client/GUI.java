package client;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class GUI extends JFrame {
    private JTextField messageField;
    private JTextArea responseArea;
    private JButton sendButton;
    private io.netty.channel.Channel channel;
    private Client client;

    public GUI() {
        setTitle("Client - Quét Subdomain");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Gửi");
        inputPanel.add(new JLabel("Tin nhắn: "), BorderLayout.WEST);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        responseArea = new JTextArea();
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);

        JLabel statusLabel = new JLabel("Trạng thái: Sẵn sàng");

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
    }

    public void setChannel(io.netty.channel.Channel channel) {
        this.channel = channel;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            String signature = client.signMessage(message);
            String encryptedPublicKey = client.getCryptoUtilitis().encryptPublicKey();

            String payload = String.format(
                    "{\"message\":\"%s\",\"signature\":\"%s\",\"publicKey\":\"%s\"}",
                    Base64.getEncoder().encodeToString(message.getBytes()),
                    signature,
                    encryptedPublicKey);

            channel.writeAndFlush(payload + "\n");
            responseArea.append("Đã gửi tin nhắn: " + message + "\n");
            messageField.setText("");
        } catch (Exception e) {
            responseArea.append("Lỗi: " + e.getMessage() + "\n");
        }
    }

    public void appendResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            responseArea.append("Phản hồi từ server: " + response + "\n");
        });
    }
}
