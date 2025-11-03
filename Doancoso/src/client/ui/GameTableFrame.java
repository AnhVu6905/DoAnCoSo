package client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI bàn Tiến Lên (đã thêm màu + card đẹp)
 * - Nền bàn xanh rêu
 * - Lá bài bo góc, bóng đổ, đỏ/đen theo chất
 * - Chọn lá: nổi lên nhẹ
 */
public class GameTableFrame extends JFrame {

    // ====== Models ======
    enum Suit { CLUBS("♣"), DIAMONDS("♦"), HEARTS("♥"), SPADES("♠");
        public final String sym; Suit(String s){ this.sym=s; } }
    enum Rank {
        THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"),
        TEN("10"), J("J"), Q("Q"), K("K"), A("A"), TWO("2");
        public final String label; Rank(String l){ this.label=l; }
    }
    static class Card {
        public final Rank rank;
        public final Suit suit;
        public Card(Rank r, Suit s){ this.rank=r; this.suit=s; }
        public String text() { return rank.label + suit.sym; }
    }
    static final Comparator<Card> TIEN_LEN_ORDER =
            Comparator.comparing((Card c) -> c.rank.ordinal())
                      .thenComparingInt(c -> c.suit.ordinal());

    // ====== Colors / Theme ======
    private static final Color TABLE_BG   = new Color(16, 92, 52);   // xanh rêu
    private static final Color FELT_DARK  = new Color(10, 70, 40);
    private static final Color CARD_BG    = new Color(248, 248, 248);
    private static final Color CARD_EDGE  = new Color(210, 210, 210);
    private static final Color CARD_EDGE_SEL = new Color(0,120,215);
    private static final Color SHADOW     = new Color(0,0,0,50);
    private static final Color RED_SUIT   = new Color(200, 32, 32);
    private static final Color BLACK_SUIT = new Color(20, 20, 20);

    // ====== UI components ======
    private final JPanel tablePanel = new JPanel(new BorderLayout());
    private final JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private final JPanel southHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
    private final JLabel roomLabel = new JLabel("Phòng: demo123  |  Vòng: 1  |  Lượt: Bạn");

    private final JButton btnReady = new JButton("Sẵn sàng");
    private final JButton btnSort  = new JButton("Sắp xếp");
    private final JButton btnPlay  = new JButton("Đánh");
    private final JButton btnPass  = new JButton("Bỏ lượt");

    // Chat
    private final JTextArea chatArea = new JTextArea();
    private final JTextField chatInput = new JTextField();
    private final JButton chatSend = new JButton("Gửi");

    // Vị trí người chơi
    private final PlayerWidget north = new PlayerWidget("Người chơi 1 (Bắc)");
    private final PlayerWidget west  = new PlayerWidget("Người chơi 2 (Tây)");
    private final PlayerWidget east  = new PlayerWidget("Người chơi 3 (Đông)");
    private final PlayerWidget south = new PlayerWidget("Bạn (Nam)");

    // Tay bài của bạn
    private final java.util.List<Card> myHand = new ArrayList<>();
    private final java.util.List<CardView> myCardButtons = new ArrayList<>();
    private java.util.List<Card> lastPlayed = new ArrayList<>();
    private boolean ready = false;

    public GameTableFrame() {
        super("Tiến Lên — Bàn chơi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);

        // ==== Top bar ====
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(8,8,8,8));
        top.setBackground(FELT_DARK);
        roomLabel.setForeground(new Color(230, 245, 235));
        roomLabel.setFont(roomLabel.getFont().deriveFont(Font.BOLD, 14f));
        top.add(roomLabel, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtons.setOpaque(false);
        for (JButton b : new JButton[]{btnReady, btnSort, btnPlay, btnPass}) {
            b.setFocusPainted(false);
            b.setBackground(new Color(245, 245, 245));
            b.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
            topButtons.add(b);
        }
        top.add(topButtons, BorderLayout.EAST);

        // ==== Bàn giữa ====
        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        JLabel centerTitle = new JLabel("Lần đánh gần nhất", SwingConstants.CENTER);
        centerTitle.setForeground(new Color(240, 240, 240));
        centerTitle.setBorder(new EmptyBorder(6,0,4,0));
        centerWrap.add(centerTitle, BorderLayout.NORTH);

        centerPanel.setOpaque(false);
        centerWrap.add(centerPanel, BorderLayout.CENTER);

        tablePanel.setBorder(new EmptyBorder(8,8,8,8));
        tablePanel.setBackground(TABLE_BG);
        tablePanel.add(centerWrap, BorderLayout.CENTER);
        tablePanel.add(north.panel, BorderLayout.NORTH);
        tablePanel.add(west.panel, BorderLayout.WEST);
        tablePanel.add(east.panel, BorderLayout.EAST);

        // Beautify cạnh trái/phải
        ((FlowLayout)west.cards.getLayout()).setAlignment(FlowLayout.CENTER);
        ((FlowLayout)east.cards.getLayout()).setAlignment(FlowLayout.CENTER);

        // ==== Tay bài của bạn (Nam) ====
        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setBackground(FELT_DARK);
        JLabel youTitle = new JLabel("Tay bài của bạn", SwingConstants.CENTER);
        youTitle.setForeground(new Color(230, 245, 235));
        youTitle.setBorder(new EmptyBorder(4,0,4,0));
        southWrap.add(youTitle, BorderLayout.NORTH);

        southHandPanel.setOpaque(false);
        southWrap.add(southHandPanel, BorderLayout.CENTER);

        // ==== Chat bên phải ====
        JPanel chat = new JPanel(new BorderLayout(6,6));
        chat.setBorder(new EmptyBorder(8,8,8,8));
        chat.setBackground(new Color(245,245,245));
        JLabel chatTitle = new JLabel("Chat phòng", SwingConstants.CENTER);
        chat.add(chatTitle, BorderLayout.NORTH);
        chatArea.setEditable(false);
        chat.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel chatInputBar = new JPanel(new BorderLayout(6,6));
        chatInputBar.add(chatInput, BorderLayout.CENTER);
        chatInputBar.add(chatSend, BorderLayout.EAST);
        chat.add(chatInputBar, BorderLayout.SOUTH);

        // ==== Layout tổng ====
        setLayout(new BorderLayout(8,8));
        add(top, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(southWrap, BorderLayout.SOUTH);
        add(chat, BorderLayout.EAST);

        // ==== Hành vi nút ====
        btnReady.addActionListener(e -> toggleReady());
        btnSort.addActionListener(e -> sortMyHand());
        btnPlay.addActionListener(e -> playSelected());
        btnPass.addActionListener(e -> passTurn());
        chatSend.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat());

        // ==== Dữ liệu demo ====
        dealDemo();
        renderHands();
        renderLastPlayed();
    }

    // ====== Player widget (chỉ tên + số lá) ======
    static class PlayerWidget {
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel name = new JLabel("Player");
        final JPanel cards = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        final JLabel info = new JLabel("Lá: 0  |  Ready: ❌", SwingConstants.CENTER);
        int cardCount = 0; boolean ready = false;

        PlayerWidget(String displayName) {
            panel.setOpaque(false);
            name.setText(displayName);
            name.setBorder(new EmptyBorder(4,4,4,4));
            name.setForeground(new Color(230, 245, 235));
            info.setBorder(new EmptyBorder(0,4,4,4));
            info.setForeground(new Color(230, 245, 235));
            cards.setOpaque(false);

            panel.add(name, BorderLayout.NORTH);
            panel.add(cards, BorderLayout.CENTER);
            panel.add(info, BorderLayout.SOUTH);
            panel.setPreferredSize(new Dimension(200, 140));
        }
        void setCount(int n){ this.cardCount = n; updateInfo(); }
        void setReady(boolean r){ this.ready = r; updateInfo(); }
        void updateInfo(){ info.setText("Lá: " + cardCount + "  |  Ready: " + (ready ? "✅" : "❌")); }
    }

    // ====== Lá bài custom (vẽ tay) ======
    static class CardView extends JToggleButton {
        final Card card;
        CardView(Card c, boolean selectable){
            this.card = c;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(selectable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            if(!selectable){
                // vô hiệu chọn nhưng vẫn hiển thị như bình thường
                enableEvents(0);
                setModel(new DefaultButtonModel(){
                    @Override public boolean isPressed(){ return false; }
                    @Override public boolean isArmed(){ return false; }
                    @Override public boolean isSelected(){ return false; }
                });
            }
        }
        @Override public Dimension getPreferredSize(){ return new Dimension(56, 80); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean selected = getModel().isSelected();
            int lift = selected ? 6 : 0;

            int w = getWidth(), h = getHeight();
            int rx = 10; // corner radius

            // bóng đổ
            g2.setColor(SHADOW);
            g2.fillRoundRect(4, 6 - lift, w-4, h-6, rx, rx);

            // thân lá
            GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, h, CARD_BG);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0 - lift, w-6, h-8, rx, rx);

            // viền
            g2.setColor(selected ? CARD_EDGE_SEL : CARD_EDGE);
            g2.setStroke(new BasicStroke(selected ? 2.2f : 1.2f));
            g2.drawRoundRect(0, 0 - lift, w-6, h-8, rx, rx);

            // rank + suit
            Color suitColor = (card.suit == Suit.DIAMONDS || card.suit == Suit.HEARTS) ? RED_SUIT : BLACK_SUIT;
            g2.setColor(suitColor);
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            String rank = card.rank.label;
            String suit = card.suit.sym;
            g2.drawString(rank, 8, 18 - lift);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
            g2.drawString(suit, 8, 34 - lift);

            // suit lớn giữa lá
            g2.setFont(getFont().deriveFont(Font.BOLD, 28f));
            FontMetrics fm = g2.getFontMetrics();
            int sw = fm.stringWidth(suit);
            g2.drawString(suit, (w-6 - sw)/2, (h-8)/2 + 8 - lift);

            g2.dispose();
        }
    }

    // ====== Demo: chia bài ======
    private void dealDemo() {
        java.util.List<Card> deck = new ArrayList<>(52);
        for (Rank r: Rank.values()) for (Suit s: Suit.values()) deck.add(new Card(r, s));
        Collections.shuffle(deck);
        java.util.List<Card> northHand = new ArrayList<>();
        java.util.List<Card> westHand  = new ArrayList<>();
        java.util.List<Card> eastHand  = new ArrayList<>();
        for (int i=0;i<13;i++) northHand.add(deck.get(i));
        for (int i=13;i<26;i++) westHand.add(deck.get(i));
        for (int i=26;i<39;i++) eastHand.add(deck.get(i));
        for (int i=39;i<52;i++) myHand.add(deck.get(i));
        north.setCount(northHand.size());
        west.setCount(westHand.size());
        east.setCount(eastHand.size());
        south.setCount(myHand.size());
    }

    // ====== Tay bài của bạn ======
    private void renderHands() {
        southHandPanel.removeAll();
        southHandPanel.setBackground(TABLE_BG);
        myCardButtons.clear();
        myHand.sort(TIEN_LEN_ORDER);
        for (Card c : myHand) {
            CardView btn = new CardView(c, true);
            // toggle nâng/hạ: đã xử lý trong paint
            myCardButtons.add(btn);
            southHandPanel.add(btn);
        }
        southHandPanel.revalidate();
        southHandPanel.repaint();
    }

    private void renderLastPlayed(){
        centerPanel.removeAll();
        if (lastPlayed.isEmpty()) {
            JLabel hint = new JLabel("(Chưa có lượt đánh)");
            hint.setForeground(new Color(235, 245, 240));
            centerPanel.add(hint);
        } else {
            for (Card c : lastPlayed) {
                centerPanel.add(new CardView(c, false));
            }
        }
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // ====== Nút hành động ======
    private void toggleReady(){
        ready = !ready;
        south.setReady(ready);
        appendSys("Bạn " + (ready ? "đã sẵn sàng." : "đã bỏ sẵn sàng."));
    }
    private void sortMyHand(){
        myHand.sort(TIEN_LEN_ORDER);
        renderHands();
    }
    private void playSelected(){
        List<Integer> idxs = new ArrayList<>();
        for (int i=0;i<myCardButtons.size();i++) if (myCardButtons.get(i).isSelected()) idxs.add(i);
        if (idxs.isEmpty()) { appendSys("Chưa chọn lá nào để đánh."); return; }
        List<Card> selected = idxs.stream().map(myHand::get).collect(Collectors.toList());
        lastPlayed = new ArrayList<>(selected);
        idxs.sort(Comparator.reverseOrder());
        for (int idx : idxs) myHand.remove(idx);
        south.setCount(myHand.size());
        renderHands();
        renderLastPlayed();
        appendChat("Bạn", "Đánh " + selected.stream().map(Card::text).collect(Collectors.joining(" ")));
    }
    private void passTurn(){ appendChat("Bạn", "Bỏ lượt."); }

    // ====== Chat ======
    private void sendChat(){
        String t = chatInput.getText().trim();
        if (t.isEmpty()) return;
        chatInput.setText("");
        appendChat("Bạn", t);
    }
    private void appendChat(String from, String text){
        chatArea.append(from + ": " + text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    private void appendSys(String text){
        chatArea.append("[Hệ thống] " + text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // ====== Run độc lập ======
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new GameTableFrame().setVisible(true);
        });
    }
}
