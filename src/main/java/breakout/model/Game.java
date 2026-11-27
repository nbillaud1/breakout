package breakout.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Une partie de casse-briques, sans JavaFX : la vue lit l'état et le dessine.
 * Les coordonnées sont celles d'un terrain fixe de W x H ; la vue les met à l'échelle de la fenêtre.
 */
public class Game {
	public static final double W = 700, H = 800;
	public static final double HUD_H = 48, WALL_TOP = 60;
	public static final double BRICK_W = W / Wall.COLS, BRICK_H = 30;
	public static final double PAD_W = 118, PAD_H = 20, PAD_Y = H - 70 - PAD_H;
	public static final double RADIUS = 10;
	public static final int START_LIVES = 3;
	private static final double MAX_SPEED = 760, PAD_SPEED = 700; // pixels par seconde
	private static final double WALL_DROP_SPEED = 90; // le mur glisse d'un cran au lieu de sauter
	private static final double MAX_BOUNCE_ANGLE = Math.toRadians(60);

	public enum State { READY, PLAYING, LEVEL_CLEARED, LOST, WON }

	public enum EventType { BRICK_HIT, BRICK_BROKEN, PAD_HIT, LIFE_LOST, WALL_DOWN, LEVEL_CLEARED, GAME_OVER, WON }

	/** Ce qui vient de se passer : la vue s'en sert pour les sons et les animations. row et col valent -1 hors briques. */
	public record Event(EventType type, int row, int col, int points) {}

	private final List<Level> levels;
	private final List<Event> events = new ArrayList<>();
	private int levelIndex, lives = START_LIVES, score, combo;
	private Wall wall;
	private State state;
	private double padX = (W - PAD_W) / 2, ballX, ballY, vx, vy;
	private double wallY, wallTargetY, descentTimer;
	private boolean left, right;

	public Game() {
		this(Level.ALL);
	}

	/** Pour les tests : une partie sur des niveaux choisis. */
	Game(List<Level> levels) {
		this.levels = levels;
		loadLevel(0);
	}

	private void loadLevel(int index) {
		levelIndex = index;
		wall = new Wall(level().rows());
		wallY = wallTargetY = descentTimer = 0;
		combo = 0;
		state = State.READY;
		placeBallOnPad();
	}

	public void launch() {
		if (state != State.READY) return;
		state = State.PLAYING;
		double speed = level().ballSpeed();
		vx = speed * 0.6;
		vy = -speed * 0.8;
	}

	/** Après un niveau terminé : passe au suivant. */
	public void nextLevel() {
		if (state == State.LEVEL_CLEARED) loadLevel(levelIndex + 1);
	}

	public void setLeft(boolean pressed) { left = pressed; }
	public void setRight(boolean pressed) { right = pressed; }

	/** Centre le pad sur x (souris). */
	public void movePadTo(double x) {
		padX = clamp(x - PAD_W / 2, 0, W - PAD_W);
		if (state == State.READY) placeBallOnPad();
	}

	/** Avance la partie de dt secondes. Ce qui s'est passé est ensuite lisible dans events(). */
	public void update(double dt) {
		events.clear();
		if (state != State.READY && state != State.PLAYING) return;
		if (left) movePadTo(padX + PAD_W / 2 - PAD_SPEED * dt);
		if (right) movePadTo(padX + PAD_W / 2 + PAD_SPEED * dt);
		if (state == State.READY) return;

		descentTimer += dt;
		if (descentTimer >= level().descentEvery()) {
			descentTimer -= level().descentEvery();
			wallTargetY += BRICK_H;
			events.add(new Event(EventType.WALL_DOWN, -1, -1, 0));
		}
		wallY = Math.min(wallTargetY, wallY + WALL_DROP_SPEED * dt);
		if (lowestBrickBottom() >= PAD_Y) {
			state = State.LOST;
			events.add(new Event(EventType.GAME_OVER, -1, -1, 0));
			return;
		}

		ballX += vx * dt;
		ballY += vy * dt;
		if (ballX < RADIUS) { ballX = RADIUS; vx = Math.abs(vx); }
		if (ballX > W - RADIUS) { ballX = W - RADIUS; vx = -Math.abs(vx); }
		if (ballY < HUD_H + RADIUS) { ballY = HUD_H + RADIUS; vy = Math.abs(vy); }
		if (ballY > H + RADIUS) {
			loseLife();
			return;
		}

		if (vy > 0 && ballY + RADIUS >= PAD_Y && ballY - RADIUS <= PAD_Y + PAD_H
				&& ballX >= padX - RADIUS && ballX <= padX + PAD_W + RADIUS) {
			// L'angle de rebond dépend de l'endroit touché : bord du pad = rebond très incliné.
			double hit = clamp((ballX - padX - PAD_W / 2) / (PAD_W / 2), -1, 1);
			double speed = Math.hypot(vx, vy);
			vx = speed * Math.sin(hit * MAX_BOUNCE_ANGLE);
			vy = -speed * Math.cos(hit * MAX_BOUNCE_ANGLE);
			ballY = PAD_Y - RADIUS;
			combo = 0;
			events.add(new Event(EventType.PAD_HIT, -1, -1, 0));
		}
		hitBrick();
	}

	private void loseLife() {
		lives--;
		combo = 0;
		events.add(new Event(EventType.LIFE_LOST, -1, -1, 0));
		if (lives == 0) {
			state = State.LOST;
			events.add(new Event(EventType.GAME_OVER, -1, -1, 0));
		} else {
			state = State.READY;
			placeBallOnPad();
		}
	}

	private void hitBrick() {
		for (int row = 0; row < wall.rows(); row++) {
			for (int col = 0; col < Wall.COLS; col++) {
				Brick brick = wall.brick(row, col);
				if (brick.difficulty() == 0) continue;
				double bx = brickX(col), by = brickY(row);
				double dx = ballX - clamp(ballX, bx, bx + BRICK_W);
				double dy = ballY - clamp(ballY, by, by + BRICK_H);
				if (dx * dx + dy * dy > RADIUS * RADIUS) continue;

				// On renvoie la balle du côté de la brique qu'elle a touché et on la sort de la brique :
				// sinon elle la chevauche encore à l'image suivante et la même touche compte plusieurs fois.
				if (Math.abs(dx) > Math.abs(dy)) {
					vx = Math.signum(dx) * Math.abs(vx);
					ballX = (dx > 0 ? bx + BRICK_W : bx) + Math.signum(dx) * RADIUS;
				} else {
					boolean below = dy > 0 || (dy == 0 && vy < 0); // centre dans la brique : on la renvoie d'où elle vient
					vy = below ? Math.abs(vy) : -Math.abs(vy);
					ballY = below ? by + BRICK_H + RADIUS : by - RADIUS;
				}

				double speed = Math.hypot(vx, vy);
				double factor = Math.min(speed * 1.015, MAX_SPEED) / speed;
				vx *= factor;
				vy *= factor;

				// Le combo monte à chaque brique touchée sans repasser par le pad.
				combo++;
				int points = 10 * combo;
				score += points;
				brick.setDifficulty(brick.difficulty() - 1);
				if (brick.difficulty() > 0) {
					events.add(new Event(EventType.BRICK_HIT, row, col, points));
					return;
				}
				events.add(new Event(EventType.BRICK_BROKEN, row, col, points));
				if (wall.aliveCount() == 0) {
					int bonus = 500 * (levelIndex + 1);
					score += bonus;
					boolean last = levelIndex == levels.size() - 1;
					state = last ? State.WON : State.LEVEL_CLEARED;
					events.add(new Event(last ? EventType.WON : EventType.LEVEL_CLEARED, -1, -1, bonus));
				}
				return;
			}
		}
	}

	/** Bas de la rangée de briques la plus basse encore debout (0 s'il n'y en a plus). */
	public double lowestBrickBottom() {
		for (int row = wall.rows() - 1; row >= 0; row--) {
			for (int col = 0; col < Wall.COLS; col++) {
				if (wall.brick(row, col).difficulty() > 0) return brickY(row) + BRICK_H;
			}
		}
		return 0;
	}

	/** Pour les tests : place la balle en jeu à une position et une vitesse données. */
	void placeBall(double x, double y, double speedX, double speedY) {
		state = State.PLAYING;
		ballX = x;
		ballY = y;
		vx = speedX;
		vy = speedY;
	}

	private void placeBallOnPad() {
		ballX = padX + PAD_W / 2;
		ballY = PAD_Y - RADIUS;
	}

	private Level level() {
		return levels.get(levelIndex);
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	public double brickX(int col) { return col * BRICK_W; }
	public double brickY(int row) { return WALL_TOP + wallY + row * BRICK_H; }
	public List<Event> events() { return events; }
	public State state() { return state; }
	public Wall wall() { return wall; }
	public double padX() { return padX; }
	public double ballX() { return ballX; }
	public double ballY() { return ballY; }
	public int lives() { return lives; }
	public int score() { return score; }
	public int combo() { return combo; }
	public int levelNumber() { return levelIndex + 1; }
	public int levelCount() { return levels.size(); }
	public String levelName() { return level().name(); }
	/** Avancée vers la prochaine descente du mur, de 0 à 1. */
	public double descentProgress() { return descentTimer / level().descentEvery(); }
	public double timeBeforeDescent() { return level().descentEvery() - descentTimer; }
}
