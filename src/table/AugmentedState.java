package table;

public class AugmentedState {
	private String random = null;
	private String previous = null;
	private String current = null;
	
	private AugmentedState(String random, String previous, String current) {
		this.setRandom(random);
		this.setPrevious(previous);
		this.setCurrent(current);
	}
	
	private AugmentedState(String random, String current) {
		this.setRandom(random);
		this.setCurrent(current);
	}
	
	public static AugmentedState of(String random, String previous, String current) {
		return new AugmentedState(random, previous, current);
	}
	
	public static AugmentedState of(String random, String current) {
		return new AugmentedState(random, current);
	}

	public String getRandom() {
		return random;
	}

	public void setRandom(String random) {
		this.random = random;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}
	
	
}
