package client.ui;

import client.model.RoomInfo;
import client.net.NetStub;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LobbyFrame extends JFrame implements NetStub.Listener {

    private final JButton btnCreate = new JButton("Tạo phòng");
    private final JButton btnJoinRandom = new JButton("Vào ngẫu nhiên");
    private final JButton btnJoinByList = new JButton("Chọn phòng");
    private final JButton btnRefresh = new JButton("Làm mới danh sách");
    private final JTextArea info = new JTextArea(8, 40);

    private final NetStub net = NetStub.i();
    private List<RoomInfo> lastRooms = java.util.Collections.emptyList();

    public LobbyFrame() {
        super("Tiến Lên — Sảnh chơi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 360);
        setLocationRelativeTo(null);

        info.setEditable(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btnCreate);
        top.add(btnJoinRandom);
        top.add(btnJoinByList);
        top.add(btnRefresh);

        setLayout(new BorderLayout(8, 8));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(info), BorderLayout.CENTER);

        btnCreate.addActionListener(e -> net.createRoom());
        btnJoinRandom.addActionListener(e -> net.joinRandom());
        btnJoinByList.addActionListener(e -> openRoomListDialog());
        btnRefresh.addActionListener(e -> net.listRooms());

        net.addListener(this);
        net.listRooms();
    }

    private void openRoomListDialog() {
        RoomListDialog dlg = new RoomListDialog(this, lastRooms);
        String chosen = dlg.showDialog();
        if (chosen != null) {
            net.joinById(chosen);
        }
    }

    // === NetStub.Listener ===
    @Override public void onOk(String message) {
        info.append("OK: " + message + "\n");
    }

    @Override public void onError(String message) {
        info.append("Lỗi: " + message + "\n");
    }

    @Override public void onRoomList(List<RoomInfo> rooms) {
        lastRooms = rooms;
        info.append("Danh sách phòng (" + rooms.size() + "): \n");
        for (RoomInfo r : rooms) {
            info.append(" - " + r.roomId() + " (" + r.size() + ")\n");
        }
        info.append("\n");
    }

    @Override public void onRoomJoined(String roomId) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new RoomFrame(roomId).setVisible(true);
        });
    }

    @Override public void onChat(String roomId, String from, String text, long ts) {
        // Bỏ qua chat khi đang ở Lobby
    }
}
