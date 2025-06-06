package breakout.controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class GameViewController implements EventHandler<MouseEvent>{
	
	@FXML
	private Button idBtnQuit;
	
	@FXML
	private void quit() {
		Platform.exit();
	}

	@Override
	public void handle(MouseEvent event) {
		// TODO Auto-generated method stub
	}
	
}
