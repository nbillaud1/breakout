package breakout.controller;

import breakout.model.GameFonctions;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class BeachGameViewController implements EventHandler<MouseEvent>{
	
	@FXML
	private Button idBtnQuit;
	
	@FXML
	private Rectangle idPad;
	
	@FXML
	private Circle idBall;
	
	@FXML
	private GridPane idWall;
	
	@FXML
	private StackPane idBrick1_9;
	
	@FXML
	private StackPane idBrick1_8;
	
	@FXML
	private void quit() {
		Platform.exit();
	}
	
	private Scene scene;
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void initialize() {
		Timeline timeline = GameFonctions.animateBall(idBall, idWall, idPad);
		timeline.play();
	}
	
	public void movePad() {
		scene.setOnKeyPressed(e -> {
		    double gap = 20;
		    if (e.getCode() == KeyCode.LEFT) gap = -gap;
		    else if (e.getCode() != KeyCode.RIGHT) return;

		    double newX = idPad.getTranslateX() + gap;
		    double halfScene = scene.getWidth() / 2;
		    double limit = halfScene - idPad.getWidth() / 2;

		    idPad.setTranslateX(Math.max(-limit, Math.min(limit, newX))); // empêche de dépasser les limites de la scène.
		});
	}

	@Override
	public void handle(MouseEvent event) {
		// TODO Auto-generated method stub
	}
	
}
