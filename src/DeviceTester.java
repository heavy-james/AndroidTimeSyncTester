import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DeviceTester {

    String sn;
    boolean mPullResult;
    private long deviceTime;

    public DeviceTester(String sn) {
        this.sn = sn;
    }

    public static List<String> getAllDevices() {

        String cmd = "adb devices";

        final List<String> devices = new ArrayList<>();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ProcessReader reader = new ProcessReader(cmd, new ProcessReader.Callback() {
            @Override
            public void onReadLine(String line) {
                if (!line.contains("List") && line.contains("device")) {
                    String deviceSn = line.substring(0, line.indexOf("device") - 1);
                    devices.add(deviceSn);
                }
            }

            @Override
            public void onFinished() {
                countDownLatch.countDown();
            }
        });

        reader.execute();

        return devices;
    }

    /**
     * 检测是否有设备连接
     *
     * @return
     */
    public boolean detectDevice() {

        List<String> devices = getAllDevices();

        if (devices.contains(sn)) {
            return true;
        }
        return false;
    }

    public long getDeviceTime() {

        deviceTime = 0;

        String cmd = getAdbCommand() + " shell date +%s";

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ProcessReader reader = new ProcessReader(cmd, new ProcessReader.Callback() {
            @Override
            public void onReadLine(String line) {
                try {
                    deviceTime = Long.parseLong(line) * 1000;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinished() {
                countDownLatch.countDown();
            }
        });

        reader.execute();

        return deviceTime;
    }

    public boolean reboot() {

        String command = getAdbCommand() + " reboot";

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ProcessReader reader = new ProcessReader(command, new ProcessReader.Callback() {
            @Override
            public void onReadLine(String line) {

            }

            @Override
            public void onFinished() {
                countDownLatch.countDown();
            }
        });

        reader.execute();

        return false;
    }

    public synchronized boolean pullLog(String logPath, String savePath) {

        mPullResult = false;

        if (!logPath.endsWith("/")) {
            logPath = logPath.concat("/");
        }

        File file = new File(savePath);

        if (!file.exists()) {
            file.mkdirs();
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);

        String cmd = getAdbCommand() + " pull " + logPath + " " + file.getAbsoluteFile().getAbsolutePath();

        ProcessReader processReader = new ProcessReader(cmd, new ProcessReader.Callback() {
            @Override
            public void onReadLine(String line) {
                System.out.println(line);
                mPullResult = line.contains("files pulled");
            }

            @Override
            public void onFinished() {
                countDownLatch.countDown();
            }
        });

        processReader.execute();

        return mPullResult;
    }


    private String getAdbCommand() {
        return "adb -s " + sn;
    }
}
