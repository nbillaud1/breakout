package breakout.model;

public class Brick{
	private final int initialDifficulty;
	private int difficulty;

	public Brick(int difficulty) {
		this.initialDifficulty = difficulty;
		this.difficulty = difficulty;
	}

	public int difficulty() {
		return this.difficulty;
	}

	/** Points de vie au début du niveau : la vue s'en sert pour dessiner les fissures. */
	public int initialDifficulty() {
		return this.initialDifficulty;
	}

	public String toString() {
		return "[" + this.difficulty + "]";
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
}
