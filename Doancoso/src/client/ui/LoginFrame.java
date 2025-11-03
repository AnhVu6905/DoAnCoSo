package client.ui;

import client.net.NetStub;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame implements NetStub.Listener {

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JButton btnLogin = new JButton("Đăng nhập");
    private final JButton btnRegister = new JButton("Đăng ký");
    private final JLabel status = new JLabel(" ");

    private final NetStub net = NetStub.i();

    public LoginFrame(){
        super("Tiến Lên — Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 220);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(0,1,6,6));
        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnRegister);
        actions.add(btnLogin);

        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        root.add(status, BorderLayout.NORTH);
        setContentPane(root);

        btnLogin.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> doRegister());

        net.addListener(this);
    }

    private void doLogin(){
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        net.login(u,p);
    }

    private void doRegister(){
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        net.register(u,p);
    }

    // === NetStub.Listener ===
    @Override public void onOk(String message) {
        status.setText(message);
        if (message.toLowerCase().contains("đăng nhập")) {
            SwingUtilities.invokeLater(() -> {
                dispose();
                new LobbyFrame().setVisible(true);
            });
        }
    }
    @Override public void onError(String message) { status.setText("Lỗi: " + message); }
    @Override public void onRoomList(java.util.List<client.model.RoomInfo> rooms) {}
    @Override public void onRoomJoined(String roomId) {}
    @Override public void onChat(String roomId, String from, String text, long ts) {}
}
