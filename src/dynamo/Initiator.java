package dynamo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import message.Mess;
import message.RequestMess;

public class Initiator implements Runnable{

	private static Random rand = new Random();
	private DatagramSocket socket;
	private InetAddress localAddress;

	private int receivePort;
	private int periodTime; // in seconds
	private int totalPorts;
	private int period = 0;
	private int numFriends;
	private HashMap<Integer, Boolean> status;
	private HashMap<Integer, HashMap<Mess.Type, Integer>> lastHeard;
	private ReentrantLock lock = new ReentrantLock();

	public Initiator(int receivePort, int periodTime, int totalPorts, int numFriends, DatagramSocket socket) 
			throws UnknownHostException, SocketException {
		localAddress = InetAddress.getLocalHost();
		this.socket = socket;
		
		this.receivePort = receivePort;
		this.periodTime = periodTime;
		this.totalPorts = totalPorts;
		this.numFriends = numFriends;
		this.status = new HashMap<Integer, Boolean>();
		this.lastHeard = new HashMap<Integer, HashMap<Mess.Type, Integer>>();
		for (int i = 0; i < totalPorts ; i++) {
			this.status.put(i, true);
			this.lastHeard.put(i, new HashMap<Mess.Type, Integer>());
		}
	}

	@Override
	public void run() {
		
		try {
			while (true) {
				Thread.sleep(this.periodTime);
				this.period = this.period + 1;
				int randomPort = randomPort();
				
				sendMessage(new Mess(Mess.Type.PING, this.receivePort, randomPort, this.period), randomPort);

				Thread.sleep(500);
				lock.lock();
				if (hasHeardBack(randomPort, Mess.Type.ACK)) {
					lock.unlock();
					continue;
				} else {
					lock.unlock();
					ArrayList<Integer> friends = new ArrayList<Integer>(this.numFriends);
					for (int i = 0 ; i < this.numFriends; i++) {
						Integer randomFriend = new Integer(randomPort());
						while (friends.contains(randomFriend) || randomFriend == this.receivePort) {
							randomFriend = new Integer(randomPort());
						}
						friends.add(randomFriend);
						sendMessage(new RequestMess("PING_REQ", this.receivePort, randomPort, this.period, randomFriend), randomFriend);
					}
					
					Thread.sleep(500);
					lock.lock();
					boolean alive = hasHeardBack(randomPort, Mess.Type.ACK_REQ);
					
					if (!alive) {
						this.status.put(randomPort, false);
						if (randomPort == 0) {
							System.out.println(this.period);
							return;
						}
					}
					lock.unlock();
				}
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(Mess m, int dstPort) throws IOException {
		byte[] buffer = m.toString().getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
				this.localAddress, 61000 + dstPort);
		socket.send(packet);
		//System.out.println("send a packet to " + dstPort);
	}
	
	public boolean hasHeardBack(int toPort, Mess.Type t) {
		if (this.lastHeard.get(toPort).containsKey(t) &&
				(this.lastHeard.get(toPort).get(t) == this.period)) {
			return true;
		}
		return false;
	}

	public int randomPort() {
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
	    int num = rand.nextInt(this.totalPorts);
	    while (!this.status.get(num) || num == this.receivePort) {
	    	num = rand.nextInt(this.totalPorts);
	    }
	    return num;
	}

	public HashMap<Integer, HashMap<Mess.Type, Integer>> getLastHeard() {
		return lastHeard;
	}

	public void setLastHeard(HashMap<Integer, HashMap<Mess.Type, Integer>> lastHeard) {
		this.lastHeard = lastHeard;
	}
	
	
}
