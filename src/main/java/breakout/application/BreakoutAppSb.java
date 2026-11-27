package breakout.application;

import java.io.IOException;
import java.io.UncheckedIOException;

import breakout.view.GameView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BreakoutAppSb extends Application {

	// Une seule fenêtre : le menu et le jeu remplacent tour à tour son contenu.
	private static Stage stage;

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		stage.setScene(new Scene(new Pane(), 700, 800));
		stage.setTitle("Breakout");
		stage.setMinWidth(360);
		stage.setMinHeight(420);
		showLobby();
		stage.show();
	}

	public static void showLobby() {
		try {
			stage.getScene().setRoot(FXMLLoader.load(BreakoutAppSb.class.getResource("/breakout/view/LobbyViewBreakout.fxml")));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** theme vaut null pour la version d'origine. */
	public static void startGame(String theme) {
		GameView game = new GameView(theme, BreakoutAppSb::showLobby);
		stage.getScene().setRoot(game);
		game.requestFocus(); // sinon les touches n'arrivent pas au jeu
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

}
