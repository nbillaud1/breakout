package breakout.model;

import java.util.ArrayList;

public class Wall {
	public static final int COLS = 10;
	private ArrayList<ArrayList<Brick>> wall;

	/** Le mur d'origine : 5 rangées, de 5 points en haut à 1 point en bas. */
	public Wall() {
		this("5555555555", "4444444444", "3333333333", "2222222222", "1111111111");
	}

	/** Une chaîne par rangée : un chiffre = les points de vie de la brique, '.' = pas de brique. */
	public Wall(String... rows) {
		this.wall = new ArrayList<>();
		for(String row : rows) {
			ArrayList<Brick> line = new ArrayList<>();
			for(int i=0; i<COLS; i++) {
				char c = i < row.length() ? row.charAt(i) : '.';
				line.add(new Brick(Character.isDigit(c) ? c - '0' : 0));
			}
			this.wall.add(line);
		}
	}

	public int rows() {
		return this.wall.size();
	}

	public Brick brick(int row, int col) {
		return this.wall.get(row).get(col);
	}

	public int aliveCount() {
		int count = 0;
		for(ArrayList<Brick> line : this.wall) {
			for(Brick brick : line) {
				if(brick.difficulty() > 0) count++;
			}
		}
		return count;
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
