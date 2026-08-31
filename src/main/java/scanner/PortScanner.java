package scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Multi-threaded TCP port scanner. Attempts a socket connection to each
 * port in the given range; on success, attempts a lightweight banner
 * grab (reads whatever the service sends first, with a short timeout —
 * doesn't send any payload, so it won't trigger anything a plain TCP
 * connect wouldn't already trigger).
 */
public class PortScanner {

    private final int connectTimeoutMs;
    private final int bannerTimeoutMs;
    private final int threadPoolSize;

    public PortScanner(int connectTimeoutMs, int bannerTimeoutMs, int threadPoolSize) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.bannerTimeoutMs = bannerTimeoutMs;
        this.threadPoolSize = threadPoolSize;
    }

    public PortScanner() {
        this(300, 500, 100);
    }

    /**
     * Scans all ports in [startPort, endPort] (inclusive) on the given host.
     * Returns only OPEN ports by default use scanRange(..., true) for full results.
     */
    public List<ScanResult> scanRange(String host, int startPort, int endPort, boolean includeClosedInResult)
            throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<ScanResult>> futures = new ArrayList<>();

        for (int port = startPort; port <= endPort; port++) {
            final int p = port;
            futures.add(pool.submit(() -> scanPort(host, p)));
        }

        List<ScanResult> results = new ArrayList<>();
        for (Future<ScanResult> future : futures) {
            try {
                ScanResult result = future.get();
                if (result.isOpen() || includeClosedInResult) {
                    results.add(result);
                }
            } catch (ExecutionException e) {
                // individual port scan failed unexpectedly — skip it
            }
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        results.sort((a, b) -> Integer.compare(a.getPort(), b.getPort()));
        return results;
    }

    /**
     * Scans a single port and returns its result (open/closed, service
     * guess, banner if grabbed, response time).
     */
    public ScanResult scanPort(String host, int port) {
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            long elapsed = System.currentTimeMillis() - start;
            String banner = grabBanner(socket);
            return new ScanResult(host, port, true, CommonPorts.lookup(port), banner, elapsed);
        } catch (IOException e) {
            long elapsed = System.currentTimeMillis() - start;
            return new ScanResult(host, port, false, null, null, elapsed);
        }
    }

    private String grabBanner(Socket socket) {
        try {
            socket.setSoTimeout(bannerTimeoutMs);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            char[] buffer = new char[256];
            int read = reader.read(buffer);
            if (read > 0) {
                String banner = new String(buffer, 0, read).trim();
                // collapse whitespace/newlines for clean single-line display
                return banner.replaceAll("\\s+", " ");
            }
        } catch (IOException e) {
            // service didn't send anything within the timeout — normal for
            // many services that wait for the client to speak first (e.g. HTTP)
        }
        return "";
    }
}
