package uk.ac.soton.comp1206.component;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp1206.scene.LobbyScene;

/**
 * Chat extends VBox.
 * Visual element for chat functionality in multiplayer.
 */
public class Chat extends VBox {

    /**
     * Build chat UI.
     */
    public Chat() {

        setSpacing(10.0);
        setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        getStyleClass().add("gameBox");

        // Generate player list in the channel
        LobbyScene.playerList = new PlayerList();
        getChildren().add(LobbyScene.playerList);

        //Generate the scroller to hold chat messages
        LobbyScene.scroller = new ScrollPane();
        LobbyScene.scroller.setPrefHeight(LobbyScene.gameWindow.getHeight() / 2.0);
        LobbyScene.scroller.getStyleClass().add("scroller");
        LobbyScene.scroller.setFitToWidth(true);

        //Generate Vbox for chat messages inside the scroller
        LobbyScene.messages = new VBox();
        LobbyScene.messages.getStyleClass().add("messages");
        getChildren().add(LobbyScene.scroller);
        LobbyScene.scroller.setContent(LobbyScene.messages);


        // Generate anchor pane with leave and start buttons on the left and right
        AnchorPane buttons = new AnchorPane();
        getChildren().add(buttons);
        Button partButton = new Button("Leave game");
        partButton.setOnAction(e -> {
            LobbyScene.send("PART");
            LobbyScene.send("LIST");
        });
        buttons.getChildren().add(partButton);
        AnchorPane.setRightAnchor(partButton, 0.0);
        Button startButton = new Button("Start game");
        startButton.visibleProperty().bind(LobbyScene.host);
        startButton.setOnAction(e -> LobbyScene.requestStart());
        buttons.getChildren().add(startButton);
        AnchorPane.setLeftAnchor(startButton, 0.0);
    }
}