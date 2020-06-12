import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class InfoWindow extends JFrame {

    private final JCheckBox mSoftRebootCheckBox;
    //左边的区域
    JScrollPane jScrollPane;
    private List<String> mDevices;
    //第一行
    private JLabel mTestInfoTtile;
    private JLabel mTestSettingTitle;
    //一横一竖分隔线
    private JPanel mHorizontalSeperator;
    private JPanel mVerticalSeperator;
    private JTextPane mInfoTextPane;

    //右边的区域
    private JLabel mSelectDeviceTitleLable;
    private JComboBox<String> mTargetDeviceBox;
    private JButton mRefreshDeviceListButton;

    private JLabel mTestTimeTitleLabel;
    private JTextField mTestTimeField;
    private JButton mTestStartButton;

    private JLabel  mDeviceLogPathLabel;
    private JTextField mDeviceLogPathFiled;

    private JLabel mLogSavePathTitleLabel;
    private JTextField mLogSavePathFiled;
    private JButton mOpenChooserButton;


    private StartListener startListener;
    private ActionListener mStartActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            boolean start = mTestStartButton.getText().equals("开始");

            if (startListener == null || !startListener.onStartAction(start)) {
                return;
            }

            if (start) {
                mTestTimeField.setEnabled(false);
                mLogSavePathFiled.setEnabled(false);
                mTargetDeviceBox.setEnabled(false);
                mDeviceLogPathFiled.setEnabled(false);
                mOpenChooserButton.setEnabled(false);
                mRefreshDeviceListButton.setEnabled(false);
                mSoftRebootCheckBox.setEnabled(false);
                mTestStartButton.setText("停止");
            } else {
                mTestTimeField.setEnabled(true);
                mLogSavePathFiled.setEnabled(true);
                mTargetDeviceBox.setEnabled(true);
                mDeviceLogPathFiled.setEnabled(true);
                mOpenChooserButton.setEnabled(true);
                mRefreshDeviceListButton.setEnabled(true);
                mSoftRebootCheckBox.setEnabled(true);
                mTestStartButton.setText("开始");
            }
        }
    };

    public InfoWindow() {

        setTitle("NTP时间测试工具");
        setBackground(Color.white);

        this.setResizable(true);
        this.setSize(1080, 720);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(null);

        mTestInfoTtile = new JLabel("测试信息");
        mTestInfoTtile.setFont(new Font("微软雅黑", Font.BOLD, 20));
        mTestInfoTtile.setBounds(40, 40, 80, 25);

        mTestSettingTitle = new JLabel("测试设置");
        mTestSettingTitle.setFont(new Font("微软雅黑", Font.BOLD, 20));
        mTestSettingTitle.setBounds(542, 40, 80, 25);

        mHorizontalSeperator = new JPanel();
        mHorizontalSeperator.setBackground(Color.GRAY);
        mHorizontalSeperator.setBounds(40, 76, 1025, 1);

        mVerticalSeperator = new JPanel();
        mVerticalSeperator.setBackground(Color.GRAY);
        mVerticalSeperator.setBounds(540, 76, 1, 650);


        mInfoTextPane = new JTextPane();
        mInfoTextPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        mInfoTextPane.setBackground(new Color(220, 220, 220, 220));
        mInfoTextPane.setEditable(false);
        jScrollPane = new JScrollPane(mInfoTextPane);
        jScrollPane.setBorder(null);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setBounds(40, 90, 480, 575);
        jScrollPane.setBackground(new Color(220, 220, 220, 220));


        JPopupMenu pm = new JPopupMenu();

        JMenuItem m1 = new JMenuItem("清空日志");

        m1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });

        MouseAdapter adapter = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    pm.add(m1);
                    pm.show(mInfoTextPane, e.getX(), e.getY());
                }
            }
        };

        mInfoTextPane.addMouseListener(adapter);

        mSelectDeviceTitleLable = new JLabel("目标设备");
        mSelectDeviceTitleLable.setBounds(570, 100, 200, 35);
        mTargetDeviceBox = new JComboBox<String>();
        mTargetDeviceBox.setBounds(660, 100, 260, 35);
        mRefreshDeviceListButton = new JButton("刷新");
        mRefreshDeviceListButton.setBounds(960, 100, 100, 35);
        mRefreshDeviceListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> devices = DeviceTester.getAllDevices();
                setDevices(devices);
            }
        });

        mDeviceLogPathLabel = new JLabel("设备日志路径:");
        mDeviceLogPathLabel.setBounds(570, 160, 200, 35);
        mDeviceLogPathFiled = new JTextField();
        mDeviceLogPathFiled.setText("/sdcard/com.ayst.androidx");
        mDeviceLogPathFiled.setBounds(660, 160, 260, 35);
        mSoftRebootCheckBox = new JCheckBox("命令重启");
        mSoftRebootCheckBox.setBounds(960, 160, 260, 35);
        //todo 暂时关闭软重启，加了软重启之后，停止测试无法立刻停止上一次的重启逻辑，还是会重启一次才能恢复正常，暂时没时间解决这个问题
        //jPanel.add(mSoftRebootCheckBox);


        mLogSavePathTitleLabel = new JLabel("日志保存路径:");
        mLogSavePathTitleLabel.setBounds(570, 220, 200, 35);
        mLogSavePathFiled = new JTextField();
        mLogSavePathFiled.setBounds(660, 220, 260, 35);
        mOpenChooserButton = new JButton("选择");
        mOpenChooserButton.setBounds(960, 220, 100, 35);

        mTestTimeTitleLabel = new JLabel("测试次数:");
        mTestTimeTitleLabel.setBounds(570, 280, 200, 35);
        mTestTimeField = new JTextField("100");
        mTestTimeField.setDocument(new NumberDocument());
        mTestTimeField.setBounds(660, 280, 260, 35);
        mTestStartButton = new JButton("开始");
        mTestStartButton.setBounds(960, 280, 100, 35);
        mTestStartButton.addActionListener(mStartActionListener);


        mOpenChooserButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser mLogSavePathChooser = new JFileChooser();
                mLogSavePathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                mLogSavePathChooser.setBackground(Color.WHITE);
                mLogSavePathChooser.setMultiSelectionEnabled(false);
                mLogSavePathChooser.setBounds(120, 0, 200, 35);
                mLogSavePathChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                mLogSavePathChooser.showDialog(mLogSavePathFiled, "确定");

                File file = mLogSavePathChooser.getSelectedFile();

                if (file != null) {
                    if (!file.exists() || !file.canWrite()) {
                        System.out.println("日志文件路径无效 : " + file.getAbsolutePath());
                    } else {
                        mLogSavePathFiled.setText(file.getAbsolutePath());
                    }
                }

            }
        });

        jPanel.add(mTestInfoTtile);
        jPanel.add(mTestSettingTitle);
        jPanel.add(mHorizontalSeperator);
        jPanel.add(mVerticalSeperator);
        jPanel.add(jScrollPane);

        jPanel.add(mSelectDeviceTitleLable);
        jPanel.add(mTargetDeviceBox);
        jPanel.add(mRefreshDeviceListButton);

        jPanel.add(mDeviceLogPathLabel);
        jPanel.add(mDeviceLogPathFiled);

        jPanel.add(mTestStartButton);
        jPanel.add(mTestTimeTitleLabel);
        jPanel.add(mTestTimeField);
        jPanel.add(mLogSavePathFiled);
        jPanel.add(mLogSavePathTitleLabel);
        jPanel.add(mOpenChooserButton);

        add(jPanel);
    }

    public void addInfo(String info) {
        Main.getUiThread().post(new Runnable() {
            @Override
            public void run() {
                insertInfo(info, 12, Color.black);
            }
        });
    }

    public void addAlarmInfo(String info, boolean positive) {
        Main.getUiThread().post(new Runnable() {
            @Override
            public void run() {
                insertInfo(info, 13, positive ? Color.blue : Color.red);
            }
        });
    }

    private void insertInfo(String content, int size, Color color) {
        if (!content.endsWith("\r\n")) {
            content = content.concat("\r\n");
        }
        Document doc = mInfoTextPane.getStyledDocument();
        MutableAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, color);//设置文字颜色
        StyleConstants.setFontSize(set, size);//设置字体大小

        try {
            doc.insertString(doc.getLength(), content, set);//插入文字
        } catch (BadLocationException e) {

        }
        if (jScrollPane.getVerticalScrollBar() != null) {
            //todo 手动指定高度刷新，笔面试闪屏，后续可根据内容高度来灵活计算，避免布局改动后，无效或产生bug
            if (jScrollPane.getVerticalScrollBar().getMaximum() > 575) {
                jScrollPane.getViewport().setViewPosition(new Point(0, jScrollPane.getVerticalScrollBar().getMaximum() + 100));
            }
        }
    }

    public void clearLog() {
        mInfoTextPane.setDocument(new DefaultStyledDocument());
    }

    public void setDevices(List<String> devices) {
        if (devices != null) {
            mDevices = devices;
            for (String device : devices) {
                mTargetDeviceBox.addItem(device);
            }
        }
    }

    public String getTargetDevice() {
        if (mDevices != null) {
            return mDevices.get(mTargetDeviceBox.getSelectedIndex());
        }
        return null;
    }

    public void setStartActionListener(StartListener listener) {
        this.startListener = listener;
    }

    public boolean useSoftReboot() {
        return mSoftRebootCheckBox.isSelected();
    }

    /**
     * 创建并显示GUI。出于线程安全的考虑，
     * 这个方法在事件调用线程中调用。
     */
    public void createAndShowGUI() {

        GlobalConfig config = GlobalConfig.getInstance();

        if (config.getDeviceLogPath() != null) {
            mDeviceLogPathFiled.setText(config.getDeviceLogPath());
        }
        mLogSavePathFiled.setText(config.getLogSavePath());
        mTestTimeField.setText(config.getTestTime() + "");

        setVisible(true);
    }

    public String getLogSavePath() {
        return mLogSavePathFiled.getText();
    }

    public int getTestTime() {
        return Integer.parseInt(mTestTimeField.getText());
    }

    public String getDeviceLogPath() {
        return mDeviceLogPathFiled.getText();
    }

    public interface StartListener {
        boolean onStartAction(boolean start);
    }
}
