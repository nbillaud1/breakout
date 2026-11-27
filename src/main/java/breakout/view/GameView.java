package breakout.view;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.prefs.Preferences;

import breakout.audio.MusicManager;
import breakout.model.Brick;
import breakout.model.Game;
import breakout.model.Wall;
import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Écran de jeu : dessine la partie et ses effets sur un Canvas qui suit la taille de la fenêtre.
 * Le terrain garde ses proportions et reste centré ; le décor remplit toute la fenêtre.
 */
public class GameView extends Pane {
	private static final Preferences PREFS = Preferences.userNodeForPackage(GameView.class);
	private static final double W = Game.W, H = Game.H;
	private static final double BANNER_DURATION = 2.4;
	private static final Font BIG = Font.font("Segoe UI Black", 52);
	private static final Font MEDIUM = Font.font("Segoe UI", FontWeight.BOLD, 26);
	private static final Font SMALL = Font.font("Segoe UI", FontWeight.BOLD, 19);
	private static final Font HUD = Font.font("Segoe UI", FontWeight.BOLD, 20);
	private static final Random RANDOM = new Random();

	/** Meilleur score, gardé d'une partie à l'autre. */
	public static int bestScore() {
		return PREFS.getInt("best", 0);
	}

	private static final class Particle {
		double x, y, vx, vy, life, maxLife, size, rot, spin;
		Color color;
	}

	private static final class Floater {
		double x, y, life = 0.9;
		String text;
	}

	private final Canvas canvas = new Canvas();
	private final Theme theme;
	private final Runnable onMenu;
	private Game game = new Game();
	private boolean paused, newRecord;
	private int best = bestScore(), lastBonus;
	private double time, shake, padPulse, hurt, bannerTime, overlayTime;
	private Game.State lastState;
	private final List<Particle> particles = new ArrayList<>();
	private final List<Floater> floaters = new ArrayList<>();
	private final ArrayDeque<double[]> trail = new ArrayDeque<>();
	private final Map<Integer, Double> flashes = new HashMap<>();

	private final AnimationTimer timer = new AnimationTimer() {
		private long last;

		@Override
		public void handle(long now) {
			// Pas de temps plafonné à 20 ms : après un gel de la fenêtre, la balle ne traverse pas les briques.
			double dt = last == 0 ? 0 : Math.min((now - last) / 1e9, 0.02);
			last = now;
			time += dt;
			if (!paused) {
				game.update(dt);
				for (Game.Event e : game.events()) onEvent(e);
			}
			animate(paused ? 0 : dt);
			draw();
		}
	};

	/** onMenu est appelé quand le joueur revient au menu. */
	public GameView(Theme theme, Runnable onMenu) {
		this.theme = theme;
		this.onMenu = onMenu;
		getChildren().add(canvas);
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());
		setFocusTraversable(true);
		addEventFilter(KeyEvent.KEY_PRESSED, e -> onKey(e.getCode(), true));
		addEventFilter(KeyEvent.KEY_RELEASED, e -> onKey(e.getCode(), false));
		setOnMouseMoved(e -> { if (!paused) game.movePadTo(toFieldX(e.getX())); });
		setOnMouseDragged(e -> { if (!paused) game.movePadTo(toFieldX(e.getX())); });
		setOnMouseClicked(e -> action());
		timer.start();
	}

	private void onKey(KeyCode code, boolean pressed) {
		switch (code) {
		case LEFT -> game.setLeft(pressed);
		case RIGHT -> game.setRight(pressed);
		case SPACE -> { if (pressed) action(); }
		case P -> { if (pressed && game.state() == Game.State.PLAYING) paused = !paused; }
		case ESCAPE -> {
			if (pressed) {
				timer.stop();
				onMenu.run();
			}
		}
		default -> { }
		}
	}

	/** Espace ou clic : lance la balle, reprend après une pause, passe au niveau suivant ou rejoue. */
	private void action() {
		if (paused) {
			paused = false;
			return;
		}
		switch (game.state()) {
		case READY -> game.launch();
		case LEVEL_CLEARED -> {
			game.nextLevel();
			bannerTime = 0;
		}
		case LOST, WON -> {
			game = new Game();
			newRecord = false;
			particles.clear();
			bannerTime = 0;
		}
		default -> { }
		}
	}

	private void onEvent(Game.Event e) {
		double cx = game.brickX(e.col()) + Game.BRICK_W / 2, cy = game.brickY(e.row()) + Game.BRICK_H / 2;
		switch (e.type()) {
		case BRICK_HIT -> {
			MusicManager.DROPSOUND.play(0.35, 0, 1.3, 0, 1);
			flashes.put(e.row() * Wall.COLS + e.col(), 1.0);
			int hpBefore = game.wall().brick(e.row(), e.col()).difficulty() + 1;
			burst(cx, cy, 6, theme.brickColor(hpBefore), 140);
			floater(cx, cy, "+" + e.points());
		}
		case BRICK_BROKEN -> {
			MusicManager.DROPSOUND.play();
			burst(cx, cy, 20, theme.brickColor(1), 280);
			floater(cx, cy, "+" + e.points());
			shake = Math.max(shake, 0.12);
		}
		case PAD_HIT -> {
			padPulse = 1;
			MusicManager.DROPSOUND.play(0.25, 0, 0.7, 0, 1);
		}
		case LIFE_LOST -> {
			hurt = 1;
			shake = 0.35;
		}
		case WALL_DOWN -> shake = Math.max(shake, 0.2);
		case LEVEL_CLEARED -> {
			lastBonus = e.points();
			confetti();
		}
		case WON -> {
			lastBonus = e.points();
			confetti();
			saveRecord();
		}
		case GAME_OVER -> saveRecord();
		}
	}

	private void saveRecord() {
		if (game.score() > best) {
			best = game.score();
			newRecord = true;
			PREFS.putInt("best", best);
		}
	}

	private void burst(double x, double y, int count, Color color, double speed) {
		for (int i = 0; i < count; i++) {
			Particle p = new Particle();
			double angle = RANDOM.nextDouble() * Math.PI * 2, v = speed * (0.4 + 0.6 * RANDOM.nextDouble());
			p.x = x;
			p.y = y;
			p.vx = Math.cos(angle) * v;
			p.vy = Math.sin(angle) * v - 80;
			p.life = p.maxLife = 0.5 + 0.4 * RANDOM.nextDouble();
			p.size = 4 + 5 * RANDOM.nextDouble();
			p.rot = RANDOM.nextDouble() * 360;
			p.spin = (RANDOM.nextDouble() - 0.5) * 720;
			p.color = color;
			particles.add(p);
		}
	}

	private void confetti() {
		for (int hp = 1; hp <= 5; hp++) burst(W / 2, H * 0.4, 16, theme.brickColor(hp), 480);
	}

	private void floater(double x, double y, String text) {
		Floater f = new Floater();
		f.x = x;
		f.y = y;
		f.text = text;
		floaters.add(f);
	}

	private void animate(double dt) {
		for (Particle p : particles) {
			p.vy += 700 * dt;
			p.x += p.vx * dt;
			p.y += p.vy * dt;
			p.rot += p.spin * dt;
			p.life -= dt;
		}
		particles.removeIf(p -> p.life <= 0);
		for (Floater f : floaters) {
			f.y -= 45 * dt;
			f.life -= dt;
		}
		floaters.removeIf(f -> f.life <= 0);
		flashes.replaceAll((key, v) -> v - dt * 6);
		flashes.values().removeIf(v -> v <= 0);
		shake = Math.max(0, shake - dt);
		padPulse = Math.max(0, padPulse - dt * 5);
		hurt = Math.max(0, hurt - dt * 1.5);
		bannerTime += dt;
		overlayTime += dt;
		if (game.state() != lastState) {
			lastState = game.state();
			overlayTime = 0;
		}
		if (game.state() == Game.State.PLAYING && dt > 0) {
			trail.addFirst(new double[] { game.ballX(), game.ballY() });
			while (trail.size() > 10) trail.removeLast();
		} else if (game.state() != Game.State.PLAYING) {
			trail.clear();
		}
	}

	private double scale() {
		return Math.min(getWidth() / W, getHeight() / H);
	}

	private double toFieldX(double x) {
		return (x - (getWidth() - W * scale()) / 2) / scale();
	}

	private void draw() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		double w = getWidth(), h = getHeight(), s = scale();
		double fx = (w - W * s) / 2, fy = (h - H * s) / 2, fw = W * s, fh = H * s;
		g.setTransform(1, 0, 0, 1, 0, 0);
		g.setGlobalAlpha(1);
		theme.paintCover(g, w, h, time);

		// On assombrit le décor autour du terrain.
		g.setFill(Color.rgb(0, 0, 0, 0.4));
		g.fillRect(0, 0, w, fy);
		g.fillRect(0, fy + fh, w, h - fy - fh);
		g.fillRect(0, fy, fx, fh);
		g.fillRect(fx + fw, fy, w - fx - fw, fh);

		double shakeX = (RANDOM.nextDouble() - 0.5) * 14 * shake, shakeY = (RANDOM.nextDouble() - 0.5) * 14 * shake;
		g.translate(fx + shakeX, fy + shakeY);
		g.scale(s, s);
		g.save();
		g.beginPath();
		g.rect(0, 0, W, H);
		g.clip();

		g.setTextAlign(TextAlignment.CENTER);
		g.setTextBaseline(VPos.CENTER);
		drawDangerLine(g);
		drawBricks(g);
		drawParticles(g);
		drawPad(g);
		drawBall(g);
		for (Floater f : floaters) {
			text(g, f.text, f.x, f.y, SMALL, Color.gray(1, Math.min(1, f.life * 2)));
		}
		if (hurt > 0) {
			g.setFill(Color.rgb(255, 40, 40, 0.3 * hurt));
			g.fillRect(0, 0, W, H);
		}
		drawHud(g);
		drawBanner(g);
		drawOverlay(g);
		g.restore();

		g.setStroke(Color.rgb(255, 255, 255, 0.5));
		g.setLineWidth(2);
		g.strokeRect(0, 0, W, H);
	}

	/** Ligne rouge qui clignote quand le mur approche du pad. */
	private void drawDangerLine(GraphicsContext g) {
		if (Game.PAD_Y - game.lowestBrickBottom() > 4 * Game.BRICK_H) return;
		g.setStroke(Color.web("#ff4d4d", 0.35 + 0.4 * (0.5 + 0.5 * Math.sin(time * 8))));
		g.setLineWidth(3);
		g.setLineDashes(14, 10);
		g.strokeLine(0, Game.PAD_Y - 3, W, Game.PAD_Y - 3);
		g.setLineDashes((double[]) null);
	}

	private void drawBricks(GraphicsContext g) {
		// Le mur tremble pendant la seconde et demie qui précède sa descente.
		double jitter = game.state() == Game.State.PLAYING && game.timeBeforeDescent() < 1.5 ? Math.sin(time * 50) * 1.5 : 0;
		Wall wall = game.wall();
		for (int row = 0; row < wall.rows(); row++) {
			for (int col = 0; col < Wall.COLS; col++) {
				Brick brick = wall.brick(row, col);
				int hp = brick.difficulty();
				if (hp == 0) continue;
				double x = game.brickX(col) + 2 + jitter, y = game.brickY(row) + 2;
				double bw = Game.BRICK_W - 4, bh = Game.BRICK_H - 4;
				Color c = theme.brickColor(hp);
				g.setFill(Color.rgb(0, 0, 0, 0.25));
				g.fillRoundRect(x, y + 3, bw, bh, 10, 10);
				g.setFill(new LinearGradient(0, y, 0, y + bh, false, CycleMethod.NO_CYCLE,
						new Stop(0, c.deriveColor(0, 0.85, 1.2, 1)), new Stop(1, c.deriveColor(0, 1.1, 0.78, 1))));
				g.fillRoundRect(x, y, bw, bh, 10, 10);
				g.setStroke(c.deriveColor(0, 1, 0.55, 1));
				g.setLineWidth(1.5);
				g.strokeRoundRect(x, y, bw, bh, 10, 10);
				g.setStroke(Color.rgb(255, 255, 255, 0.5));
				g.setLineWidth(2);
				g.strokeLine(x + 7, y + 4, x + bw - 7, y + 4);
				if (brick.initialDifficulty() > hp) cracks(g, x, y, bw, bh, row * 31 + col * 17, brick.initialDifficulty() - hp);
				// Un point par point de vie restant.
				if (hp > 1) {
					g.setFill(Color.rgb(255, 255, 255, 0.9));
					for (int i = 0; i < hp; i++) {
						double px = x + bw / 2 + (i - (hp - 1) / 2.0) * 9;
						g.fillOval(px - 2.5, y + bh / 2 - 1.5, 5, 5);
					}
				}
				Double flash = flashes.get(row * Wall.COLS + col);
				if (flash != null) {
					g.setFill(Color.rgb(255, 255, 255, 0.75 * flash));
					g.fillRoundRect(x, y, bw, bh, 10, 10);
				}
			}
		}
	}

	private static void cracks(GraphicsContext g, double x, double y, double w, double h, int seed, int damage) {
		Random r = new Random(seed); // même graine à chaque image : les fissures ne bougent pas
		g.setStroke(Color.rgb(0, 0, 0, 0.45));
		g.setLineWidth(1.2);
		for (int k = 0; k < damage; k++) {
			double cx = x + w * (0.2 + 0.6 * r.nextDouble()), cy = y + h * (0.3 + 0.4 * r.nextDouble());
			for (int j = 0; j < 3; j++) {
				double a = r.nextDouble() * Math.PI * 2, l = 6 + 8 * r.nextDouble();
				g.strokeLine(cx, cy, cx + Math.cos(a) * l, cy + Math.sin(a) * l * 0.6);
			}
		}
	}

	private void drawParticles(GraphicsContext g) {
		for (Particle p : particles) {
			g.save();
			g.translate(p.x, p.y);
			g.rotate(p.rot);
			g.setGlobalAlpha(Math.max(0, p.life / p.maxLife));
			g.setFill(p.color);
			g.fillRect(-p.size / 2, -p.size * 0.3, p.size, p.size * 0.6);
			g.restore();
		}
	}

	private void drawPad(GraphicsContext g) {
		// Le pad s'écrase un instant quand la balle le touche.
		double pw = Game.PAD_W * (1 + 0.1 * padPulse), ph = Game.PAD_H * (1 - 0.25 * padPulse);
		double px = game.padX() + (Game.PAD_W - pw) / 2, py = Game.PAD_Y + (Game.PAD_H - ph);
		g.setFill(Color.rgb(0, 0, 0, 0.3));
		g.fillRoundRect(px + 2, py + 5, pw, ph, ph, ph);
		g.setFill(new LinearGradient(0, py, 0, py + ph, false, CycleMethod.NO_CYCLE,
				new Stop(0, theme.accent.deriveColor(0, 0.8, 1.25, 1)), new Stop(1, theme.accent.deriveColor(0, 1.1, 0.7, 1))));
		g.fillRoundRect(px, py, pw, ph, ph, ph);
		g.setStroke(Color.rgb(255, 255, 255, 0.6));
		g.setLineWidth(2);
		g.strokeLine(px + ph / 2, py + 4, px + pw - ph / 2, py + 4);
	}

	private void drawBall(GraphicsContext g) {
		double r = Game.RADIUS;
		int i = 0;
		for (double[] p : trail) {
			double k = 1 - (double) i++ / trail.size();
			double tr = r * (0.4 + 0.6 * k);
			g.setFill(theme.ball.deriveColor(0, 1, 1, 0.3 * k));
			g.fillOval(p[0] - tr, p[1] - tr, 2 * tr, 2 * tr);
		}
		double bx = game.ballX(), by = game.ballY();
		g.setFill(Color.rgb(0, 0, 0, 0.3));
		g.fillOval(bx - r + 2, by - r + 4, 2 * r, 2 * r);
		g.setFill(new RadialGradient(0, 0, bx - 3, by - 3, r * 1.4, false, CycleMethod.NO_CYCLE,
				new Stop(0, Color.WHITE), new Stop(1, theme.ball.deriveColor(0, 1, 0.8, 1))));
		g.fillOval(bx - r, by - r, 2 * r, 2 * r);
		g.setStroke(Color.rgb(0, 0, 0, 0.35));
		g.setLineWidth(1.5);
		g.strokeOval(bx - r, by - r, 2 * r, 2 * r);
	}

	private void drawHud(GraphicsContext g) {
		double y = Game.HUD_H / 2 - 2;
		g.setFill(Color.rgb(0, 0, 0, 0.45));
		g.fillRect(0, 0, W, Game.HUD_H);
		g.setTextAlign(TextAlignment.LEFT);
		text(g, "Score " + game.score(), 16, y, HUD, Color.WHITE);
		if (game.combo() > 1) text(g, "x" + game.combo(), 190, y, HUD, theme.accent);
		g.setTextAlign(TextAlignment.CENTER);
		text(g, "Niveau " + game.levelNumber() + "/" + game.levelCount(), W / 2, y, HUD, Color.WHITE);
		for (int i = 0; i < game.lives(); i++) {
			double cx = W - 22 - i * 26;
			g.setFill(theme.ball);
			g.fillOval(cx - 8, y - 8, 16, 16);
			g.setStroke(Color.rgb(0, 0, 0, 0.5));
			g.setLineWidth(1.5);
			g.strokeOval(cx - 8, y - 8, 16, 16);
		}
		// Jauge avant la prochaine descente du mur.
		double p = game.descentProgress();
		g.setFill(Color.rgb(255, 255, 255, 0.15));
		g.fillRect(0, Game.HUD_H - 4, W, 4);
		g.setFill(p > 0.8 ? Color.web("#ff5a5a") : theme.accent);
		g.fillRect(0, Game.HUD_H - 4, W * p, 4);
	}

	/** « Niveau 2 » qui grossit, se pose, puis s'efface au début de chaque niveau. */
	private void drawBanner(GraphicsContext g) {
		if (bannerTime >= BANNER_DURATION) return;
		double alpha = Math.min(1, bannerTime / 0.3) * Math.min(1, (BANNER_DURATION - bannerTime) / 0.6);
		double zoom = 1 + 0.35 * Math.max(0, 1 - bannerTime / 0.3);
		g.save();
		g.translate(W / 2, H * 0.42);
		g.scale(zoom, zoom);
		g.setGlobalAlpha(alpha);
		text(g, "NIVEAU " + game.levelNumber(), 0, 0, BIG, Color.WHITE);
		text(g, game.levelName(), 0, 52, MEDIUM, theme.accent);
		g.restore();
	}

	private void drawOverlay(GraphicsContext g) {
		String record = newRecord ? "Nouveau record !" : "Record : " + best;
		String again = "Espace ou clic pour rejouer, Échap pour le menu";
		if (paused) {
			panel(g, "PAUSE", Color.WHITE, "Espace ou clic pour reprendre");
			return;
		}
		switch (game.state()) {
		case READY -> {
			double y = H * 0.64;
			if (game.lives() < Game.START_LIVES) {
				text(g, game.lives() == 1 ? "Dernière vie !" : "Encore " + game.lives() + " vies", W / 2, y - 40, MEDIUM, theme.accent);
			}
			text(g, "Espace ou clic pour lancer la balle", W / 2, y, SMALL, Color.WHITE);
			text(g, "Flèches ou souris pour bouger, P pour la pause", W / 2, y + 30, SMALL, Color.gray(1, 0.75));
		}
		case LEVEL_CLEARED -> panel(g, "NIVEAU TERMINÉ", theme.accent,
				"Bonus +" + lastBonus, "Score " + game.score(), "Espace ou clic pour le niveau suivant");
		case LOST -> panel(g, "PERDU", Color.web("#ff6b6b"),
				game.lives() > 0 ? "Le mur a atteint ta raquette" : "Plus de vies", "Score " + game.score(), record, again);
		case WON -> panel(g, "VICTOIRE !", theme.accent,
				"Tous les niveaux sont terminés", "Score " + game.score(), record, again);
		default -> { }
		}
	}

	/** Panneau centré qui apparaît en zoomant légèrement. */
	private void panel(GraphicsContext g, String title, Color titleColor, String... lines) {
		double appear = Math.min(1, overlayTime / 0.25);
		double ph = 120 + lines.length * 36, pw = 580;
		g.save();
		g.translate(W / 2, H / 2);
		g.scale(0.85 + 0.15 * appear, 0.85 + 0.15 * appear);
		g.setGlobalAlpha(appear);
		g.setFill(Color.rgb(8, 10, 28, 0.8));
		g.fillRoundRect(-pw / 2, -ph / 2, pw, ph, 30, 30);
		g.setStroke(theme.accent.deriveColor(0, 1, 1, 0.8));
		g.setLineWidth(2);
		g.strokeRoundRect(-pw / 2, -ph / 2, pw, ph, 30, 30);
		g.setTextAlign(TextAlignment.CENTER);
		text(g, title, 0, -ph / 2 + 58, BIG, titleColor);
		for (int i = 0; i < lines.length; i++) {
			boolean hint = i == lines.length - 1;
			text(g, lines[i], 0, -ph / 2 + 118 + i * 36, SMALL, hint ? Color.gray(1, 0.7) : Color.WHITE);
		}
		g.restore();
	}

	/** Texte avec une ombre portée, lisible sur n'importe quel décor. */
	private static void text(GraphicsContext g, String s, double x, double y, Font font, Color color) {
		g.setFont(font);
		g.setFill(Color.rgb(0, 0, 0, 0.5 * color.getOpacity()));
		g.fillText(s, x + 2, y + 3);
		g.setFill(color);
		g.fillText(s, x, y);
	}
}
