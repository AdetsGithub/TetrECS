package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * Intro scene extends base scene.
 * Plays a sequence before menu screen appears.
 */
public class IntroScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(IntroScene.class);

    /**
     * Create intro scene.
     *
     * @param gameWindow the game window
     */
    public IntroScene(GameWindow gameWindow) {
        super(gameWindow);

        logger.info("Creating Intro Scene");

        Multimedia.playAudio("intro.mp3");
    }

    /**
     * Override initialise method.
     * Not required to do anything here.
     */
    @Override
    public void initialise() {
    }

    /**
     * Build UI elements of intro scene.
     */
    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        // Set up stack pane
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        StackPane introPane = new StackPane();
        introPane.setMaxWidth(gameWindow.getWidth());
        introPane.setMaxHeight(gameWindow.getHeight());
        introPane.getStyleClass().add("intro");

        // Generate game logo
        ImageView logo = new ImageView(Multimedia.getImage("ECSGames.png"));
        logo.setFitWidth(gameWindow.getWidth() / 3.0);
        logo.setPreserveRatio(true);
        logo.setOpacity(0.0);
        introPane.getChildren().add(logo);
        root.getChildren().add(introPane);

        // Generate fade transition
        FadeTransition fadeIn = new FadeTransition(new Duration(2000.0), logo);
        fadeIn.setToValue(1.0);
        PauseTransition pause = new PauseTransition(new Duration(1500.0));
        FadeTransition fadeOut = new FadeTransition(new Duration(500.0), logo);
        fadeOut.setToValue(0.0);
        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();
        sequence.setOnFinished(e -> gameWindow.startMenu());
    }
}

