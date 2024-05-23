package com.example.chatfx;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

public class MusicPlayer {
    private MediaPlayer backgroundPlayer;

    public void playMusic(String musicFile) {
        new Thread(() -> {
            Media sound = new Media(Objects.requireNonNull(getClass().getResource(musicFile)).toString());
            backgroundPlayer = new MediaPlayer(sound);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.play();
        }).start();
    }
}
