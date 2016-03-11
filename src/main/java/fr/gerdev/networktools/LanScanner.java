package fr.gerdev.networktools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class that scans ip local network Any address in the range 192.168.xxx.xxx is
 * a private (aka site local) IP address. The same applies to 10.xxx.xxx.xxx
 * addresses, and 172.16.xxx.xxx through 172.31.xxx.xxx. Addresses in the range
 * 169.254.xxx.xxx are link local IP addresses. These are reserved for use on a
 * single network segment. Addresses in the range 224.xxx.xxx.xxx through
 * 239.xxx.xxx.xxx are multicast addresses. The address 255.255.255.255 is the
 * broadcast address. Anything else should be a valid public point-to-point IPv4
 * address.
 * 
 * @author germo_000
 *
 */

public class LanScanner {

	private String localIp = "";
	private String submask;

	private Set<String> liveHosts = new HashSet<>();

	// class will perform scans in a pool of threads to maximize perfs.
	private final ExecutorService scanExecutor = Executors.newFixedThreadPool(1000);

	public LanScanner() {
		super();

		// registering the local ip adress
		try {
			this.localIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.localIp = "";
		}

		// we keep the first 2 bytes of ip to get the submask
		// dont forget to espace character ., or use Pattern.quote(".")
		String[] ipBytes = localIp.split("\\.");

		if (ipBytes.length==4) {
			this.submask = ipBytes[0] + "." + ipBytes[1];
		}

		System.out.println("Your local ip is : " + localIp);
		System.out.println("Scan beeing performed on submask : " + submask);
	}

	/**
	 * Performs scans on ip linked to submask, with a timeout of 2500ms;
	 * 
	 * @return Set<String> representing live hosts.
	 */
	public Set<String> getLiveHosts() {

		List<Future<ScanResult<String>>> futures = new ArrayList<>();

		for (int firstByte = 0; firstByte < 20; firstByte++) {
			for (int secondByte = 0; secondByte < 255; secondByte++) {
				String ip = this.submask + "." + firstByte + "." + secondByte;
				futures.add(hostScan(ip));
			}
		}

		scanExecutor.shutdown();

		try {
			for (final Future<ScanResult<String>> f : futures) {
				ScanResult<String> scanResult = f.get();
				if (scanResult.isOpen()) {
					this.liveHosts.add(scanResult.getEntity());
				}
			}
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
		return liveHosts;
	}

	private Future<ScanResult<String>> hostScan(final String ipScanned) {
		return scanExecutor.submit(new Callable<ScanResult<String>>() {
			public ScanResult<String> call() {

				boolean isOpen = false;

				// this part of code can throw exceptions if bad ipScanned
				// value, or
				// unreachable host.
				try {
					InetAddress ipAdress = InetAddress.getByName(ipScanned);
					if (ipAdress.isReachable(2500)) {
						liveHosts.add(ipScanned);
						isOpen = true;
					}
				} catch (Exception e) {
				}

				return new ScanResult<String>(ipScanned, isOpen);
			}
		});
	}

}
