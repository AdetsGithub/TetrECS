package uk.ac.soton.comp1206.component;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class PlayerList extends TextFlow {
    private final ArrayList<String> players = new ArrayList<>();

    /**
     * Create player list.
     */
    public PlayerList() {
        getStyleClass().add("playerBox");
    }

    /**
     * Add new players to player list.
     *
     * @param newPlayers string list of players
     */
    public void setPlayers(List<String> newPlayers) {
        players.clear();
        players.addAll(newPlayers);
        update();
    }

    /**
     * Updates player list.
     */
    public void update() {
        getChildren().clear();
        for (String player : players) {
            Text playerName = new Text(player + " ");
            getChildren().add(playerName);
        }
    }
}