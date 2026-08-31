# Network Scanner (Java)

A multi-threaded TCP port scanner with banner grabbing, written in Java
using only the standard library (no external dependencies required to
run it). Built as an educational/defensive security tool for scanning
hosts and networks you own or are authorized to test.

## Features

- **Multi-threaded port scanning** — configurable thread pool (default
  100 concurrent connection attempts) for fast range scans
- **Banner grabbing** — reads whatever a service sends first on connect
  (e.g. SSH version strings), without sending any payload of its own
- **Well-known port labeling** — maps common ports (22, 80, 443, 3306,
  etc.) to service names
- **Host resolution + reachability check** before scanning
- **Authorization confirmation prompt** — requires typing `YES` before
  scanning a host (skippable with `--yes` for authorized automated use)
- **Input validation** — rejects invalid port ranges before attempting
  anything

## Project Structure

```
network-scanner/
├── src/main/java/scanner/
│   ├── Main.java            # CLI entry point, authorization prompt
│   ├── PortScanner.java     # core multi-threaded scanning engine
│   ├── HostDiscovery.java   # hostname resolution + reachability check
│   ├── ScanResult.java      # result data class
│   └── CommonPorts.java     # well-known port → service name lookup
├── pom.xml                  # Maven build (optional — see note below)
└── README.md
```

## Quick Start (plain javac/java — no Maven required)

```bash
git clone https://github.com/isaacLagat/Network_scanning_for_cybersecurity.git
cd Network_scanning_for_cybersecurity
mkdir build
javac -d build src/main/java/scanner/*.java
java -cp build scanner.Main <host> <startPort> <endPort>
```

Example:
```bash
java -cp build scanner.Main 127.0.0.1 1 1024
```

You'll be asked to confirm authorization before the scan starts (see
Legal/Ethical Use below). Pass `--yes` as a 4th argument to skip the
prompt for scripted use.

### Real, tested output

Tested against two local TCP servers (one sending an SSH-style banner,
one sending nothing) on an otherwise-closed port range:

```
$ java -cp build scanner.Main 127.0.0.1 8999 9003
============================================================
NETWORK SCANNER — AUTHORIZATION CHECK
============================================================
You are about to scan: 127.0.0.1
Only scan systems you own or have explicit written
authorization to test. Unauthorized scanning may be
illegal in your jurisdiction (e.g. under the U.S.
Computer Fraud and Abuse Act or similar laws elsewhere).
============================================================
Type YES to confirm you are authorized to scan this host: YES
Resolving 127.0.0.1...
Resolved to 127.0.0.1
Host reachable (ICMP/echo): true (note: many hosts block this even when up — scan proceeds regardless)
Scanning 127.0.0.1, ports 8999-9003 ...

============================================================
SCAN RESULTS
============================================================
127.0.0.1:9001   OPEN   service=unknown      (20ms)  banner="SSH-2.0-OpenSSH_8.9"
127.0.0.1:9002   OPEN   service=unknown      (12ms)
============================================================
Scanned 5 ports in 0.30s — 2 open
```

Both open ports were correctly detected, and the banner was correctly
captured on the port that sent one — the port that sent nothing
correctly shows no banner. Closed ports in the same range (8999, 9000,
9003) were correctly excluded from the results.

## Optional: Maven Build

A `pom.xml` is included for building a runnable JAR with Maven:

```bash
mvn package
java -jar target/network-scanner.jar 127.0.0.1 1 1024
```

**Note:** this Maven build path is standard and should work with a
normal internet connection, but I wasn't able to verify it end-to-end
in the environment I built this in (restricted network access to Maven
Central). The plain `javac`/`java` path above is the one I've fully
tested — use Maven if you prefer it, but the javac path is guaranteed
to work.

## Known Limitations

- **ICMP reachability checks are unreliable** — Java's
  `InetAddress.isReachable()` can report a host as "reachable" even
  when it isn't (or vice versa), especially across firewalls/NAT. This
  is a well-known limitation, not specific to this tool (even
  industry-standard scanners like `nmap` face the same issue) — treat
  the reachability line as advisory, and trust the actual port scan
  results over it.
- Banner grabbing only captures what a service sends unprompted on
  connect — it won't grab banners from services that wait for the
  client to speak first (e.g. plain HTTP without sending a request).
- No UDP scanning — TCP only.

## Legal / Ethical Use

This tool is for **educational purposes and authorized security testing
only**.
- Only scan hosts and networks you own or have explicit written
  authorization to test
- Unauthorized port scanning may violate computer misuse laws (e.g. the
  U.S. Computer Fraud and Abuse Act, or equivalent laws elsewhere)
- The authorization prompt in `Main.java` is a reminder, not a
  substitute for actually having permission

## License

MIT License
