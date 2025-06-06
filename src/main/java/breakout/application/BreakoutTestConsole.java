package breakout.application;

import breakout.model.Wall;

public class BreakoutTestConsole {
	private static final String INTERLINE = "--------------------------------------------------------";

	public static void main(String args[]) {
		System.out.println(INTERLINE);
		System.out.println("Bienvenue dans mon super jeu de Breakout !!!");
		System.out.println(INTERLINE);
		System.out.println();
		
		Wall wall = new Wall();
		wall.printWall();
		
		System.out.println();
		System.out.println();
		System.out.println("             *");
		System.out.println("            ___");
	}
}
