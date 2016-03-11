package fr.gerdev.networktools;

import java.util.Set;

/**
 * Hello world!
 */
public class App {
	public static void main(String[] args) {

		//scanIp("192.168.2.88");

		scanLan();
	}

	private static void scanLan() {
		long startTime = System.currentTimeMillis();

		LanScanner lanScanner = new LanScanner();
		Set<String> upHosts = lanScanner.getLiveHosts();

		long duration = (System.currentTimeMillis() - startTime);

		for (String ip : upHosts) {
			System.out.println("Host " + ip + " is up");
		}

		System.out.println("Scan duration : " + duration + " ms");
	}

	private static void scanIp(String ip) {
		long startTime = System.currentTimeMillis();

		PortScanner portScanner = new PortScanner(ip);
		Set<Integer> openPorts = portScanner.getOpenPorts();

		long duration = (System.currentTimeMillis() - startTime);

		for (Integer port : openPorts) {
			System.out.println("Le port " + port + " est ouvert");
		}

		System.out.println("Scan duration : " + duration + " ms");
	}
}
