package uk.ac.soton.comp1206.component;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Visual component which displays players and their scores
 */
public class ScoreBox extends VBox {
    private static final Logger logger = LogManager.getLogger(ScoreBox.class);

    /**
     * A list containing tuples of names and scores.
     */
    public final SimpleListProperty<Pair<String, Integer>> nameAndScores = new SimpleListProperty<>();
    /**
     * ArrayList for scores and dead players.
     */
    private final ArrayList<HBox> scoreBoxes = new ArrayList<>();
    /**
     * Name of local player.
     */
    private final StringProperty name = new SimpleStringProperty();
    /**
     * Names of dead players.
     */
    private final ArrayList<String> deadPlayers = new ArrayList();
    /**
     * Number of scores to be shown.
     */
    private int numberOfScores = 10;
    /**
     * Auto reveal for the score boxes.
     */
    private boolean reveal = false;

    /**
     * Create score box.
     */
    public ScoreBox() {
        getStyleClass().add("scorelist");
        setAlignment(Pos.CENTER);
        setSpacing(2.0);
        nameAndScores.addListener((InvalidationListener) c -> this.updateList());
        name.addListener(e -> this.updateList());
    }

    /**
     * Reveal scores sequentially.
     */
    public void reveal() {
        logger.info("Revealing {} scores", scoreBoxes.size());

        ArrayList<Transition> transitions = new ArrayList<>();
        for (HBox scoreBox : scoreBoxes) {
            // Creating a fade transition for each score box
            FadeTransition fade = new FadeTransition(new Duration(200), scoreBox);
            fade.setFromValue(0);
            fade.setToValue(1);
            transitions.add(fade);
        }
        SequentialTransition transition = new SequentialTransition(transitions.toArray(Animation[]::new));
        transition.play();
    }

    /**
     * Update the score list.
     */
    public void updateList() {
        logger.info("Updating score list. Number of scores: {}", nameAndScores.size());

        scoreBoxes.clear();
        getChildren().clear();
        int counter = 0;
        for (Pair pair : nameAndScores) {
            // Break when counter exceeds number of scorers to show
            if (counter++ > numberOfScores) break;

            // Create score box for player
            HBox scoreBox = new HBox();
            scoreBox.setOpacity(0.0);
            scoreBox.getStyleClass().add("scoreitem");
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(10.0);

            // Making the player name text
            var colour = GameBlock.COLOURS[counter];
            Text player = new Text(pair.getKey() + ":");
            player.setTextAlignment(TextAlignment.CENTER);
            player.setFill(colour);
            HBox.setHgrow(player, Priority.ALWAYS);

            //Modifying style depending on who scorer is
            String scorerName = pair.getKey().toString();
            player.getStyleClass().add("scorer");
            if (scorerName.equals(name.get())) {
                player.getStyleClass().add("myscore");
            }
            if (deadPlayers.contains(scorerName)) {
                player.getStyleClass().add("deadscore");
            }

            // Making the plaer score text
            Text points = new Text(pair.getValue().toString());
            points.getStyleClass().add("points");
            points.setTextAlignment(TextAlignment.CENTER);
            points.setFill(colour);
            HBox.setHgrow(points, Priority.ALWAYS);

            // Adding player name and score to score box
            scoreBox.getChildren().addAll(player, points);
            getChildren().add(scoreBox);
            scoreBoxes.add(scoreBox);
        }
        if (reveal) {
            reveal();
        }
    }

    /**
     * Add player to the dead players list.
     *
     * @param player name
     */
    public void kill(String player) {
        deadPlayers.add(player);
    }

    /**
     * Get the player name.
     *
     * @return name
     */
    public StringProperty getNameProperty() {
        return name;
    }

    /**
     * Get the player name and scores list.
     *
     * @return name and scores of players
     */
    public ListProperty<Pair<String, Integer>> getScoreProperty() {
        return nameAndScores;
    }

    /**
     * Set reveal status of score box.
     *
     * @param reveal or not reveal
     */
    public void setReveal(boolean reveal) {
        this.reveal = reveal;
    }

    /**
     * Sets the number of scores to show.
     *
     * @param number of scores to show
     */
    public void setNumberOfScores(int number) {
        numberOfScores = number;
    }
}

