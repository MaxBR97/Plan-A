package Utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Kernel32 extends Library {
    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
    boolean GenerateConsoleCtrlEvent(int dwCtrlEvent, int dwProcessGroupId);
}
