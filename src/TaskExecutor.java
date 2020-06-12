import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class TaskExecutor {

    public BlockingDeque<Runnable> runnables = new LinkedBlockingDeque<>();
    private boolean mFlag;

    public void post(Runnable runnable) {
        runnables.offer(runnable);
    }

    public void start() {
        mFlag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlag) {
                    Runnable runnable = null;
                    try {
                        runnable = runnables.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        }).start();
    }

    public void stop() {
        mFlag = false;
    }
}
