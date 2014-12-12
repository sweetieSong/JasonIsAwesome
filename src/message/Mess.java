package message;

public class Mess {
	public enum Type {PING, ACK, PING_REQ, ACK_REQ, PING_FWD, ACK_FWD}

	protected Type type;
	protected int fromPort;
	protected int toPort;
	protected int period;

	public Mess(int fp, int tp, int per) {
		this.fromPort = fp;
		this.toPort = tp;
		this.period = per;
	}

	public Mess(String t, int fp, int tp, int per) {
		this(fp, tp, per);
		if (t.equals(Type.PING.toString())) {
			this.type = Type.PING;
		} else {
			this.type = Type.ACK;
		}
	}
	
	public Mess(Mess.Type t, int fp, int tp, int per) {
		this(fp, tp, per);
		this.type = t;
	}

	public String toString() {
		return String.format("%s %d %d %d", this.type.toString(), 
				this.fromPort, this.toPort, this.period);
	}

	public static Mess fromString(String s) {
		String[] splitted = s.split(" ");
		String t = splitted[0];
		if (t.contains("REQ")) {
			return new RequestMess(t, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), 
					Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]));
		} else if (t.contains("FWD")) {
			return new ForwardMess(t, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), 
					Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]));
		} else {
			return new Mess(t, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), 
					Integer.parseInt(splitted[3]));
		}
	}

	public boolean isRequest() {
		return type == Type.PING_REQ || type == Type.ACK_REQ;
	}

	public boolean isFroward() {
		return type == Type.PING_FWD || type == Type.ACK_FWD;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getFromPort() {
		return fromPort;
	}

	public void setFromPort(int fromPort) {
		this.fromPort = fromPort;
	}

	public int getToPort() {
		return toPort;
	}

	public void setToPort(int toPort) {
		this.toPort = toPort;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public int getRelayPort() {
		return toPort;
	}

	public void setRelayPort(int toPort) {
		this.toPort = toPort;
	}
}
