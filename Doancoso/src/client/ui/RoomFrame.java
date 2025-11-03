package client.ui;

import client.net.NetStub;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RoomFrame extends JFrame implements NetStub.Listener {
    private final String roomId;
    private final JTextArea chatArea = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton btnSend = new JButton("Gửi");
    private final JLabel title = new JLabel();

    private final NetStub net = NetStub.i();
    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");

    public RoomFrame(String roomId){
        super("Tiến Lên — Phòng");
        this.roomId = roomId;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 420);
        setLocationRelativeTo(null);

        title.setText("Phòng: " + roomId);
        title.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        chatArea.setEditable(false);
        JScrollPane sp = new JScrollPane(chatArea);

        JPanel inputBar = new JPanel(new BorderLayout(6,6));
        inputBar.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        inputBar.add(input, BorderLayout.CENTER);
        inputBar.add(btnSend, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(inputBar, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> send());
        input.addActionListener(e -> send());

        net.addListener(this);
    }

    private void send(){
        String text = input.getText().trim();
        if(text.isEmpty()) return;
        net.sendChat(roomId, text);
        input.setText("");
    }

    // ==== NetStub.Listener ====
    @Override public void onOk(String message) { /* ignore */ }

    @Override public void onError(String message) {
        chatArea.append("[Lỗi] " + message + "\n");
    }

    @Override public void onRoomList(java.util.List<client.model.RoomInfo> rooms) { /* ignore */ }

    @Override public void onRoomJoined(String rid) { /* ignore */ }

    @Override public void onChat(String rid, String from, String text, long ts) {
        if(!this.roomId.equals(rid)) return;
        String time = fmt.format(new Date(ts));
        chatArea.append("[" + time + "] " + from + ": " + text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}
