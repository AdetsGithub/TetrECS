package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ChannelList;
import uk.ac.soton.comp1206.component.Chat;
import uk.ac.soton.comp1206.component.PlayerList;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.util.Multimedia;

import java.util.*;

/**
 * Lobby scene extends base scene.
 * Displays lobby UI.
 * Communicates with server to update UI.
 */
public class LobbyScene extends BaseScene {
    public static final BooleanProperty host = new SimpleBooleanProperty(false);
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private static final StringProperty channel = new SimpleStringProperty("");
    public static ScrollPane scroller = null;
    public static VBox messages = null;
    public static PlayerList playerList;
    private static Communicator communicator = null;
    private final StringProperty name = new SimpleStringProperty();
    private ChannelList channelList;
    private Timer timer;

    /**
     * Create lobby scene.
     * Set communicator to game window communicator
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();

        logger.info("Creating Lobby");
    }

    /**
     * Sends request to server.
     *
     * @param message to be sent
     */
    public static void send(String message) {
        logger.info("Sending message: {}", message);

        communicator.send(message);
    }

    /**
     * Request to join a channel.
     *
     * @param name of channel
     */
    public static void requestJoin(String name) {
        if (channel.get().equals(name)) {
            return;
        }

        host.set(false);
        send("JOIN " + name);
    }

    /**
     * Send server request to start game.
     */
    public static void requestStart() {
        send("START");
    }

    /**
     * Initialise lobby scene.
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                leave();
                stopRequests();
                gameWindow.startMenu();
            }
        });

        // Request all channels
        send("LIST");
        communicator.addListener(message -> Platform.runLater(() -> receiver(message.trim())));
        TimerTask refreshChannels = new TimerTask() {
            @Override
            public void run() {
                logger.info("Refreshing channel list");
                LobbyScene.send("LIST");
            }
        };
        timer = new Timer();
        timer.schedule(refreshChannels, 0, 4000);
    }

    /**
     * Build UI elements of lobby scene.
     */
    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        //Setup stack pane
        StackPane lobbyPane = generateMainPane("menu-background");


        BorderPane mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);

        // Generate title
        Text multiplayerText = new Text("Multiplayer");
        BorderPane.setAlignment(multiplayerText, Pos.CENTER);
        multiplayerText.setTextAlignment(TextAlignment.CENTER);
        multiplayerText.getStyleClass().add("title");
        mainPane.setTop(multiplayerText);

        // Generate list title
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10.0);
        gridPane.setVgap(10.0);
        gridPane.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        Text channelText = new Text("Current Games");
        channelText.setTextAlignment(TextAlignment.CENTER);
        channelText.getStyleClass().add("heading");
        gridPane.add(channelText, 0, 0);
        mainPane.setCenter(gridPane);

        // Generate channel list
        channelList = new ChannelList();
        channelList.getChannelProperty().bind(channel);
        gridPane.add(channelList, 0, 1);

        // Generate channel title
        Text lobbyText = new Text();
        lobbyText.textProperty().bind(channel);
        lobbyText.setTextAlignment(TextAlignment.CENTER);
        lobbyText.getStyleClass().add("heading");
        gridPane.add(lobbyText, 1, 0);

        // Generate lobby chat
        Chat chat = new Chat();
        gridPane.add(chat, 1, 1);
        chat.visibleProperty().bind(channel.isNotEmpty());
        GridPane.setHgrow(chat, Priority.ALWAYS);
    }

    /**
     * Stop sending server requests.
     */
    private void stopRequests() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Leave channel.
     */
    private void leave() {
        if (!channel.isEmpty().get()) {
            send("PART");
        }
    }

    /**
     * Receive server message.
     * Parse message and handle.
     *
     * @param message from server
     */
    private void receiver(String message) {
        logger.info("Message received: {}", message);

        String[] components = message.split(" ", 2);
        String command = components[0];

        // Receive list of channels
        if (command.equals("CHANNELS") && components.length > 1) {
            receiveChannelList(components[1]);
        } else {
            channelList.setChannels(new ArrayList<>());
        }

        // Receive channel joined successfully
        if (command.equals("JOIN")) {
            host.set(false);
            String channel = components[1];
            join(channel);
        }

        // Receive nickname set by player and if valid, set name
        if (command.equals("NICK") && components.length > 1 && !components[1].contains(":")) {
            setName(components[1]);

        }

        // Receive channel left successfully
        if (command.equals("PARTED")) {
            channel.set("");
        }

        // Receive users successfully
        if (command.equals("USERS") && components.length > 1) {
            setUsers(components[1]);
        }

        // Received host confirmation
        if (command.equals("HOST")) {
            host.set(true);
        }
    }

    /**
     * Sets all users in channel
     *
     * @param data players as string
     */
    private void setUsers(String data) {
        logger.info("Received user list: {}", data);

        String[] players = data.split("\\R");
        List<String> list = Arrays.asList(players);
        playerList.setPlayers(list);
        Multimedia.playAudio("message.wav");
    }

    /**
     * Set player name
     *
     * @param name of player
     */
    private void setName(String name) {
        logger.info("Local name set as: {}", name);
        this.name.set(name);
    }

    /**
     * Receive list of all live channels.
     *
     * @param data on channels available
     */
    private void receiveChannelList(String data) {
        logger.info("Channel list: {}", data);

        String[] channels = data.split("\\R");
        List<String> listChannels = Arrays.asList(channels);

        channelList.setChannels(listChannels);
    }

    /**
     * Join channel successfully.
     *
     * @param name of channel
     */
    public void join(String name) {
        channelList.addChannel(name);
        channel.set(name);
        messages.getChildren().clear();
        Text intro = new Text("Welcome to the lobby\nType /nick <New name> to change your name\n\n");
        messages.getChildren().add(intro);
    }
}

