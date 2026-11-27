package breakout.model;

/**
 * Une partie de casse-briques, sans JavaFX : la vue lit l'état et le dessine.
 * Les coordonnées sont celles d'un terrain fixe de W x H ; la vue les met à l'échelle de la fenêtre.
 */
public class Game {
	public static final double W = 700, H = 800;
	public static final double BRICK_W = W / Wall.COLS, BRICK_H = 30, WALL_TOP = 5;
	public static final double PAD_W = 118, PAD_H = 23, PAD_Y = H - 60 - PAD_H;
	public static final double RADIUS = 10;
	private static final double START_SPEED = 350, MAX_SPEED = 750, PAD_SPEED = 650; // pixels par seconde
	private static final double MAX_BOUNCE_ANGLE = Math.toRadians(60);

	public enum State { READY, PLAYING, LOST, WON }

	private final Wall wall = new Wall();
	private int bricksLeft = Wall.ROWS * Wall.COLS;
	private State state = State.READY;
	private double padX = (W - PAD_W) / 2, ballX, ballY, vx, vy;
	private boolean left, right;

	public Game() {
		placeBallOnPad();
	}

	public void launch() {
		if (state != State.READY) return;
		state = State.PLAYING;
		vx = START_SPEED * 0.6;
		vy = -START_SPEED * 0.8;
	}

	public void setLeft(boolean pressed) { left = pressed; }
	public void setRight(boolean pressed) { right = pressed; }

	/** Centre le pad sur x (souris). */
	public void movePadTo(double x) {
		padX = clamp(x - PAD_W / 2, 0, W - PAD_W);
		if (state == State.READY) placeBallOnPad();
	}

	/** Avance la partie de dt secondes. Renvoie true si une brique vient d'être détruite. */
	public boolean update(double dt) {
		if (state == State.LOST || state == State.WON) return false;
		if (left) movePadTo(padX + PAD_W / 2 - PAD_SPEED * dt);
		if (right) movePadTo(padX + PAD_W / 2 + PAD_SPEED * dt);
		if (state == State.READY) return false;

		ballX += vx * dt;
		ballY += vy * dt;
		if (ballX < RADIUS) { ballX = RADIUS; vx = Math.abs(vx); }
		if (ballX > W - RADIUS) { ballX = W - RADIUS; vx = -Math.abs(vx); }
		if (ballY < RADIUS) { ballY = RADIUS; vy = Math.abs(vy); }
		if (ballY > H + RADIUS) { state = State.LOST; return false; }

		if (vy > 0 && ballY + RADIUS >= PAD_Y && ballY - RADIUS <= PAD_Y + PAD_H
				&& ballX >= padX - RADIUS && ballX <= padX + PAD_W + RADIUS) {
			// L'angle de rebond dépend de l'endroit touché : bord du pad = rebond très incliné.
			double hit = clamp((ballX - padX - PAD_W / 2) / (PAD_W / 2), -1, 1);
			double speed = Math.hypot(vx, vy);
			vx = speed * Math.sin(hit * MAX_BOUNCE_ANGLE);
			vy = -speed * Math.cos(hit * MAX_BOUNCE_ANGLE);
			ballY = PAD_Y - RADIUS;
		}
		return hitBrick();
	}

	private boolean hitBrick() {
		for (int row = 0; row < Wall.ROWS; row++) {
			for (int col = 0; col < Wall.COLS; col++) {
				Brick brick = wall.brick(row, col);
				if (brick.difficulty() == 0) continue;
				double bx = col * BRICK_W, by = WALL_TOP + row * BRICK_H;
				double dx = ballX - clamp(ballX, bx, bx + BRICK_W);
				double dy = ballY - clamp(ballY, by, by + BRICK_H);
				if (dx * dx + dy * dy > RADIUS * RADIUS) continue;

				// On renvoie la balle du côté de la brique qu'elle a touché.
				if (Math.abs(dx) > Math.abs(dy)) vx = Math.signum(dx) * Math.abs(vx);
				else if (dy != 0) vy = Math.signum(dy) * Math.abs(vy);
				else vy = -vy;

				double speed = Math.hypot(vx, vy);
				double factor = Math.min(speed * 1.02, MAX_SPEED) / speed;
				vx *= factor;
				vy *= factor;

				brick.setDifficulty(brick.difficulty() - 1);
				if (brick.difficulty() > 0) return false;
				if (--bricksLeft == 0) state = State.WON;
				return true;
			}
		}
		return false;
	}

	private void placeBallOnPad() {
		ballX = padX + PAD_W / 2;
		ballY = PAD_Y - RADIUS;
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	public State state() { return state; }
	public Wall wall() { return wall; }
	public double padX() { return padX; }
	public double ballX() { return ballX; }
	public double ballY() { return ballY; }
}
