package breakout.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

/** Décor animé qui remplit son conteneur : le fond du menu. */
public class ThemeBackdrop extends Pane {
	private final Canvas canvas = new Canvas();
	private Theme theme = Theme.CLASSIQUE;

	private final AnimationTimer timer = new AnimationTimer() {
		private long start;

		@Override
		public void handle(long now) {
			if (start == 0) start = now;
			theme.paintCover(canvas.getGraphicsContext2D(), getWidth(), getHeight(), (now - start) / 1e9);
		}
	};

	public ThemeBackdrop() {
		getChildren().add(canvas);
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());
		setMouseTransparent(true);
		// L'animation ne tourne que tant que le menu est affiché.
		sceneProperty().addListener((obs, old, scene) -> {
			if (scene == null) timer.stop();
			else timer.start();
		});
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}
}
