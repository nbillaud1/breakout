package breakout.controller;

import breakout.application.BreakoutAppSb;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

public class LobbyViewController implements EventHandler<MouseEvent>{

	@Override
	public void handle(MouseEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	private Button idBtn;
	
	@FXML
	private ComboBox<String> idCbTheme;
	
	@FXML
	private Button idBtnQuit;
	
	@FXML
	private void quit() {
		Platform.exit();
	}
	
	@FXML
	public void start() {
		BreakoutAppSb app = new BreakoutAppSb();
		app.startTheGame();
	}
	
	public void initialize() {
		idCbTheme.getItems().addAll("Montagne", "Plage", "Forêt");
	}
}
