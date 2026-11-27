package breakout.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameTest {

	private static final double DT = 0.01;

	@Test
	void ballStaysOnPadUntilLaunched() {
		Game game = new Game();
		game.movePadTo(100);
		for (int i = 0; i < 100; i++) game.update(DT);
		assertEquals(Game.State.READY, game.state());
		assertEquals(100, game.ballX(), 1e-9);
	}

	@Test
	void missedBallLosesAfterBreakingBricks() {
		Game game = new Game();
		game.launch();
		for (int i = 0; i < 100_000 && game.state() == Game.State.PLAYING; i++) {
			game.movePadTo(game.ballX() < Game.W / 2 ? Game.W : 0); // pad toujours loin de la balle
			game.update(DT);
		}
		assertEquals(Game.State.LOST, game.state());
		assertTrue(damagedBricks(game) > 0, "la balle aurait dû toucher le mur");
	}

	@Test
	void padFollowingTheBallNeverLosesAndEventuallyWins() {
		Game game = new Game();
		game.launch();
		for (int i = 0; i < 2_000_000 && game.state() == Game.State.PLAYING; i++) {
			double offset = ((i / 5_000) % 7 - 3) * 15; // vise différents endroits du pad pour varier les angles
			game.movePadTo(game.ballX() + offset);
			game.update(DT);
		}
		assertEquals(Game.State.WON, game.state());
	}

	@Test
	void oneImpactRemovesOnlyOnePoint() {
		Game game = new Game();
		int col = 3, row = Wall.ROWS - 2; // brique à 2 points, celle du dessous est retirée
		game.wall().brick(Wall.ROWS - 1, col).setDifficulty(0);
		double brickBottom = Game.WALL_TOP + (row + 1) * Game.BRICK_H;
		// Balle lente qui chevauche déjà le bas de la brique : elle y resterait plusieurs images sans correction.
		game.placeBall(col * Game.BRICK_W + Game.BRICK_W / 2, brickBottom + 2, 0, -100);
		for (int i = 0; i < 50; i++) game.update(DT);
		assertEquals(1, game.wall().brick(row, col).difficulty());
	}

	private static int damagedBricks(Game game) {
		int n = 0;
		for (int r = 0; r < Wall.ROWS; r++)
			for (int c = 0; c < Wall.COLS; c++)
				if (game.wall().brick(r, c).difficulty() < Wall.ROWS - r) n++;
		return n;
	}
}
