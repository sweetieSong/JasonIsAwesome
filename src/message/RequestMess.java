package message;


public class RequestMess extends Mess{
	
	protected int relayPort;

	public RequestMess(int fp, int tp, int per) {
		super(fp, tp, per);
	}
	
	public RequestMess(String t, int fp, int tp, int per, int rp) {
		super(fp, tp, per);
		this.relayPort = rp;
		if (t.equals(Mess.Type.PING_REQ.toString())) {
			this.type = Mess.Type.PING_REQ;
		} else {
			this.type = Mess.Type.ACK_REQ;
		}
	}
	
	public RequestMess(Mess.Type t, int fp, int tp, int per, int rp) {
		super(fp, tp, per);
		this.relayPort = rp;
		this.type = t;
	}
	
	public String toString() {
		return String.format("%s %d %d %d %d", this.type.toString(), 
				this.fromPort, this.toPort, this.period, this.relayPort);
	}
	
	public static Mess fromString(String s) {
		String[] splitted = s.split(" ");
		String t = splitted[0];
		if (t.contains("REQ")) {
			return new RequestMess(t, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), 
					Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]));
		} else {
			return new ForwardMess(t, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), 
				Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]));
		}
	}

	public int getRelayPort() {
		return relayPort;
	}

	public void setRelayPort(int relayPort) {
		this.relayPort = relayPort;
	}
	
	
}
