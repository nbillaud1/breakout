package breakout.controller;

import breakout.audio.MusicManager;
import breakout.model.Wall;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
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
	private Boolean collision = false;
	
	private double ballSpeedX = 2;
	private double ballSpeedY = 2;
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void initialize() {
		Timeline timeline = animateBall(idBall);
		timeline.play();
	}
	
	private Timeline animateBall(Circle ball) {
		Timeline timeline = new Timeline();

	    KeyFrame keyFrame = new KeyFrame(Duration.millis(16), e -> {
	    	ball.setTranslateX(ball.getTranslateX() - ballSpeedX);
	        ball.setTranslateY(ball.getTranslateY() - ballSpeedY);

	        Bounds bounds = ball.getParent().getLayoutBounds();
	        double radius = ball.getRadius();

	        double nextX = ball.getTranslateX() + ball.getLayoutX();
	        double nextY = ball.getTranslateY() + ball.getLayoutY();

	        if (nextX <= radius || nextX >= bounds.getWidth() - radius) {
	            ballSpeedX = -ballSpeedX;
	        }

	        if (nextY <= radius || nextY >= bounds.getHeight() - radius) {
	            ballSpeedY = -ballSpeedY;
	        }
	        
	        for (Node brick : idWall.getChildren()) {
	            if (brick.isVisible() && ball.getBoundsInParent().intersects(brick.getBoundsInParent())) {
	            	if(((Labeled) brick).getText().compareTo("1") > 0) {
	            		updateBrick(brick);
	            		ballSpeedY = -ballSpeedY -1;
		                ballSpeedX = -ballSpeedX -1;
		                break;
	            	}
	            	else {
	            		MusicManager.DROPSOUND.play();
		                brick.setVisible(false); // La brique "casse"
		                ballSpeedY = -ballSpeedY -1;
		                ballSpeedX = -ballSpeedX -1;
		                break;
	            	}
	            }
	        }
	    });
	    
	    timeline.getKeyFrames().add(keyFrame);
	    timeline.setCycleCount(Timeline.INDEFINITE);
	    return timeline;
	}
	
	public static void updateBrick(Node brick) {
		String brickStyle = "-fx-background-color:";
		switch(((Labeled) brick).getText()) {
		case "5":
			brickStyle += "#ff0000";
		case "4":
			brickStyle += "#ff9900";
		case "3":
			brickStyle += "#0088ff";
		case "2":
			brickStyle += "#00ff00";
		}
		brick.setStyle(brickStyle + ";-fx-border-color:black;");
		((Labeled) brick).setText(Integer.toString(Integer.parseInt(((Labeled) brick).getText()) - 1));
		
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
