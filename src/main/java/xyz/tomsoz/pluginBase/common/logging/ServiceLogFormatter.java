package xyz.tomsoz.pluginBase.common.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Created on 28/04/2025
 *
 * @author Preva1l
 */
@SuppressWarnings("unused")
public class ServiceLogFormatter extends SimpleFormatter {
    private final static String FORMAT = "[%tT %s]: [%s] [Services] [%s] %s%s";
    private final static String ANONYMOUS_SERVICE = "Anonymous Service";
    private final static ZoneId ZONE_ID = ZoneId.systemDefault();

    private final String rootLoggerName;

    public ServiceLogFormatter(String rootLoggerName) {
        this.rootLoggerName = rootLoggerName;
    }

    public static ConsoleHandler asConsoleHandler(String rootLoggerName) {
        return asConsoleHandler(false, rootLoggerName);
    }

    public static ConsoleHandler asConsoleHandler(boolean raw, String rootLoggerName) {
        ConsoleHandler handler = raw ? new RawLogger() : new ConsoleHandler();
        handler.setFormatter(new ServiceLogFormatter(rootLoggerName));
        return handler;
    }

    @Override
    public String format(LogRecord record) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(record.getInstant(), ZONE_ID);

        var sourceClass = record.getSourceClassName();
        String service = (sourceClass != null) ? sourceClass.substring(sourceClass.lastIndexOf('.') + 1) : ANONYMOUS_SERVICE;

        String message = formatMessage(record);

        String throwable = "";
        var thrown = record.getThrown();
        if (thrown != null) {
            var sw = new StringWriter(1024);
            var pw = new PrintWriter(sw);
            pw.println();
            thrown.printStackTrace(pw);
            pw.flush();
            throwable = sw.toString();
        }

        return FORMAT.formatted(
                zdt,
                switch (record.getLevel().getName()) {
                    case "SEVERE" -> "ERROR";
                    case "WARNING" -> "WARN";
                    case "INFO" -> "INFO";
                    case "CONFIG" -> "DEBUG";
                    default -> "TRACE";
                },
                rootLoggerName,
                service,
                message,
                throwable
        );
    }
}