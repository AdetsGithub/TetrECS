package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.LobbyScene;
import uk.ac.soton.comp1206.util.Multimedia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ChannelList extends Vbox.
 * Displays list of channels on UI
 */
public class ChannelList extends VBox {

    private static final Logger logger = LogManager.getLogger(ChannelList.class);
    private final HashMap<String, Text> channelList = new HashMap<>();
    private final StringProperty channel = new SimpleStringProperty();

    /**
     * Build channel list UI.
     */
    public ChannelList() {
        logger.info("Building " + getClass().getName());

        // Set up general UI properties
        setSpacing(10.0);
        setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        setPrefWidth(300.0);

        // Generate text field to create new channels
        getStyleClass().add("channelList");
        Text newChannel = new Text("Host New Game");
        newChannel.getStyleClass().add("channelItem");
        getChildren().add(newChannel);

        // Bind to channel property to style created channel
        channel.addListener((observable, oldValue, channelName) -> {
            for (Text all : channelList.values()) {
                all.getStyleClass().remove("selected");
            }
        });
        TextField newChannelField = new TextField();
        newChannelField.setVisible(false);
        getChildren().add(newChannelField);
        newChannel.setOnMouseClicked(e -> {
            Multimedia.playAudio("rotate.wav");
            newChannelField.setVisible(true);
            newChannelField.requestFocus();
        });

        // Add new channel to list and send message to server
        newChannelField.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                Multimedia.playAudio("rotate.wav");
                LobbyScene.send("CREATE " + newChannelField.getText().trim());
                LobbyScene.send("LIST");
                newChannelField.setVisible(false);
                newChannelField.clear();
            }
        });
    }

    /**
     * Gets created channel property.
     *
     * @return property of channel
     */
    public StringProperty getChannelProperty() {
        return channel;
    }

    /**
     * Add channel to list of channels.
     *
     * @param name of channel
     */
    public void addChannel(String name) {
        if (channelList.containsKey(name)) {
            return;
        }
        logger.info("Adding {}", name);
        Text channelText = new Text(name);
        channelText.getStyleClass().add("channelItem");
        getChildren().add(channelText);
        channelList.put(name, channelText);
        channelText.setOnMouseClicked(e -> LobbyScene.requestJoin(name));
    }

    /**
     * Compares channel list with new channel list and updates channel list.
     *
     * @param channels new channel list
     */
    public void setChannels(List<String> channels) {
        Set<String> existing = channelList.keySet();
        if (existing.size() == channels.size() && existing.containsAll(channels)) {
            return;
        }
        HashSet<String> removeChannels = new HashSet<>();
        for (String existingChannel : existing) {
            if (channels.contains(existingChannel)) continue;
            removeChannels.add(existingChannel);
        }
        for (String removing : removeChannels) {
            getChildren().remove(channelList.get(removing));
        }
        channelList.keySet().removeAll(removeChannels);
        for (String channelName : channels) {
            addChannel(channelName);
        }
    }
}