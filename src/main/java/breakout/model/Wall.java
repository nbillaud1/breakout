package breakout.model;

import java.util.ArrayList;

public class Wall {
	public static final int ROWS = 5;
	public static final int COLS = 10;
	private ArrayList<ArrayList<Brick>> wall;

	public Wall() {
		this.wall = new ArrayList<>();
		for(int difficulty = ROWS; difficulty>0; difficulty--) {
			ArrayList<Brick> line = new ArrayList<>();
			for(int i=0; i<COLS; i++) {
				line.add(new Brick(difficulty));
			}
			this.wall.add(line);
		}
	}

	public Brick brick(int row, int col) {
		return this.wall.get(row).get(col);
	}

	public void printWall() {
		for(ArrayList<Brick> line : this.wall) {
			System.out.print("|");
			for(Brick brick : line) {
				System.out.print(brick);
			}
			System.out.println("|");
		}
	}
}
