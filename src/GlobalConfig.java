import java.io.*;
import java.util.Properties;

public class GlobalConfig extends Properties {

    private static final String KEY_DEVICE_LOG_PATH = "KEY_DEVICE_LOG_PATH";
    private static final String KEY_LOG_SAVE_PATH = "KEY_LOG_SAVE_PATH";
    private static final String KEY_TEST_TIME = "KEY_TEST_TIME";
    private static final String KEY_TARGET_DEVICE = "KEY_TARGET_DEVICE";
    private static final String CACHE_FILE = "ntp_time_test.cache";
    private static GlobalConfig instance;
    private String deviceLogPath;
    private String logSavePath;
    private int testTime;
    private File mPropertyFile;

    private GlobalConfig() {
        File file = new File(System.getProperty("user.dir"));
        mPropertyFile = new File(file.getParent(), CACHE_FILE);

        if (mPropertyFile.exists() && mPropertyFile.canRead()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(mPropertyFile);
                load(inputStream);
                testTime = Integer.parseInt(getProperty(KEY_TEST_TIME));
                logSavePath = getProperty(KEY_LOG_SAVE_PATH);
                deviceLogPath = getProperty(KEY_DEVICE_LOG_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static synchronized GlobalConfig getInstance() {
        if (instance == null) {
            instance = new GlobalConfig();
        }
        return instance;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public String getLogSavePath() {
        return logSavePath;
    }

    public void setLogSavePath(String logSavePath) {
        this.logSavePath = logSavePath;
    }

    public String getDeviceLogPath() {
        return deviceLogPath;
    }

    public void setDeviceLogPath(String deviceLogPath) {
        this.deviceLogPath = deviceLogPath;
    }


    public void store() {

        if (mPropertyFile == null) {
            File file = new File(System.getProperty("user.dir"));
            mPropertyFile = new File(file.getParent(), CACHE_FILE);
        }

        if (!mPropertyFile.exists()) {
            try {
                mPropertyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!mPropertyFile.exists() || !mPropertyFile.canWrite()) {
            return;
        }

        setProperty(KEY_DEVICE_LOG_PATH, getDeviceLogPath());
        setProperty(KEY_LOG_SAVE_PATH, getLogSavePath());
        setProperty(KEY_TEST_TIME, getTestTime() + "");

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(mPropertyFile);
            store(outputStream, "NTP Time Tester cache, please do not edit manually");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
