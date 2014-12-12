package gossip;

import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Sender implements Runnable{

	public enum Style {ORIGINAL, ROBIN}
	private static Random rand = new Random();

	private Style style;
	private int receivePort;
	private int periodTime; // in seconds
	private int totalPorts;
	private int failPeriod;
	private int heartbeatVersion = 0;
	private HashMap<Integer, Triple> versions;
	private ReentrantLock lock = new ReentrantLock();

	public Sender(Style s, int receivePort, int periodTime, int totalPorts) 
			throws UnknownHostException, SocketException {

		this.style = s;
		this.receivePort = receivePort;
		this.periodTime = periodTime;
		this.totalPorts = totalPorts;
		this.versions = new HashMap<Integer, Triple>();

		for (int i = 0; i < this.totalPorts ; i++) {
			this.versions.put(i, new Triple(0, 0));
		}
	}

	public Sender(String s, int receivePort, int periodTime, int totalPorts) 
			throws UnknownHostException, SocketException {

		this(Style.ROBIN, receivePort, periodTime, totalPorts);
		if (s.equals("ORIGINAL")) {
			this.style = Style.ORIGINAL;
		}
		
		if (this.style == Style.ORIGINAL) {
			this.failPeriod = (int) Math.ceil(Math.sqrt(this.totalPorts));
		} else {
			this.failPeriod = (int) Math.ceil(Math.log(this.totalPorts)/Math.log(2));
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(this.periodTime);
				this.heartbeatVersion = this.heartbeatVersion + 1;
				this.versions.get(this.receivePort).setVersion(this.heartbeatVersion);
				this.versions.get(this.receivePort).setLastUpdatePeriod(this.heartbeatVersion);

				if (this.receivePort == 0 && this.heartbeatVersion == 10) {
					return;
				}
				gossip();
				failAndCleanup();

				if (this.style == Style.ROBIN) {
					if (this.heartbeatVersion > 0) {
						int residual = (this.heartbeatVersion - 2) % this.failPeriod;
						int origin = this.receivePort - ((int) Math.pow(2, residual));
						if (origin < 0) {
							origin += this.totalPorts;
						}
						if (origin == this.receivePort) {
							continue;
						}
						try {
							if (this.versions.containsKey(origin) && this.versions.get(origin).getAlive() &&
									this.versions.get(origin).getLastUpdatePeriod() < this.heartbeatVersion - 2) {
								this.versions.get(origin).setAlive(false);
								System.out.println(this.heartbeatVersion);
								return;
							}
						} catch (Exception e) {}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int randomPort() {
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int num = rand.nextInt(this.totalPorts);
		while (!this.versions.containsKey(num) || num == this.receivePort) {
			num = rand.nextInt(this.totalPorts);
		}
		return num;
	}

	public void gossip(){
		int dstPort;
		if (this.style == Style.ORIGINAL) {
			dstPort = randomPort();
		} else {
			int residual = (this.heartbeatVersion - 1) % this.failPeriod;
			dstPort = (this.receivePort + ((int) Math.pow(2, residual)) )  % this.totalPorts;
		}
		if (dstPort == this.receivePort) {
			return;
		}
		lock.lock();
		String s = this.receivePort + " " + serializeHashMap();


		try {
			Socket clientSocket = new Socket("localhost", 62000+dstPort);
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println(s);
			clientSocket.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		lock.unlock();
	}

	public void mergeVersions(HashMap<Integer, Integer> given) {
		for (Entry<Integer, Integer> e : given.entrySet()) {
			int k = e.getKey();
			int v = e.getValue();

			if (this.versions.containsKey(k) && this.versions.get(k).getAlive()) {
				Triple mine = versions.get(k);
				if (mine.getVersion() < v) {
					mine.setVersion(v);
					mine.setLastUpdatePeriod(this.heartbeatVersion);
				}
			}
		}
	}

	private String serializeHashMap() {
		String s = "{";
		for (Map.Entry<Integer, Triple> e : this.versions.entrySet()) {
			if (e.getValue().getAlive()) {
				s = s + e.getKey() + ":" + e.getValue().getVersion() + ",";
			}
		}
		return s.substring(0, s.length() - 1) + "}";
	}

	private void failAndCleanup(){
		try {
			for (int i = 0 ; i < this.totalPorts ; i++) {
				if (!this.versions.containsKey(i)) {
					continue;
				}

				Triple p = this.versions.get(i);
				if (this.heartbeatVersion - p.getLastUpdatePeriod() > 2*this.failPeriod + 1) {
					this.versions.remove(i);
				} else if (this.heartbeatVersion - p.getLastUpdatePeriod() > this.failPeriod + 1
						&& p.getAlive()){
					p.setAlive(false);
					System.out.println(this.heartbeatVersion);
					return;
				}
			}
		} catch (Exception e) {
		}
	}
}
