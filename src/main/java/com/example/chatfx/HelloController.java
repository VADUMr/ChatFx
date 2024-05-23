package com.example.chatfx;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HelloController{
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private MusicPlayer musicPlayer = new MusicPlayer();

    @FXML
    private TextField ServerIP;
    @FXML
    private TextField ChatNickname;
    @FXML
    private TextField Message;
    @FXML
    private TextArea messageArea;

    @FXML
    protected void connect() {
        try {
            String IP = ServerIP.getText();
            socket = new Socket(IP, 8888);
            Platform.runLater(() -> messageArea.appendText("Підключено до сервера." + "\n"));
            musicPlayer.playMusic("/music/background.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupStreams();
        startReceivingMessages();
    }
    private void setupStreams() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String nickname = ChatNickname.getText();
            writer.println(nickname);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startReceivingMessages() {
        Thread receiveThread = new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    if(message.equals("/Del")) {
                        Platform.runLater(() -> messageArea.clear());
                    }
                    else{
                        String finalMessage = message;
                        Platform.runLater(() -> messageArea.appendText(finalMessage + "\n"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();
    }
    @FXML
    protected void sendMessage() {
        String message = Message.getText().trim();
        if (!message.isEmpty()) {
            writer.println(message);
            Message.clear();
        }
    }
    @FXML
    protected void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                Platform.runLater(() -> messageArea.appendText("Відключено від сервера." + "\n"));
            }
            //Platform.exit(); // Закриття вікна програми
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}