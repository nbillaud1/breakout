package breakout.model;

public class Brick{
	private int difficulty;
	
	public Brick(int difficulty) {
		this.difficulty = difficulty;
	}
	
	public int difficulty() {
		return this.difficulty;
	}
	
	public String toString() {
		return "[" + this.difficulty + "]";
	}
	
	public void touched() {
		difficulty --;
	}
	
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
}