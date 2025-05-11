package Utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class WindowsSigintSender {
    

    public static void sendCtrlC(int processGroupId) {
        // 0 = CTRL_C_EVENT
        boolean result = Kernel32.INSTANCE.GenerateConsoleCtrlEvent(0, processGroupId);
        if (!result) {
            System.err.println("Failed to send Ctrl+C");
        }
    }

    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "your-app.jar");
        pb.inheritIO(); // optional: to link IO with parent console
        Process process = pb.start();

        // Give the app a few seconds to start
        Thread.sleep(5000);

        // Send Ctrl+C (SIGINT equivalent)
        sendCtrlC((int) process.pid());
    }
}
