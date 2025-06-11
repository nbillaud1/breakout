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
	
	private String theme;
	
	@FXML
	private void quit() {
		Platform.exit();
	}
	
	@FXML
	public void start() {
		BreakoutAppSb app = new BreakoutAppSb();
		System.out.println(idCbTheme.getValue());
		if(idCbTheme.getValue() == null) {
			app.startTheOriginalVersion();
		}
		else {
			switch(idCbTheme.getValue()) {
			case "Montagne": app.startTheMountainVersion();
			case "Plage": app.startTheBeachVersion();
			case "Forêt": app.startTheForestVersion();
			}
		}
	}
	
	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	
	public void initialize() {
		idCbTheme.getItems().addAll("Montagne", "Plage", "Forêt");
	}
}
