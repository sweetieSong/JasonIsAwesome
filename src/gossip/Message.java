package gossip;

import java.util.HashMap;

public class Message {

	private int fromPort;
	private HashMap<Integer, Integer> allCounters; 

	public Message(int fromPort, HashMap<Integer, Integer> allCounters) {
		this.fromPort = fromPort;
		this.allCounters = allCounters;
	}

	public static Message fromString(String s) {
		String[] splitted = s.split(" ");
		return new Message(Integer.parseInt(splitted[0]), deserializeHashMap(splitted[1]));
	}

	private static HashMap<Integer, Integer> deserializeHashMap(String s) {
		s = s.substring(1, s.length() - 1);
		String[] splitted = s.split(",");

		HashMap<Integer, Integer> c = new HashMap<Integer, Integer>();
		for (String entry : splitted) {
			String[] keyVal = entry.split(":");
			c.put(Integer.parseInt(keyVal[0]), Integer.parseInt(keyVal[1]));
		}
		return c;
	}

	public int getFromPort() {
		return fromPort;
	}

	public HashMap<Integer, Integer> getAllCounters() {
		return allCounters;
	}
}
