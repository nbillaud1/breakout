package breakout.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class GameTest {

	private static final double DT = 0.01;

	private static Level level(double descentEvery, String... rows) {
		return new Level("Test", descentEvery, 350, rows);
	}

	@Test
	void ballStaysOnPadUntilLaunched() {
		Game game = new Game();
		game.movePadTo(100);
		for (int i = 0; i < 100; i++) game.update(DT);
		assertEquals(Game.State.READY, game.state());
		assertEquals(100, game.ballX(), 1e-9);
	}

	@Test
	void eachMissedBallCostsALifeThenTheGame() {
		Game game = new Game();
		for (int lives = Game.START_LIVES - 1; lives >= 0; lives--) {
			game.launch();
			for (int i = 0; i < 100_000 && game.state() == Game.State.PLAYING; i++) {
				game.movePadTo(game.ballX() < Game.W / 2 ? Game.W : 0); // pad toujours loin de la balle
				game.update(DT);
			}
			assertEquals(lives, game.lives());
		}
		assertEquals(Game.State.LOST, game.state());
		assertTrue(game.score() > 0, "la balle aurait dû toucher des briques");
	}

	@Test
	void aPerfectPlayerFinishesEveryLevel() {
		Game game = new Game();
		for (int i = 0; i < 5_000_000 && game.state() != Game.State.WON && game.state() != Game.State.LOST; i++) {
			if (game.state() == Game.State.READY) game.launch();
			if (game.state() == Game.State.LEVEL_CLEARED) game.nextLevel();
			double offset = ((i / 5_000) % 7 - 3) * 15; // vise différents endroits du pad pour varier les angles
			game.movePadTo(game.ballX() + offset);
			game.update(DT);
		}
		assertEquals(Game.State.WON, game.state());
		assertEquals(Game.START_LIVES, game.lives());
	}

	@Test
	void oneImpactRemovesOnlyOnePoint() {
		Game game = new Game(List.of(level(1000, "2222222222")));
		int col = 3;
		// Balle lente qui chevauche déjà le bas de la brique : elle y resterait plusieurs images sans correction.
		game.placeBall(game.brickX(col) + Game.BRICK_W / 2, game.brickY(0) + Game.BRICK_H + 2, 0, -100);
		for (int i = 0; i < 50; i++) game.update(DT);
		assertEquals(1, game.wall().brick(0, col).difficulty());
	}

	@Test
	void comboRaisesPointsUntilThePadIsTouched() {
		Game game = new Game(List.of(level(1000, "1.........", "..........", "1.........")));
		// Balle dans le trou entre les deux briques : elle casse celle du haut, redescend et casse celle du bas.
		game.placeBall(game.brickX(0) + Game.BRICK_W / 2, game.brickY(1) + Game.BRICK_H / 2, 0, -200);
		for (int i = 0; i < 100; i++) game.update(DT);
		assertEquals(0, game.wall().brick(0, 0).difficulty());
		assertEquals(0, game.wall().brick(2, 0).difficulty());
		// 10 pour la première brique, 20 pour la seconde (combo), 500 de bonus de fin de niveau.
		assertEquals(10 + 20 + 500, game.score());
	}

	@Test
	void clearingALevelGivesABonusThenTheNextLevel() {
		Game game = new Game(List.of(level(1000, "1........."), level(1000, "1.........")));
		game.placeBall(game.brickX(0) + Game.BRICK_W / 2, game.brickY(0) + Game.BRICK_H + 5, 0, -100);
		for (int i = 0; i < 20; i++) game.update(DT);
		assertEquals(Game.State.LEVEL_CLEARED, game.state());
		assertEquals(10 + 500, game.score());
		game.nextLevel();
		assertEquals(Game.State.READY, game.state());
		assertEquals(2, game.levelNumber());
		assertEquals(1, game.wall().brick(0, 0).difficulty());
	}

	@Test
	void theWallGoesDownAndCrushesThePlayer() {
		Game game = new Game(List.of(level(0.5, "1.........")));
		double startY = game.brickY(0);
		game.placeBall(600, 300, 0, -200); // rebondit à la verticale, loin de la brique
		for (int i = 0; i < 100_000 && game.state() == Game.State.PLAYING; i++) {
			game.movePadTo(600);
			game.update(DT);
		}
		assertEquals(Game.State.LOST, game.state());
		assertEquals(Game.START_LIVES, game.lives(), "perdu par le mur, pas par la balle");
		assertTrue(game.brickY(0) > startY);
	}
}
