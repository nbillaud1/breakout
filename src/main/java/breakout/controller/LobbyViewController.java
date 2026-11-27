package breakout.controller;

import breakout.application.BreakoutAppSb;
import breakout.view.GameView;
import breakout.view.Theme;
import breakout.view.ThemeBackdrop;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LobbyViewController {

	@FXML
	private StackPane idRoot;

	@FXML
	private VBox idPanel;

	@FXML
	private ToggleGroup idThemes;

	@FXML
	private Button idBtn;

	@FXML
	private Label idBest;

	private final ThemeBackdrop backdrop = new ThemeBackdrop();
	private static Theme lastTheme = Theme.CLASSIQUE; // retrouvé en revenant au menu

	@FXML
	private void quit() {
		Platform.exit();
	}

	@FXML
	public void start() {
		lastTheme = selectedTheme();
		BreakoutAppSb.startGame(lastTheme);
	}

	private Theme selectedTheme() {
		return Theme.valueOf((String) idThemes.getSelectedToggle().getUserData());
	}

	public void initialize() {
		idRoot.getChildren().add(0, backdrop); // le décor choisi s'anime derrière le menu
		idThemes.getToggles().forEach(t -> t.setSelected(lastTheme.name().equals(t.getUserData())));
		backdrop.setTheme(selectedTheme());
		idThemes.selectedToggleProperty().addListener((obs, old, selected) -> {
			if (selected == null) old.setSelected(true); // on garde toujours un décor choisi
			else backdrop.setTheme(selectedTheme());
		});
		idBest.setText(GameView.bestScore() > 0 ? "Record : " + GameView.bestScore() : "Pas encore de record");

		// Le panneau glisse et apparaît, puis le bouton Jouer respire doucement.
		FadeTransition fade = new FadeTransition(Duration.millis(500), idPanel);
		fade.setFromValue(0);
		fade.setToValue(1);
		TranslateTransition slide = new TranslateTransition(Duration.millis(500), idPanel);
		slide.setFromY(30);
		slide.setToY(0);
		slide.setInterpolator(Interpolator.EASE_OUT);
		fade.play();
		slide.play();
		ScaleTransition pulse = new ScaleTransition(Duration.millis(900), idBtn);
		pulse.setToX(1.06);
		pulse.setToY(1.06);
		pulse.setAutoReverse(true);
		pulse.setCycleCount(ScaleTransition.INDEFINITE);
		pulse.play();
	}
}
