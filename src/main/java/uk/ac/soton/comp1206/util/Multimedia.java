package uk.ac.soton.comp1206.util;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Multimedia manages image and audio elements of project.
 */
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);
    private static boolean audioEnabled = true;
    private static MediaPlayer mediaPlayer;
    private static MediaPlayer backgroundPlayer;

    /**
     * Create multimedia.
     * Dummy constructor, no usages.
     */
    public Multimedia() {
    }

    /**
     * Stop all music.
     */
    public static void stopAll() {
        try {
            mediaPlayer.stop();
            backgroundPlayer.stop();
        } catch (Exception ignore) {
        }
    }

    /**
     * Play music file.
     *
     * @param music the music file name
     * @param loop  whether music should loop or not
     */
    public static void playBackgroundMusic(String music, boolean loop) {
        if (audioEnabled) {
            if (backgroundPlayer != null) {
                backgroundPlayer.stop();
            }

            try {
                var musicFile = Objects.requireNonNull(Multimedia.class.getResource("/music/" + music)).toExternalForm();
                var play = new Media(musicFile);
                backgroundPlayer = new MediaPlayer(play);
                backgroundPlayer.setVolume(0.8);
                if (loop) {
                    backgroundPlayer.setCycleCount(-1);
                }

                backgroundPlayer.play();
            } catch (Exception e) {
                audioEnabled = false;
                e.printStackTrace();
                logger.error("Background music failed");
            }
        }
    }

    /**
     * Play audio.
     *
     * @param sound name of the sound
     */
    public static void playAudio(String sound) {
        if (audioEnabled) {
            var musicFile = Objects.requireNonNull(Multimedia.class.getResource("/sounds/" + sound)).toExternalForm();
            try {
                Media play = new Media(musicFile);
                mediaPlayer = new MediaPlayer(play);
                mediaPlayer.play();
            } catch (Exception e) {
                audioEnabled = false;
                e.printStackTrace();
                logger.error("Audio failed");
            }
        }
    }

    /**
     * Load image.
     *
     * @param image the name of the image file
     * @return the image object
     */
    public static Image getImage(String image) {
        try {
            return new Image(Objects.requireNonNull(Multimedia.class.getResource("/images/" + image)).toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Image could not be loaded: {}", image);
            return null;
        }
    }
}
