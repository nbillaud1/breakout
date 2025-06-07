package breakout.application;

import java.io.IOException;

import breakout.controller.GameViewController;
import breakout.controller.LobbyViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BreakoutAppSb extends Application {
	
	private Stage lobbyStage;
	private Stage gameStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.lobbyStage = primaryStage;
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/breakout/view/LobbyViewBreakout.fxml"));
		StackPane root = new StackPane();
		root.getChildren().add(loader.load());
		Scene scene = new Scene(root, 700, 800);
		
		/*LobbyViewController controller = loader.getController(); 
	    controller.setScene(scene); 							// on donne la scene pour savoir quoi controller au clavier.
	    controller.movePad();*/
		
		lobbyStage.setScene(scene);
		lobbyStage.setTitle("Breakout Lobby!");
		lobbyStage.setResizable(false);
		lobbyStage.initStyle(StageStyle.UNDECORATED);
		lobbyStage.show();
	}
	
	public void startTheGame() {
		this.gameStage = new Stage();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/breakout/view/GameViewBreakout.fxml"));
		StackPane root = new StackPane();
		try {
			root.getChildren().add(loader.load());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scene scene = new Scene(root, 700, 800);
		
		GameViewController controller = loader.getController(); 
	    controller.setScene(scene); 							// on donne la scene pour savoir quoi controller au clavier.
	    controller.movePad();
		
		gameStage.setScene(scene);
		gameStage.setTitle("Breakout!");
		gameStage.setResizable(false);
		gameStage.initStyle(StageStyle.UNDECORATED);
		gameStage.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
