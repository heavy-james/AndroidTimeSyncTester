import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Main {

    static InfoWindow infoWindow;
    static DeviceTester deviceTester;
    static TaskExecutor taskExecutor;
    static TaskExecutor promptTasksExecutor;
    static TaskExecutor uiThread;
    static String mToastTitle = "操作提示";
    static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    private static JDialog mStopPowerApplyDialog;
    private static JLabel mPowerDialogMessageLabel;

    public static void main(String[] args) {


        infoWindow = new InfoWindow();

        infoWindow.createAndShowGUI();

        promptTasksExecutor = new TaskExecutor();

        promptTasksExecutor.start();

        uiThread = new TaskExecutor();

        uiThread.start();

        infoWindow.setStartActionListener(new InfoWindow.StartListener() {

            private Boolean mStarted;

            @Override
            public boolean onStartAction(boolean start) {
                if (start) {

                    final String sn = infoWindow.getTargetDevice();

                    if (sn == null) {
                        JOptionPane.showMessageDialog(infoWindow, "请选则目标设备", mToastTitle, JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    final String deviceLogPath = infoWindow.getDeviceLogPath();

                    if (deviceLogPath == null) {
                        JOptionPane.showMessageDialog(infoWindow, "请选则设备日志路径", mToastTitle, JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    final String mLogSavePath = infoWindow.getLogSavePath();

                    if (mLogSavePath == null) {
                        JOptionPane.showMessageDialog(infoWindow, "请选则日志保存路径", mToastTitle, JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    final int mRepeatTime = infoWindow.getTestTime();

                    if (mRepeatTime <= 0) {
                        JOptionPane.showMessageDialog(infoWindow, "请填写正确的重试次数", mToastTitle, JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                    mStarted = true;

                    deviceTester = new DeviceTester(sn);

                    taskExecutor = new TaskExecutor();

                    taskExecutor.post(new Runnable() {
                        @Override
                        public void run() {
                            //保存启动配置到缓存，避免多次设置
                            GlobalConfig config = GlobalConfig.getInstance();
                            config.setTestTime(mRepeatTime);
                            config.setDeviceLogPath(deviceLogPath);
                            config.setLogSavePath(mLogSavePath);
                            GlobalConfig.getInstance().store();

                            int repeat = 0;
                            //根据最大重试次数运行测试
                            while (repeat < mRepeatTime && mStarted) {

                                infoWindow.addInfo("正在检测设备...");
                                while (!deviceTester.detectDevice() && mStarted) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //一分钟内,每秒检测时间是否同步
                                for (int i = 0; i < 60; i++) {

                                    if (!mStarted) {
                                        break;
                                    }

                                    long myTime = System.currentTimeMillis();
                                    long deviceTime = deviceTester.getDeviceTime();

                                    String myTimeString = dateFormatter.format(myTime);
                                    String deviceTimeString = dateFormatter.format(deviceTime);

                                    String info = "第 " + repeat + "测试; 第" + i + "次同步时间";

                                    if (Math.abs(myTime - deviceTime) > 60 * 1000) {
                                        infoWindow.addInfo(info + "失败!\r\n当前时间 : " + myTimeString + "\r\n设备时间 : " + deviceTimeString);

                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        if (i == 59) {
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                            String logPath = mLogSavePath + File.separator + simpleDateFormat.format(System.currentTimeMillis());
                                            infoWindow.addAlarmInfo("本次测试未通过", false);
                                            if (deviceTester.pullLog(deviceLogPath, logPath)) {
                                                infoWindow.addAlarmInfo("已保存日志到: " + logPath, false);
                                            } else {
                                                infoWindow.addAlarmInfo("日志保存失败", false);
                                            }
                                        }
                                    } else {
                                        infoWindow.addAlarmInfo(info + "成功!\r\n当前时间 : " + myTimeString + "\r\n设备时间 : " + deviceTimeString, true);
                                        break;
                                    }
                                }

                                waitUntilDeviceRebooted();

                                repeat++;
                            }
                            if (mStarted) {
                                infoWindow.addInfo("测试已完成");
                                mStarted = false;
                            }
                        }
                    });

                    taskExecutor.start();

                } else {
                    mStarted = false;
                    infoWindow.addInfo("已停止测试");
                    taskExecutor.stop();
                    return true;
                }
                return true;
            }

            private void waitUntilDeviceRebooted() {
                if (deviceTester == null) {
                    return;
                }

                if (mStarted) {
                    if (mStopPowerApplyDialog == null) {
                        mStopPowerApplyDialog = createPowerPromptDialog();
                    }
                    String rebootMessage = infoWindow.useSoftReboot() ? "正在重启设备" : "请断开设备电源";
                    mPowerDialogMessageLabel.setText(rebootMessage);
                    promptTasksExecutor.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mStopPowerApplyDialog.isShowing()) {
                                mStopPowerApplyDialog.setVisible(true);
                            }
                        }
                    });
                }

                if (mStarted && infoWindow.useSoftReboot() && deviceTester.detectDevice()) {
                    deviceTester.reboot();
                }

                while (deviceTester.detectDevice() && mStarted) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (mStarted) {
                    if (mStopPowerApplyDialog.isShowing()) {
                        mStopPowerApplyDialog.setVisible(false);
                    }
                    infoWindow.addInfo(infoWindow.useSoftReboot() ? "重启成功" : "断开设备电源成功");
                }
            }
        });
    }


    private static JDialog createPowerPromptDialog() {

        final JDialog jDialog = new JDialog(infoWindow, mToastTitle);

        JPanel jPanel = new JPanel();
        jPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setVgap(35);
        flowLayout.setHgap(100);
        flowLayout.setAlignment(FlowLayout.CENTER);
        jPanel.setLayout(flowLayout);

        mPowerDialogMessageLabel = new JLabel();
        mPowerDialogMessageLabel.setSize(70, 50);
        mPowerDialogMessageLabel.setLocation(100, 60);
        jPanel.add(mPowerDialogMessageLabel);

        JButton button = new JButton("确定");
        jPanel.add(button);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jDialog.isShowing()) {
                    jDialog.setVisible(false);
                }
            }
        });
        jDialog.add(jPanel);

        Rectangle rectangle = infoWindow.getBounds();

        int width = 200;
        int height = 160;
        int x = rectangle.x + (rectangle.width - width) / 2;
        int y = rectangle.y + (rectangle.height - height) / 2;

        jDialog.setBounds(x, y, width, height);
        return jDialog;
    }

    public static TaskExecutor getUiThread() {
        return uiThread;
    }
}
