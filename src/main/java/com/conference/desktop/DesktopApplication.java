package com.conference.desktop;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JMenuItem;
import javax.swing.JWindow;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class DesktopApplication {
    private static final int DEFAULT_PORT = 9900;
    private static final String PORT_KEY = "serverPort";

    private final Preferences preferences = Preferences.userNodeForPackage(DesktopApplication.class);
    private final JFrame frame = new JFrame("视频会议服务");
    private final JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_PORT, 1, 65535, 1));
    private final JLabel statusLabel = new JLabel();
    private final JButton startButton = new JButton("启动服务");
    private final JButton stopButton = new JButton("停止服务");
    private final JMenuItem trayStartItem = new JMenuItem("启动");
    private final JMenuItem trayStopItem = new JMenuItem("停止");
    private final JPopupMenu trayMenu = new JPopupMenu();
    private final JWindow trayMenuHost = new JWindow();

    private final ServerController serverController;
    private TrayIcon trayIcon;

    private DesktopApplication(String[] args) {
        serverController = new ServerController(args, this::stateChanged, this::showStartError);
    }

    public static void launch(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            DesktopApplication application = new DesktopApplication(args);
            application.initialize();
        });
    }

    private void initialize() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "当前系统不支持任务栏托盘。", "无法启动",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        int savedPort = preferences.getInt(PORT_KEY, DEFAULT_PORT);
        portSpinner.setValue(savedPort);
        createWindow();
        createTrayIcon();
        stateChanged(ServerController.State.STOPPED);
        frame.setVisible(true);
        startServer();
    }

    private void createWindow() {
        JLabel title = new JLabel("视频会议服务管理");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel hint = new JLabel("设置 HTTPS 服务端口；修改端口后需停止并重新启动。");
        hint.setForeground(new Color(90, 98, 108));

        JPanel portPanel = new JPanel();
        portPanel.add(new JLabel("服务端口："));
        portPanel.add(portSpinner);

        JPanel actionPanel = new JPanel();
        actionPanel.add(startButton);
        actionPanel.add(stopButton);

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        title.setAlignmentX(0.5f);
        hint.setAlignmentX(0.5f);
        portPanel.setAlignmentX(0.5f);
        statusLabel.setAlignmentX(0.5f);
        actionPanel.setAlignmentX(0.5f);
        content.add(title);
        content.add(javax.swing.Box.createVerticalStrut(10));
        content.add(hint);
        content.add(javax.swing.Box.createVerticalStrut(18));
        content.add(portPanel);
        content.add(javax.swing.Box.createVerticalStrut(12));
        content.add(statusLabel);
        content.add(javax.swing.Box.createVerticalStrut(16));
        content.add(actionPanel);

        startButton.addActionListener(event -> startServer());
        stopButton.addActionListener(event -> serverController.stop());

        frame.setContentPane(content);
        frame.setIconImage(createApplicationIcon(64));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                hideToTray();
            }
        });
        frame.pack();
        frame.setSize(760, 340);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    private void createTrayIcon() {
        JMenuItem openWindowItem = new JMenuItem("打开主界面");
        JMenuItem exitItem = new JMenuItem("退出");

        openWindowItem.addActionListener(event -> showWindow());
        trayStartItem.addActionListener(event -> startServer());
        trayStopItem.addActionListener(event -> serverController.stop());
        exitItem.addActionListener(event -> exitApplication());

        trayMenu.add(openWindowItem);
        trayMenu.addSeparator();
        trayMenu.add(trayStartItem);
        trayMenu.add(trayStopItem);
        trayMenu.addSeparator();
        trayMenu.add(exitItem);
        trayMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                trayMenuHost.setVisible(false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent event) {
                trayMenuHost.setVisible(false);
            }
        });

        trayIcon = new TrayIcon(createApplicationIcon(32), "视频会议服务");
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(event -> SwingUtilities.invokeLater(this::showWindow));
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    showTrayMenu(event.getXOnScreen(), event.getYOnScreen());
                }
            }
        });
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException exception) {
            JOptionPane.showMessageDialog(frame, "无法创建任务栏托盘：" + exception.getMessage(),
                    "启动失败", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void showTrayMenu(int x, int y) {
        SwingUtilities.invokeLater(() -> {
            trayMenuHost.setAlwaysOnTop(true);
            trayMenuHost.setBounds(x, y, 1, 1);
            trayMenuHost.setVisible(true);
            trayMenu.show(trayMenuHost.getContentPane(), 0, 0);
        });
    }

    private void startServer() {
        int port = (Integer) portSpinner.getValue();
        preferences.putInt(PORT_KEY, port);
        serverController.start(port);
    }

    private void stateChanged(ServerController.State state) {
        SwingUtilities.invokeLater(() -> {
            switch (state) {
                case STOPPED -> statusLabel.setText("状态：已停止");
                case STARTING -> statusLabel.setText("状态：正在启动…");
                case RUNNING -> {
                    String address = accessAddress();
                    statusLabel.setText("状态：运行中　访问地址：" + address);
                    if (trayIcon != null) {
                        trayIcon.displayMessage("视频会议服务已启动", "访问地址：" + address,
                                TrayIcon.MessageType.INFO);
                    }
                }
                case STOPPING -> statusLabel.setText("状态：正在停止…");
                case FAILED -> statusLabel.setText("状态：启动失败");
            }
            boolean canStart = state == ServerController.State.STOPPED || state == ServerController.State.FAILED;
            boolean running = state == ServerController.State.RUNNING;
            startButton.setEnabled(canStart);
            stopButton.setEnabled(running);
            portSpinner.setEnabled(canStart);
            trayStartItem.setEnabled(canStart);
            trayStopItem.setEnabled(running);
            if (trayIcon != null) {
                trayIcon.setToolTip("视频会议服务 - " + (running ? "运行中" : "已停止"));
            }
        });
    }

    private String accessAddress() {
        return "https://" + localAddress() + ":" + portSpinner.getValue() + "/#/im";
    }

    private static String localAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
            // Fall back to localhost when network information is unavailable.
        }
        return "localhost";
    }

    private void showStartError(String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                "服务启动失败：" + message + "\n请检查端口是否被占用，并查看 logs 目录中的日志。",
                "启动失败", JOptionPane.ERROR_MESSAGE));
    }

    private void hideToTray() {
        frame.setVisible(false);
        trayIcon.displayMessage("视频会议服务", "程序仍在后台运行，可通过托盘图标重新打开。",
                TrayIcon.MessageType.INFO);
    }

    private void showWindow() {
        frame.setVisible(true);
        frame.setState(JFrame.NORMAL);
        frame.toFront();
    }

    private void exitApplication() {
        trayStartItem.setEnabled(false);
        trayStopItem.setEnabled(false);
        serverController.shutdown(() -> {
            if (trayIcon != null) {
                SystemTray.getSystemTray().remove(trayIcon);
            }
            System.exit(0);
        });
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing's default look and feel remains usable.
        }
    }

    private static Image createApplicationIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(45, 108, 223));
        graphics.fillRoundRect(1, 1, size - 2, size - 2, size / 4, size / 4);
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(size / 5, size / 3, size / 2, size / 3, size / 10, size / 10);
        int[] x = {size * 7 / 10, size * 9 / 10, size * 9 / 10, size * 7 / 10};
        int[] y = {size * 5 / 12, size / 3, size * 2 / 3, size * 7 / 12};
        graphics.fillPolygon(x, y, 4);
        graphics.setColor(new Color(45, 108, 223));
        graphics.setStroke(new BasicStroke(Math.max(1f, size / 18f)));
        graphics.drawLine(size / 3, size / 2, size / 2, size / 2);
        graphics.dispose();
        return image;
    }
}
