package gossip;

public class Receiver implements Runnable{

	private Sender s;
	private Message m;
	
	public Receiver(Sender init, String s, int drop) {
		this.s = init;
		m = Message.fromString(s);
	}

	public void run() {
		s.mergeVersions(m.getAllCounters());
	}
}
