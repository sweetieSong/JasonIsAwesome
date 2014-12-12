package message;

public class ForwardMess extends RequestMess{
	
	public ForwardMess(String t, int fp, int tp, int per, int rp) {
		super(fp, tp, per);
		this.relayPort = rp;
		if (t.equals(Mess.Type.PING_FWD.toString())) {
			this.type = Mess.Type.PING_FWD;
		} else {
			this.type = Mess.Type.ACK_FWD;
		}
	}
	
	public ForwardMess(Mess.Type t, int fp, int tp, int per, int rp) {
		super(fp, tp, per);
		this.relayPort = rp;
		this.type = t;
	}

	public static Mess fromString(String s) {
		String[] splitted = s.split(" ");
		String t = splitted[0];
		return new ForwardMess(t, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), 
				Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]));
	}
}
