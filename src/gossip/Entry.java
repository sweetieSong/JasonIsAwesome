package gossip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Entry {

	public static void main(String[] args) throws IOException {
		int receivePort = Integer.parseInt(args[0]);
		int periodTime = Integer.parseInt(args[1]);
		int totalPorts = Integer.parseInt(args[2]);
		int drop = Integer.parseInt(args[3]);
		String s = args[4];
		

		Sender init = new Sender(s, receivePort, periodTime, totalPorts);
		new Thread(init).start();

		ServerSocket welcomeSocket = new ServerSocket(62000 + receivePort);
		ExecutorService executor = Executors.newFixedThreadPool(5);
		Random random = new Random();
		
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient =
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			String clientSentence = inFromClient.readLine();
			
			int prob = random.nextInt(101);
			if (prob < drop) {
				connectionSocket.close();
				continue;
			}
	
			Receiver res = new Receiver(init, clientSentence, drop);
			executor.execute(res);
			connectionSocket.close();
		}
	}
}
