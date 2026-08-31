package scanner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Simple host reachability check, run before a port scan so the tool can
 * report "host down" instead of a long list of closed/timed-out ports.
 */
public class HostDiscovery {

    /**
     * Returns true if the host responds to InetAddress.isReachable() within
     * the timeout. Note: on many systems this requires ICMP (ping) privileges
     * to work as an actual ping — without them, Java falls back to a TCP
     * echo attempt on port 7. Either way, a false result here doesn't
     * guarantee the host is truly down (it may just be blocking ICMP/echo),
     * so callers should treat "unreachable" as advisory, not conclusive.
     */
    public static boolean isReachable(String host, int timeoutMs) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeoutMs);
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static String resolveHostname(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
