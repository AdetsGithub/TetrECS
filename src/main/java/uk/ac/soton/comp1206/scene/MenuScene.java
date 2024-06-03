package uk.ac.soton.comp1206.scene;

import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.Menu;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private Menu menu;

    /**
     * Create a new menu scene
     *
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        Multimedia.playBackgroundMusic("menu.mp3", true);
        scene.setOnKeyPressed(this::handleKey);
    }

    /**
     * Handle a key press.
     *
     * @param keyEvent event object
     */
    private void handleKey(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            App.getInstance().shutdown();
        }
        if (keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.W)) {
            menu.up();
        }
        if (keyEvent.getCode().equals(KeyCode.DOWN) || keyEvent.getCode().equals(KeyCode.S)) {
            menu.down();
        }
        if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.SPACE)) {
            menu.select();
        }
    }

    /**
     * Build UI elements of menu scene.
     */
    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        // Create game pane
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        // Setup stack pane
        StackPane menuPane = generateMainPane("menu-background");

        BorderPane mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // Generate menu image
        ImageView image = new ImageView(Multimedia.getImage("TetrECS.png"));
        image.setFitWidth(gameWindow.getHeight());
        image.setPreserveRatio(true);
        mainPane.setCenter(image);

        // Apply scale transition to menu image
        ScaleTransition st = new ScaleTransition(Duration.millis(20000), image);
        st.setByX(1.5);
        st.setByY(1.5);
        st.setCycleCount(-1);
        st.setAutoReverse(true);

        st.play();

        // Generate menu buttons
        menu = new Menu();
        BorderPane.setAlignment(menu, Pos.CENTER);
        menu.addItem("SinglePlayer", gameWindow::startChallenge);
        menu.addItem("MultiPlayer", gameWindow::startLobby);
        menu.addItem("How to Play", gameWindow::startInstructions);
        menu.addItem("Leaderboard", gameWindow::startLeaderBoard);
        menu.addItem("Exit", () -> App.getInstance().shutdown());
        mainPane.setBottom(menu);
    }
}

