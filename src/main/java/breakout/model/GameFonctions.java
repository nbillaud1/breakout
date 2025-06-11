package breakout.model;

import breakout.audio.MusicManager;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Labeled;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class GameFonctions {
	
	private static double ballSpeedX = 2;
	private static double ballSpeedY = 2;
	
	public static Timeline animateBall(Circle ball, GridPane idWall, Rectangle idPad) {
		Timeline timeline = new Timeline();

	    KeyFrame keyFrame = new KeyFrame(Duration.millis(16), e -> {
	    	ball.setTranslateX(ball.getTranslateX() - ballSpeedX);
	        ball.setTranslateY(ball.getTranslateY() - ballSpeedY);

	        Bounds bounds = ball.getParent().getLayoutBounds();
	        double radius = ball.getRadius();

	        double nextX = ball.getTranslateX() + ball.getLayoutX();
	        double nextY = ball.getTranslateY() + ball.getLayoutY();

	        if (nextX <= radius || nextX >= bounds.getWidth() - radius || ball.getBoundsInParent().intersects(idPad.getBoundsInParent())) {
	            ballSpeedX = -ballSpeedX;
	        }

	        if (nextY <= radius/* || nextY >= bounds.getHeight() - radius*/|| ball.getBoundsInParent().intersects(idPad.getBoundsInParent())) {
	            ballSpeedY = -ballSpeedY;
	        }
	        
	        if( nextY >= bounds.getHeight() - radius) { //sortie d'écran
	        	shutTheGame(timeline);
	        }
	        
	        for (Node brick : idWall.getChildren()) {
	            if (brick.isVisible() && ball.getBoundsInParent().intersects(brick.getBoundsInParent())) {
	            	if(((Labeled) brick).getText().compareTo("1") > 0) {
	            		updateBrick(brick);
	            		ballSpeedY = -ballSpeedY -1;
		                ballSpeedX = ballSpeedX -1;
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
	
	public static void shutTheGame(Timeline timeline) {
		PauseTransition pause = new PauseTransition(Duration.seconds(5));
		pause.setOnFinished(event -> Platform.exit());
		pause.play();
		Alert fin = new Alert(AlertType.WARNING, "Vous avez perdu le jeu va s'eteindre.", ButtonType.OK);
		timeline.stop();
    	fin.show();
    	
	}
}
