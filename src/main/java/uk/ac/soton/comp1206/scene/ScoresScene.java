package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoreBox;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;
import uk.ac.soton.comp1206.util.Storage;

import java.util.ArrayList;
import java.util.Timer;

/**
 * Scores scene extends base scene.
 * Build visual elements of scene.
 * Communicates with server to update remote scores.
 * Updates remote and local scores.
 */
public class ScoresScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final ArrayList<Pair<String, Integer>> remoteScores = new ArrayList<>();
    private final StringProperty myName = new SimpleStringProperty("");
    private final Communicator communicator;
    public ScoreBox hiScoreCol1;
    public ScoreBox hiScoreCol2;
    protected BooleanProperty showScores = new SimpleBooleanProperty(false);
    private Timer timer;
    private boolean newScore = false;
    private boolean newRemoteScore = false;
    private Pair<String, Integer> myScore;
    private ObservableList<Pair<String, Integer>> scoreList;
    private ObservableList<Pair<String, Integer>> remoteScoreList;
    private boolean waitingForScores = true;
    private VBox scoreBox;
    private Text hiscoreText;

    /**
     * Create score scene.
     * Initialise communicator.
     *
     * @param gameWindow the game window
     * @param game       the game
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
        this.game = game;
        logger.info("Creating Score Scene");
    }

    /**
     * Create score scene for when game is null.
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
        game = new Game() {
            @Override
            public GamePiece spawnPiece() {
                return null;
            }
        };
    }

    /**
     * Cancels all timers.
     * Returns to menu.
     */

    public void returnToMenu() {
        if (newScore) {
            return;
        }
        if (timer != null) {
            timer.cancel();
        }
        gameWindow.startMenu();
    }

    /**
     * Initialise score scene.
     * Request server for scores.
     */
    @Override
    public void initialise() {
        Multimedia.playAudio("explode.wav");
        Multimedia.playBackgroundMusic("end.wav", true);
        communicator.addListener(message -> Platform.runLater(() -> receiver(message.trim())));
        if (!game.getScores().isEmpty()) {
            myName.set(game.getNameProperty().getValue());
        }
        communicator.send("HISCORES");
    }

    /**
     * Check if there are any high scores to add.
     */
    public void checkForHiScore() {
        logger.info("Checking for high score");

        if (!game.getScores().isEmpty()) {
            reveal();
            return;
        }

        int currentScore = game.getScore();
        int counter = 0;
        int remoteCounter = 0;
        int lowestScore = 0;
        int lowestScoreRemote = 0;

        if (!scoreList.isEmpty()) {
            lowestScore = (scoreList.get(scoreList.size() - 1)).getValue();
        }
        if (scoreList.size() < 10) {
            newScore = true;
        }
        if (!remoteScores.isEmpty()) {
            lowestScoreRemote = remoteScores.get(remoteScores.size() - 1).getValue();
        }
        if (remoteScores.size() < 10) {
            newRemoteScore = true;
        }
        if (currentScore > lowestScore) {
            for (Pair pair : scoreList) {
                if ((Integer) pair.getValue() < currentScore) {
                    newScore = true;
                    break;
                }
                counter++;
            }
        }

        if (currentScore > lowestScoreRemote) {
            for (Pair pair : remoteScores) {
                if ((Integer) pair.getValue() < currentScore) {
                    newRemoteScore = true;
                    break;
                }
                remoteCounter++;
            }
        }

        if (newScore || newRemoteScore) {
            hiscoreText.setText("You got a High Score!");
            TextField name = new TextField();
            name.setPromptText("Enter your name");
            name.setPrefWidth(gameWindow.getWidth() / 2.0);
            name.requestFocus();
            scoreBox.getChildren().add(2, name);
            Button button = new Button("Submit");
            button.setDefaultButton(true);
            scoreBox.getChildren().add(3, button);
            int addResult = counter;
            int addRemoteResult = remoteCounter;
            button.setOnAction(e -> {
                String myName = name.getText().replace(":", "");
                this.myName.set(myName);
                scoreBox.getChildren().remove(2);
                scoreBox.getChildren().remove(2);
                myScore = new Pair<>(myName, currentScore);
                if (newScore) {
                    scoreList.add(addResult, myScore);
                }
                if (newRemoteScore) {
                    remoteScoreList.add(addRemoteResult, myScore);
                }
                communicator.send("HISCORE " + myName + ":" + currentScore);
                Storage.writeScores(scoreList);
                communicator.send("HISCORES");
                newScore = false;
                newRemoteScore = false;
                Multimedia.playAudio("pling.wav");
            });
        } else {
            reveal();
        }
    }

    /**
     * Show score lists.
     */
    public void reveal() {
        scene.setOnKeyPressed(e -> returnToMenu());
        showScores.set(true);
        hiScoreCol1.reveal();
        hiScoreCol2.reveal();
    }

    /**
     * Receives message from server.
     *
     * @param message from server
     */
    private void receiver(String message) {
        logger.info("Received message: {}", message);
        String[] components = message.split(" ", 2);
        String command = components[0];
        if (command.equals("HISCORES")) {
            if (components.length > 1) {
                String data = components[1];
                receiveScores(data);
            } else {
                receiveScores("");
            }
        }
    }

    /**
     * Parse scores data and add to UI element.
     *
     * @param data from server
     */
    private void receiveScores(String data) {
        logger.info("Scores: {}", data);

        remoteScores.clear();
        for (String scoreLine : data.split("\\R")) {

            String[] components = scoreLine.split(":", 2);
            String player = components[0];

            if (components.length < 2) continue;

            int score = Integer.parseInt(components[1]);

            logger.info("Received score: {} = {}", player, score);

            remoteScores.add(new Pair<>(player, score));
        }
        remoteScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        remoteScoreList.clear();
        remoteScoreList.addAll(remoteScores);
        if (waitingForScores) {
            checkForHiScore();
            waitingForScores = false;
            return;
        }
        reveal();
    }

    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        // Setup stack pane / generate main pane for the scene
        StackPane scorePane = generateMainPane("menu-background");

        // Generate pane oto hold score boxes with scores
        BorderPane mainPane = new BorderPane();
        scorePane.getChildren().add(mainPane);
        scoreBox = new VBox();
        scoreBox.setAlignment(Pos.TOP_CENTER);
        scoreBox.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        scoreBox.setSpacing(20.0);
        mainPane.setCenter(scoreBox);

        // Generate image at the top
        ImageView image = new ImageView(Multimedia.getImage("TetrECS.png"));
        image.setFitWidth(gameWindow.getWidth() * 0.666666666666667);
        image.setPreserveRatio(true);
        scoreBox.getChildren().add(image);

        // Generate title text of scene
        Text gameOverText = new Text("Leaderboard");
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(gameOverText, Priority.ALWAYS);
        gameOverText.getStyleClass().add("bigtitle");
        scoreBox.getChildren().add(gameOverText);

        // Generate high score text
        hiscoreText = new Text("High Scores");
        hiscoreText.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(hiscoreText, Priority.ALWAYS);
        hiscoreText.getStyleClass().add("title");
        hiscoreText.setFill(Color.YELLOW);
        scoreBox.getChildren().add(hiscoreText);

        // Generate layout of score columns
        GridPane scoreGrid = new GridPane();
        scoreGrid.visibleProperty().bind(this.showScores);
        scoreGrid.setAlignment(Pos.CENTER);
        scoreGrid.setHgap(100.0);
        scoreBox.getChildren().add(scoreGrid);

        // Generate text for local scores
        Text localScoresLabel = new Text("Local Scores");
        localScoresLabel.setTextAlignment(TextAlignment.CENTER);
        localScoresLabel.getStyleClass().add("heading");
        GridPane.setHalignment(localScoresLabel, HPos.CENTER);
        if (game.getScores().isEmpty()) {
            scoreList = FXCollections.observableArrayList(Storage.loadScores());
        } else {
            scoreList = FXCollections.observableArrayList(game.getScores());
            localScoresLabel.setText("Your score");
        }
        scoreGrid.add(localScoresLabel, 0, 0);

        // Generate text for remote scores
        Text remoteScoresLabel = new Text("Online Scores");
        remoteScoresLabel.setTextAlignment(TextAlignment.CENTER);
        remoteScoresLabel.getStyleClass().add("heading");
        GridPane.setHalignment(remoteScoresLabel, HPos.CENTER);
        scoreGrid.add(remoteScoresLabel, 1, 0);

        // Generate and bind local and remote scores
        hiScoreCol1 = new ScoreBox();
        Button button = new Button("Button");
        hiScoreCol1.getChildren().add(button);
        GridPane.setHalignment(hiScoreCol1, HPos.CENTER);
        scoreGrid.add(hiScoreCol1, 0, 1);

        hiScoreCol2 = new ScoreBox();
        Button button2 = new Button("Button");
        hiScoreCol2.getChildren().add(button2);
        GridPane.setHalignment(hiScoreCol2, HPos.CENTER);
        scoreGrid.add(hiScoreCol2, 1, 1);

        if (game.getScores().isEmpty()) {
            scoreList = FXCollections.observableArrayList(Storage.loadScores());
        } else {
            scoreList = FXCollections.observableArrayList(game.getScores());
            localScoresLabel.setText("This game");
        }

        scoreList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        remoteScoreList = FXCollections.observableArrayList(remoteScores);
        SimpleListProperty<Pair<String, Integer>> wrapper = new SimpleListProperty<>(scoreList);
        hiScoreCol1.getScoreProperty().bind(wrapper);
        hiScoreCol1.getNameProperty().bind(myName);
        SimpleListProperty<Pair<String, Integer>> wrapper2 = new SimpleListProperty<>(remoteScoreList);
        hiScoreCol2.getScoreProperty().bind(wrapper2);
        hiScoreCol2.getNameProperty().bind(myName);
    }
}

