package scanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Well-known port -> service name lookup, used to label scan results.
 */
public class CommonPorts {
    private static final Map<Integer, String> PORT_SERVICES = new HashMap<>();

    static {
        PORT_SERVICES.put(21, "FTP");
        PORT_SERVICES.put(22, "SSH");
        PORT_SERVICES.put(23, "Telnet");
        PORT_SERVICES.put(25, "SMTP");
        PORT_SERVICES.put(53, "DNS");
        PORT_SERVICES.put(80, "HTTP");
        PORT_SERVICES.put(110, "POP3");
        PORT_SERVICES.put(143, "IMAP");
        PORT_SERVICES.put(443, "HTTPS");
        PORT_SERVICES.put(445, "SMB");
        PORT_SERVICES.put(3306, "MySQL");
        PORT_SERVICES.put(3389, "RDP");
        PORT_SERVICES.put(5432, "PostgreSQL");
        PORT_SERVICES.put(6379, "Redis");
        PORT_SERVICES.put(8080, "HTTP-Alt");
        PORT_SERVICES.put(8443, "HTTPS-Alt");
        PORT_SERVICES.put(27017, "MongoDB");
    }

    public static String lookup(int port) {
        return PORT_SERVICES.getOrDefault(port, "unknown");
    }
}
