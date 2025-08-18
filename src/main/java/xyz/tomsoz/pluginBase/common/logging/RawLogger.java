package xyz.tomsoz.pluginBase.common.logging;


import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

/**
 * Paper has a very odd logging system so this bypasses that.
 * <p>
 *
 * @author Preva1l
 */
public class RawLogger extends ConsoleHandler {
    private static final String ANSI_RESET = "\u001B[m";
    private static final String ANSI_ERROR = "\u001B[31;1m"; // Red
    private static final String ANSI_FATAL = "\u001B[91;1m"; // Bright Red
    private static final String ANSI_WARN = "\u001B[33;1m"; // Yellow
    private static final String ANSI_DEBUG = "\u001B[36;1m"; // Cyan

    @Override
    public void publish(LogRecord record) {
        PrintStream rawOut = new PrintStream(new FileOutputStream(FileDescriptor.out));
        String ANSI_COLOR = switch (record.getLevel().getName()) {
            case "WARNING" -> ANSI_WARN;
            case "SEVERE" -> ANSI_ERROR;
            case "CONFIG" -> ANSI_DEBUG;
            case "FATAL" -> ANSI_FATAL;
            default -> "";
        };
        rawOut.println(ANSI_COLOR + getFormatter().format(record) + ANSI_RESET);
    }
}
