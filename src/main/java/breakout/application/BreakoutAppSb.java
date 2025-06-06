package breakout.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BreakoutAppSb extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/breakout/view/GameViewBreakout.fxml"));
		StackPane root = new StackPane();
		root.getChildren().add(loader.load());
		
		/*GameViewController controller = loader.getController(); 
		controller.setRoundConter(turnNumber);
	    controller.setPlayer1Name(this.player1Name()); // si on veux communiquer entre un lobby et le jeu en lui même.
	    controller.setPlayer2Name(this.player2Name());
	    controller.initialize();*/
		
		Scene scene = new Scene(root, 700, 800);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
