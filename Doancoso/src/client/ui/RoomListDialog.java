package client.ui;


import client.model.RoomInfo;


import javax.swing.*;
import java.awt.*;
import java.util.List;


public class RoomListDialog extends JDialog {
private final DefaultListModel<String> model = new DefaultListModel<>();
private String chosenRoomId = null;


public RoomListDialog(Frame owner, List<RoomInfo> rooms){
super(owner, "Chọn phòng", true);
setSize(360, 300);
setLocationRelativeTo(owner);


JList<String> list = new JList<>(model);
for(RoomInfo r: rooms){ model.addElement(r.roomId()+" ("+r.size()+")"); }


JButton btnOk = new JButton("Vào phòng");
JButton btnCancel = new JButton("Hủy");


btnOk.addActionListener(e -> {
String sel = list.getSelectedValue();
if(sel != null){
int space = sel.indexOf(' ');
chosenRoomId = (space>0) ? sel.substring(0, space) : sel;
dispose();
}
});
btnCancel.addActionListener(e -> { chosenRoomId = null; dispose(); });


JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
south.add(btnCancel); south.add(btnOk);


setLayout(new BorderLayout(8,8));
add(new JScrollPane(list), BorderLayout.CENTER);
add(south, BorderLayout.SOUTH);
}


public String showDialog(){ setVisible(true); return chosenRoomId; }
}