package breakout.view;

import breakout.audio.MusicManager;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Écran de jeu : dessine la partie sur un Canvas qui suit la taille de la fenêtre.
 * Le terrain garde ses proportions et reste centré, quelle que soit la taille.
 */
public class GameView extends Pane {
	// Couleur d'une brique selon ses points de vie (index = difficulté).
	private static final Color[] BRICK_COLORS = { null, Color.web("#00ff00"), Color.web("#0088ff"),
			Color.web("#ff9900"), Color.web("#ff0000"), Color.web("#a400ff") };

	private final Canvas canvas = new Canvas();
	private final String theme;
	private final Runnable onMenu;
	private Game game = new Game();
	private boolean paused;

	private final AnimationTimer timer = new AnimationTimer() {
		private long last;

		@Override
		public void handle(long now) {
			// Pas de temps plafonné à 20 ms : après un gel de la fenêtre, la balle ne traverse pas les briques.
			double dt = last == 0 ? 0 : Math.min((now - last) / 1e9, 0.02);
			last = now;
			if (!paused && game.update(dt)) MusicManager.DROPSOUND.play();
			draw();
		}
	};

	/** theme peut valoir null (version d'origine). onMenu est appelé quand le joueur revient au menu. */
	public GameView(String theme, Runnable onMenu) {
		this.theme = theme;
		this.onMenu = onMenu;
		getChildren().add(canvas);
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());
		setFocusTraversable(true);
		addEventFilter(KeyEvent.KEY_PRESSED, e -> onKey(e.getCode(), true));
		addEventFilter(KeyEvent.KEY_RELEASED, e -> onKey(e.getCode(), false));
		setOnMouseMoved(e -> { if (!paused) game.movePadTo(toFieldX(e.getX())); });
		setOnMouseClicked(e -> action());
		timer.start();
	}

	private void onKey(KeyCode code, boolean pressed) {
		switch (code) {
		case LEFT: game.setLeft(pressed); break;
		case RIGHT: game.setRight(pressed); break;
		case SPACE: if (pressed) action(); break;
		case P: if (pressed && game.state() == Game.State.PLAYING) paused = !paused; break;
		case ESCAPE:
			if (pressed) {
				timer.stop();
				onMenu.run();
			}
			break;
		default: return;
		}
	}

	/** Espace ou clic : lance la balle, reprend après une pause, ou rejoue après la fin. */
	private void action() {
		if (paused) paused = false;
		else if (game.state() == Game.State.READY) game.launch();
		else if (game.state() == Game.State.LOST || game.state() == Game.State.WON) game = new Game();
	}

	private double scale() {
		return Math.min(getWidth() / Game.W, getHeight() / Game.H);
	}

	private double toFieldX(double x) {
		return (x - (getWidth() - Game.W * scale()) / 2) / scale();
	}

	private void draw() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		double s = scale();
		g.setTransform(1, 0, 0, 1, 0, 0);
		g.setFill(Color.web("#2a2a33"));
		g.fillRect(0, 0, getWidth(), getHeight());
		g.translate((getWidth() - Game.W * s) / 2, (getHeight() - Game.H * s) / 2);
		g.scale(s, s);

		g.setFill(Color.WHITE);
		g.fillRect(0, 0, Game.W, Game.H);
		g.setStroke(Color.BLACK);
		g.setLineWidth(1);
		g.setTextAlign(TextAlignment.CENTER);
		g.setTextBaseline(VPos.CENTER);

		g.setFont(Font.font(14));
		for (int row = 0; row < Wall.ROWS; row++) {
			for (int col = 0; col < Wall.COLS; col++) {
				int hp = game.wall().brick(row, col).difficulty();
				if (hp == 0) continue;
				double x = col * Game.BRICK_W, y = Game.WALL_TOP + row * Game.BRICK_H;
				g.setFill(BRICK_COLORS[hp]);
				g.fillRect(x, y, Game.BRICK_W, Game.BRICK_H);
				g.strokeRect(x, y, Game.BRICK_W, Game.BRICK_H);
				g.setFill(Color.BLACK);
				g.fillText(Integer.toString(hp), x + Game.BRICK_W / 2, y + Game.BRICK_H / 2);
			}
		}

		if (theme != null) {
			g.setFont(Font.font(14));
			g.setFill(Color.BLACK);
			g.fillText(theme.toUpperCase(), Game.W / 2, Game.H / 2 + 50);
		}

		g.setFill(Color.DODGERBLUE);
		g.fillRoundRect(game.padX(), Game.PAD_Y, Game.PAD_W, Game.PAD_H, 5, 5);
		g.strokeRoundRect(game.padX(), Game.PAD_Y, Game.PAD_W, Game.PAD_H, 5, 5);
		double r = Game.RADIUS;
		g.fillOval(game.ballX() - r, game.ballY() - r, 2 * r, 2 * r);
		g.strokeOval(game.ballX() - r, game.ballY() - r, 2 * r, 2 * r);

		String title = null, hint = null;
		if (paused) { title = "Pause"; hint = "Espace ou clic pour reprendre"; }
		else if (game.state() == Game.State.READY) hint = "Espace ou clic pour lancer la balle\nFlèches ou souris pour bouger, P pour la pause, Échap pour le menu";
		else if (game.state() == Game.State.LOST) { title = "Perdu !"; hint = "Espace ou clic pour rejouer, Échap pour le menu"; }
		else if (game.state() == Game.State.WON) { title = "Gagné !"; hint = "Espace ou clic pour rejouer, Échap pour le menu"; }
		g.setFill(Color.BLACK);
		if (title != null) {
			g.setFont(Font.font(null, FontWeight.BOLD, 48));
			g.fillText(title, Game.W / 2, Game.H / 2 - 60);
		}
		if (hint != null) {
			g.setFont(Font.font(16));
			g.fillText(hint, Game.W / 2, Game.H / 2 + 110);
		}
	}
}
