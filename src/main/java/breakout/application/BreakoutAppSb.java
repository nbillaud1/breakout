package breakout.application;

import breakout.controller.GameViewController;
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
		Scene scene = new Scene(root, 700, 800);
		
		GameViewController controller = loader.getController(); 
	    controller.setScene(scene); 							// on donne la scene pour savoir quoi controller au clavier.
	    controller.movePad();
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Breakout!");
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
