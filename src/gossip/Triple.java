package gossip;

public class Triple {

	private boolean alive;
	private int version;
	private int lastUpdatePeriod;
	
	public Triple(int v, int l) {
		this.alive = true;
		this.version = v;
		this.lastUpdatePeriod = l;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getLastUpdatePeriod() {
		return lastUpdatePeriod;
	}

	public void setLastUpdatePeriod(int lastUpdatePeriod) {
		this.lastUpdatePeriod = lastUpdatePeriod;
	}
	
	public boolean getAlive() {
		return this.alive;
	}
	
	public void setAlive(boolean b) {
		this.alive = b;
	}
	
}
