import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessReader {

    private String cmd;

    private Callback callback;

    public ProcessReader(String command, Callback callback) {
        this.cmd = command;
        this.callback = callback;
    }

    public void execute() {

        try {
            Process process = Runtime.getRuntime().exec(cmd);

            InputStream inputStream = process.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            do {
                line = bufferedReader.readLine();

                if (line != null) {
                    callback.onReadLine(line);
                }
            } while (line != null);

            if (process.isAlive()) {
                process.waitFor();
            }

            callback.onFinished();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public interface Callback {
        void onReadLine(String line);

        void onFinished();
    }

}
