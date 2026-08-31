package scanner;

/**
 * Immutable result of scanning a single port on a host.
 */
public class ScanResult {
    private final String host;
    private final int port;
    private final boolean open;
    private final String service;
    private final String banner;
    private final long responseTimeMs;

    public ScanResult(String host, int port, boolean open, String service, String banner, long responseTimeMs) {
        this.host = host;
        this.port = port;
        this.open = open;
        this.service = service;
        this.banner = banner;
        this.responseTimeMs = responseTimeMs;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public boolean isOpen() { return open; }
    public String getService() { return service; }
    public String getBanner() { return banner; }
    public long getResponseTimeMs() { return responseTimeMs; }

    @Override
    public String toString() {
        if (!open) {
            return String.format("%s:%d  CLOSED", host, port);
        }
        String bannerPart = (banner != null && !banner.isEmpty()) ? "  banner=\"" + banner + "\"" : "";
        return String.format("%s:%-6d OPEN   service=%-12s (%dms)%s",
                host, port, service, responseTimeMs, bannerPart);
    }
}
