package dynamo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sock {

	public static void main(String[] args) throws IOException {
		int receivePort = Integer.parseInt(args[0]);
		int periodTime = Integer.parseInt(args[1]);
		int totalPorts = Integer.parseInt(args[2]);
		int numFriends = Integer.parseInt(args[3]);
		int drop = Integer.parseInt(args[4]);

		DatagramSocket datagramSocket = new DatagramSocket(61000 + receivePort);
		Initiator init = new Initiator(receivePort, periodTime, totalPorts, numFriends, datagramSocket);
		new Thread(init).start();

		ExecutorService executor = Executors.newFixedThreadPool(5);
		while (true) {
			byte[] buffer = new byte[256];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			datagramSocket.receive(packet);
			String received = new String(packet.getData(), 0, packet.getLength());

			Responder res = new Responder(init, received, drop, datagramSocket);
			executor.execute(res);
		}
	}
}
