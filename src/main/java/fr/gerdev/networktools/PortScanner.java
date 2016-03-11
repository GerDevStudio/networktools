package fr.gerdev.networktools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PortScanner {

	private final static int TIMEOUT = 200;

	private String targetIp;

	// class will perform scans in a pool of threads to maximize perfs.
	private final ExecutorService scanExecutor = Executors.newFixedThreadPool(1000);
	private final Set<Integer> openPorts = new HashSet<>();

	public PortScanner(String targetIp) {
		super();
		this.targetIp = targetIp;
	}

	/**
	 * 1 to 1023 : well known ports 1024 to 49151 : registered ports
	 * 
	 * @return Set of ports opened on target ip
	 */
	public Set<Integer> getOpenPorts() {

		final List<Future<ScanResult<Integer>>> futures = new ArrayList<>();

		for (int port = 1; port <= 49151; port++) {
			futures.add(portScan(port));
		}
		scanExecutor.shutdown();

		try {
			for (final Future<ScanResult<Integer>> f : futures) {
				ScanResult<Integer> scanResult = f.get();
				if (scanResult.isOpen()) {
					this.openPorts.add(scanResult.getEntity());
				}
			}
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
		return openPorts;
	}

	/**
	 * Adds in the scanExecutor to scan specified port
	 * 
	 * @return A boolean that will be perform on user demand
	 */
	private Future<ScanResult<Integer>> portScan(final int port) {
		return scanExecutor.submit(new Callable<ScanResult<Integer>>() {
			public ScanResult<Integer> call() {

				boolean isOpen = false;

				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(targetIp, port), TIMEOUT);
					socket.close();
					isOpen = true;
				} catch (IOException e) {
					// when timeout, port is closed. IOException is thrown.
					// e.printStackTrace();
				}

				return new ScanResult<Integer>(port, isOpen);
			}
		});
	}

}
