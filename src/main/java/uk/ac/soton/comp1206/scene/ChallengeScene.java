package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.*;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.SingleplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;
import uk.ac.soton.comp1206.util.Storage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected final StringProperty multString = new SimpleStringProperty("X1");
    protected IntegerProperty score = new SimpleIntegerProperty(0);
    protected IntegerProperty hiscore = new SimpleIntegerProperty(0);
    protected int keyboardX = 0;
    protected int keyboardY = 0;
    protected Timer timer;
    protected StackPane timerStack;
    protected GameBoard board;
    protected PieceBoard currentPiece;
    protected PieceBoard followingPiece;
    protected boolean chatOpen = false;
    protected Text multiplierField;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Play audio depending on lives status.
     *
     * @param observable object
     * @param oldVal     of lives
     * @param newVal     of lives
     */
    private static void setLives(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
        if (oldVal.intValue() > newVal.intValue()) {
            Multimedia.playAudio("lifelose.wav");
        } else {
            Multimedia.playAudio("lifegain.wav");
        }
    }

    /**
     * Play sound when leveling up.
     *
     * @param observable object
     * @param oldVal     old lives value
     * @param newVal     new lives value
     */
    private static void playLevelUp(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
        if (newVal.intValue() > oldVal.intValue()) {
            Multimedia.playAudio("level.wav");
        }
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        Multimedia.playBackgroundMusic("game.wav", true);

        // Initialise listeners
        game.getScoreProperty().addListener((observableValue, oldValue, newValue) -> setScore(oldValue, newValue));
        game.setOnLineCleared(this::lineCleared);
        game.getMultiplierProperty().addListener(this::setMultiplier);
        game.setOnGameLoop(this::gameLoop);
        game.setNextPieceListener(this::nextPiece);
        scene.setOnKeyPressed(this::handleKey);
        game.getLivesProperty().addListener(ChallengeScene::setLives);
        game.getLevelProperty().addListener(ChallengeScene::playLevelUp);
        game.setOnGameOver(() -> {
            endGame();
            gameWindow.startScores(game);
        });

        // Initialise score list
        ArrayList<Pair<String, Integer>> scores = Storage.loadScores();
        hiscore.set(scores.get(0).getValue());

        // Start game
        game.start();
    }

    /**
     * Build UI elements of ChallengeScene.
     */
    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        // Setup game
        setupGame();

        // Setup StackPane
        StackPane stackPane = generateMainPane("challenge-background");

        //Generate border
        BorderPane borderPane = new BorderPane();
        stackPane.getChildren().add(borderPane);
        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.0, gameWindow.getWidth() / 2.0);
        board.setGame(game);
        borderPane.setCenter(board);

        // Generate sidebar
        VBox sideBar = new VBox();
        sideBar.setAlignment(Pos.CENTER);
        sideBar.setSpacing(6.0);
        sideBar.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        borderPane.setRight(sideBar);

        //Generate top bar
        GridPane topBar = new GridPane();
        topBar.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
        borderPane.setTop(topBar);

        // Generate score box
        VBox scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        Text scoreLabel = new Text("Score");
        scoreLabel.getStyleClass().add("heading");
        scoreBox.getChildren().add(scoreLabel);
        Text scoreField = new Text("0");
        scoreField.getStyleClass().add("score");
        scoreField.textProperty().bind(this.score.asString());
        scoreBox.getChildren().add(scoreField);
        topBar.add(scoreBox, 0, 0);

        // Generate multiplier box
        VBox multiplierBox = new VBox();
        multiplierBox.setPrefWidth(140);
        multiplierBox.setAlignment(Pos.CENTER);
        multiplierField = new Text("X1");
        multiplierField.getStyleClass().add("score");
        multiplierField.getStyleClass().add(multString.get());
        multiplierField.textProperty().bind(multString);
        multiplierBox.getChildren().add(multiplierField);
        topBar.add(multiplierBox, 0, 1);

        // Generate title
        Text title = new Text("Challenge Mode");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.getStyleClass().add("title");
        title.setTextAlignment(TextAlignment.CENTER);
        topBar.add(title, 1, 0);
        GridPane.setFillWidth(title, true);
        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHalignment(title, HPos.CENTER);

        // Generate lives box
        VBox liveBox = new VBox();
        liveBox.setAlignment(Pos.CENTER);
        Text livesLabel = new Text("Lives");
        livesLabel.getStyleClass().add("heading");
        liveBox.getChildren().add(livesLabel);
        Text livesField = new Text("0");
        livesField.getStyleClass().add("lives");
        livesField.textProperty().bind(game.getLivesProperty().asString());
        liveBox.getChildren().add(livesField);
        topBar.add(liveBox, 2, 0);

        // Generate highscore UI
        Text hiscoreLabel = new Text("High Score");
        hiscoreLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(hiscoreLabel);
        Text hiscoreField = new Text("0");
        hiscoreField.getStyleClass().add("hiscore");
        sideBar.getChildren().add(hiscoreField);
        hiscoreField.textProperty().bind(hiscore.asString());

        // Generate level UI
        Text levelLabel = new Text("Level");
        levelLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(levelLabel);
        Text levelField = new Text("0");
        levelField.getStyleClass().add("level");
        sideBar.getChildren().add(levelField);
        levelField.textProperty().bind(game.getLevelProperty().asString());

        // Generate current piece board
        Text nextPieceLabel = new Text("Current Piece");
        nextPieceLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(nextPieceLabel);
        currentPiece = new PieceBoard(3, 3, gameWindow.getWidth() / 6.0, gameWindow.getWidth() / 6.0);
        currentPiece.setCentre();
        currentPiece.setOnBlockClick(rotations -> rotatePiece());
        sideBar.getChildren().add(currentPiece);

        // Generate following piece board
        followingPiece = new PieceBoard(3, 3, gameWindow.getWidth() / 10.0, gameWindow.getWidth() / 10.0);
        followingPiece.setPadding(new Insets(20.0, 0.0, 0.0, 0.0));
        followingPiece.setOnBlockClick(block -> swapPiece());
        sideBar.getChildren().add(followingPiece);

        // Generate timer bar
        timerStack = new StackPane();
        borderPane.setBottom(timerStack);
        timer = new Timer();
        BorderPane.setMargin(timerStack, new Insets(5.0, 5.0, 5.0, 5.0));
        timerStack.getChildren().add(timer);
        StackPane.setAlignment(timer, Pos.CENTER_LEFT);

        // Handle block click event
        board.setOnRightClick(this::rotatePiece);
        board.setOnBlockClick(this::blockClicked);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        // Start new game
        game = new SingleplayerGame(5, 5);
    }

    /**
     * End game.
     */
    public void endGame() {
        logger.info("Ending game");
        game.stop();
        Multimedia.stopAll();
    }

    /**
     * Set the current piece to the next piece.
     * Set the following piece to a new piece.
     *
     * @param piece to set
     */
    protected void nextPiece(GamePiece piece) {
        logger.info("Next piece to place: " + piece);

        currentPiece.setPiece(piece);
        followingPiece.setPiece(game.getFollowingPiece());
        board.resetHovered();
    }

    /**
     * Visual component to change timer bar
     *
     * @param nextLoop length of the game loop
     */
    protected void gameLoop(int nextLoop) {

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN)),
                new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), timerStack.getWidth())),
                new KeyFrame(new Duration((double) nextLoop * 0.5), new KeyValue(timer.fillProperty(), Color.YELLOW)),
                new KeyFrame(new Duration((double) nextLoop * 0.75), new KeyValue(timer.fillProperty(), Color.RED)),
                new KeyFrame(new Duration(nextLoop), new KeyValue(timer.widthProperty(), 0)));

        timeline.play();
    }

    /**
     * Clear gameBlockCoordinates from game board for completed lines.
     *
     * @param gameBlockCoordinates of the blocks to clear
     */
    protected void lineCleared(Collection<GameBlockCoordinate> gameBlockCoordinates) {
        board.fadeBlocks(gameBlockCoordinates);
        Multimedia.playAudio("clear.wav");
    }

    /**
     * Swap current piece and following piece.
     */
    protected void swapPiece() {
        logger.info("Swapping piece");

        Multimedia.playAudio("rotate.wav");
        game.swapCurrentPiece();
        currentPiece.setPiece(game.getCurrentPiece());
        followingPiece.setPiece(game.getFollowingPiece());
    }

    /**
     * Rotate current piece.
     */
    protected void rotatePiece() {
        logger.info("Rotating piece");

        Multimedia.playAudio("rotate.wav");
        game.rotatePiece();
        currentPiece.setPiece(game.getCurrentPiece());
        board.refreshHovered(board.getHoveredBlock());
    }

    /**
     * Handle a key press event.
     *
     * @param keyEvent event object
     */
    protected void handleKey(KeyEvent keyEvent) {

        // Keyboard hovered coordinate
        keyboardX = board.getHoveredBlock().getX();
        keyboardY = board.getHoveredBlock().getY();

        // If chat open, return
        if (chatOpen) {
            return;
        }

        // Exit game
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            endGame();
            gameWindow.startMenu();
        }

        // Place game piece
        if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.X)) {
            blockClicked(board.getBlock(keyboardX, keyboardY));
        }

        // Move piece left
        if (keyEvent.getCode().equals(KeyCode.A) || keyEvent.getCode().equals(KeyCode.LEFT)) {
            if (keyboardX > 0) {
                keyboardX--;
            }
        }

        // Move piece right
        if (keyEvent.getCode().equals(KeyCode.D) || keyEvent.getCode().equals(KeyCode.RIGHT)) {
            if (keyboardX < game.getCols() - 1) {
                keyboardX++;
            }
        }

        // Move piece up
        if (keyEvent.getCode().equals(KeyCode.W) || keyEvent.getCode().equals(KeyCode.UP)) {
            if (keyboardY > 0) {
                keyboardY--;
            }
        }

        // Move piece down
        if (keyEvent.getCode().equals(KeyCode.S) || keyEvent.getCode().equals(KeyCode.DOWN)) {
            if (keyboardY < game.getRows() - 1) {
                keyboardY++;
            }
        }

        // Rotate left
        if (keyEvent.getCode().equals(KeyCode.Q) || keyEvent.getCode().equals(KeyCode.Z) || keyEvent.getCode().equals(KeyCode.OPEN_BRACKET)) {
            rotatePiece();
            rotatePiece();
            rotatePiece();
        }

        // Rotate right
        if (keyEvent.getCode().equals(KeyCode.E) || keyEvent.getCode().equals(KeyCode.C) || keyEvent.getCode().equals(KeyCode.CLOSE_BRACKET)) {
            rotatePiece();
        }

        // Swap piece
        if (keyEvent.getCode().equals(KeyCode.SPACE) || keyEvent.getCode().equals(KeyCode.R)) {
            swapPiece();
        }

        // Reset game loop
        if (keyEvent.getCode().equals(KeyCode.V)) {
            game.gameLoop();
        }

        // Refresh hovered block
        board.refreshHovered(board.getBlock(keyboardX, keyboardY));
    }

    /**
     * Handle block click event.
     *
     * @param gameBlock clicked on
     */
    protected void blockClicked(GameBlock gameBlock) {
        if (game.blockClicked(gameBlock)) {
            logger.info("Placed {}", gameBlock);

            Multimedia.playAudio("place.wav");
            game.restartGameLoop();
        } else {
            logger.info("Unable to place {}", gameBlock);

            Multimedia.playAudio("fail.wav");
        }
    }

    /**
     * Update score in game UI.
     *
     * @param oldValue old score value
     * @param newValue new score value
     */
    protected void setScore(Number oldValue, Number newValue) {
        logger.info("Score is now {}", newValue);

        if (newValue.intValue() > oldValue.intValue()) {
            hiscore.set(newValue.intValue());
        }

        // Visual effect for score change
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(score, oldValue)),
                new KeyFrame(new Duration(500.0), new KeyValue(score, newValue)));

        timeline.play();
    }

    /**
     * Change multiplier in the game UI.
     *
     * @param observable object
     * @param oldVal     old multiplier value
     * @param newVal     new multiplier value
     */
    public void setMultiplier(ObservableValue<? extends String> observable, String oldVal, String newVal) {
        if (!newVal.equals(multString.get())) {
            multString.set(newVal);
            multiplierField.getStyleClass().remove(oldVal);
            multiplierField.getStyleClass().add(newVal);
        }
    }
}


