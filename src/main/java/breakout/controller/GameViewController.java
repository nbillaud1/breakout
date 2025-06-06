package breakout.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class GameViewController implements EventHandler<MouseEvent>{
	
	@FXML
	private Button idBtnQuit;
	
	@FXML
	private Rectangle idPad;
	
	@FXML
	private Circle idBall;
	
	@FXML
	private void quit() {
		Platform.exit();
	}
	
	private Scene scene;
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void initialize() {
		Timeline timeline = animateBall(idBall);
	    timeline.play();
	}
	
	 private Timeline animateBall (Circle ball) {
	        KeyValue kv = new KeyValue(ball.translateYProperty(), ball.translateYProperty().get() + 300, Interpolator.EASE_IN);
	        KeyFrame kf = new KeyFrame(Duration.millis(1000), kv);
	        Timeline timeline = new Timeline();
	        timeline.getKeyFrames().add(kf);
	        return timeline;
	 }

	@Override
	public void handle(MouseEvent event) {
		// TODO Auto-generated method stub
	}

	public void movePad() {
		scene.setOnKeyPressed(e -> {
		    double gap = 10;
		    if (e.getCode() == KeyCode.LEFT) gap = -gap;
		    else if (e.getCode() != KeyCode.RIGHT) return;

		    double newX = idPad.getTranslateX() + gap;
		    double halfScene = scene.getWidth() / 2;
		    double limit = halfScene - idPad.getWidth() / 2;

		    idPad.setTranslateX(Math.max(-limit, Math.min(limit, newX))); // empêche de dépasser les limites de la scène.
		});
	}
	
}
