package scanner;

import java.util.List;
import java.util.Scanner;

/**
 * CLI entry point.
 *
 * Usage:
 *   java -jar network-scanner.jar <host> <startPort> <endPort> [--yes]
 *
 * Example:
 *   java -jar network-scanner.jar 127.0.0.1 1 1024
 *
 * By default, prompts for an authorization confirmation before scanning
 * anything (see the Legal/Ethical notice below) — pass --yes to skip the
 * prompt for scripted/automated use against systems you're already
 * authorized to test.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
            System.exit(1);
        }

        String host = args[0];
        int startPort, endPort;
        try {
            startPort = Integer.parseInt(args[1]);
            endPort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Error: startPort and endPort must be integers.");
            printUsage();
            System.exit(1);
            return;
        }

        if (startPort < 1 || endPort > 65535 || startPort > endPort) {
            System.err.println("Error: invalid port range. Must be within 1-65535 and startPort <= endPort.");
            System.exit(1);
        }

        boolean skipConfirm = args.length > 3 && args[3].equals("--yes");

        if (!skipConfirm && !confirmAuthorization(host)) {
            System.out.println("Scan cancelled.");
            return;
        }

        System.out.println("Resolving " + host + "...");
        String resolvedIp = HostDiscovery.resolveHostname(host);
        if (resolvedIp == null) {
            System.err.println("Could not resolve host: " + host);
            System.exit(1);
        }
        System.out.println("Resolved to " + resolvedIp);

        boolean reachable = HostDiscovery.isReachable(resolvedIp, 1000);
        System.out.println("Host reachable (ICMP/echo): " + reachable +
                " (note: many hosts block this even when up — scan proceeds regardless)");

        System.out.printf("Scanning %s, ports %d-%d ...%n", host, startPort, endPort);
        long scanStart = System.currentTimeMillis();

        PortScanner scanner = new PortScanner();
        List<ScanResult> results;
        try {
            results = scanner.scanRange(host, startPort, endPort, false);
        } catch (InterruptedException e) {
            System.err.println("Scan interrupted.");
            return;
        }

        long elapsed = System.currentTimeMillis() - scanStart;

        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("SCAN RESULTS");
        System.out.println("=".repeat(60));

        if (results.isEmpty()) {
            System.out.println("No open ports found in range " + startPort + "-" + endPort + ".");
        } else {
            for (ScanResult result : results) {
                System.out.println(result);
            }
        }

        System.out.println("=".repeat(60));
        System.out.printf("Scanned %d ports in %.2fs — %d open%n",
                (endPort - startPort + 1), elapsed / 1000.0, results.size());
    }

    private static boolean confirmAuthorization(String host) {
        System.out.println("=".repeat(60));
        System.out.println("NETWORK SCANNER — AUTHORIZATION CHECK");
        System.out.println("=".repeat(60));
        System.out.println("You are about to scan: " + host);
        System.out.println("Only scan systems you own or have explicit written");
        System.out.println("authorization to test. Unauthorized scanning may be");
        System.out.println("illegal in your jurisdiction (e.g. under the U.S.");
        System.out.println("Computer Fraud and Abuse Act or similar laws elsewhere).");
        System.out.println("=".repeat(60));
        System.out.print("Type YES to confirm you are authorized to scan this host: ");

        Scanner sc = new Scanner(System.in);
        String response = sc.hasNextLine() ? sc.nextLine().trim() : "";
        return response.equalsIgnoreCase("YES");
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar network-scanner.jar <host> <startPort> <endPort> [--yes]");
        System.out.println("Example: java -jar network-scanner.jar 127.0.0.1 1 1024");
    }
}
