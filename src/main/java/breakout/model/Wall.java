package breakout.model;

import java.util.ArrayList;

public class Wall {
	private static final int WALL_LENGTH = 10;
	private ArrayList<ArrayList<Brick>> wall;
	
	public Wall() {
		this.wall = new ArrayList<>();
		for(int difficulty = 5; difficulty>0; difficulty--) {
			ArrayList<Brick> line = new ArrayList<>();
			for(int i=0; i<WALL_LENGTH; i++) {
				line.add(new Brick(difficulty));
			}
			this.wall.add(line);
		}
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
