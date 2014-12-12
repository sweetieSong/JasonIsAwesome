package dynamo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

import message.ForwardMess;
import message.Mess;
import message.RequestMess;

public class Responder implements Runnable {

	private Initiator init;
	private Mess inMess;
	private Mess outgoingMess;
	private int drop;
	private Random random;
	private InetAddress localAddress;
	private DatagramSocket socket;

	public Responder(Initiator init, String t, int drop, DatagramSocket socket) 
			throws UnknownHostException, SocketException {
		localAddress = InetAddress.getLocalHost();
		this.socket = socket;
		this.init = init;
		this.inMess = Mess.fromString(t);
		this.drop = drop;
		this.random = new Random();
	}

	private boolean shouldDrop() {
		int prob = random.nextInt(101);
		if (prob < this.drop) {
			return true;
		}
		return false;
	}

	private void processPING() throws IOException {
		outgoingMess = new Mess(Mess.Type.ACK, inMess.getFromPort(), inMess.getToPort(), inMess.getPeriod());
		sendMessage(outgoingMess, inMess.getFromPort());
	}
	
	private void processACK() {
		HashMap<Mess.Type, Integer> lastHeardPort = init.getLastHeard().get(inMess.getToPort());
		if (!lastHeardPort.containsKey(Mess.Type.ACK) ||
				lastHeardPort.get(Mess.Type.ACK) < inMess.getPeriod()) {
			lastHeardPort.put(Mess.Type.ACK, inMess.getPeriod());
		}
	}

	private void processPING_REQ() throws IOException {
		outgoingMess = new ForwardMess(Mess.Type.PING_FWD, inMess.getFromPort(), inMess.getToPort(), inMess.getPeriod(), inMess.getRelayPort());
		sendMessage(outgoingMess, inMess.getToPort());
	}
	
	private void processACK_REQ() {
		HashMap<Mess.Type, Integer> lastHeardPort = init.getLastHeard().get(inMess.getToPort());
		if (!lastHeardPort.containsKey(Mess.Type.ACK_REQ) ||
				lastHeardPort.get(Mess.Type.ACK_REQ) < inMess.getPeriod()) {
			lastHeardPort.put(Mess.Type.ACK_REQ, inMess.getPeriod());
		}
	}
	
	private void processPING_FWD() throws IOException {
		outgoingMess = new ForwardMess(Mess.Type.ACK_FWD, inMess.getFromPort(), inMess.getToPort(), inMess.getPeriod(), inMess.getRelayPort());
		sendMessage(outgoingMess, inMess.getRelayPort());
	}
	
	private void processACK_FWD() throws IOException {
		outgoingMess = new RequestMess(Mess.Type.ACK_REQ, inMess.getFromPort(), inMess.getToPort(), inMess.getPeriod(), inMess.getRelayPort());
		sendMessage(outgoingMess, inMess.getFromPort());
	}

	@Override
	public void run() {

		if (shouldDrop()) {
			return;
		}
		try {
			Mess.Type inType = inMess.getType();
			switch (inType) {
			case PING:
				processPING();
				break;
			case ACK:
				processACK();
				break;
			case PING_REQ:
				processPING_REQ();
				break;
			case ACK_REQ:
				processACK_REQ();
				break;
			case PING_FWD:
				processPING_FWD();
				break;
			default:
				processACK_FWD();
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendMessage(Mess m, int meantForPort) throws IOException {
		byte[] buffer = m.toString().getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, 
				this.localAddress, 61000 + meantForPort);
		socket.send(packet);
	}
}
