package client;


import javax.swing.*;
import client.ui.LoginFrame;


public class ClientApp {
public static void main(String[] args) {
SwingUtilities.invokeLater(() -> {
try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
new LoginFrame().setVisible(true);
});
}
}