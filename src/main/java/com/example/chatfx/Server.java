package com.example.chatfx;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8888);
            System.out.println("Сервер запущено. Очікування підключень...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Новий клієнт підключився: " + socket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void broadcastMessage(String message, String nickname, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(nickname + ": " + message);
            }
        }
    }

    static void ExitMessage(String nickname, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage("Користувач " + nickname + " віключився!");
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                //автоматичний flush після кожного введеня інформація в потік

                nickname = reader.readLine();
                System.out.println("Клієнт " + socket.getInetAddress().getHostAddress() + " має нік: " + nickname);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(nickname + ": " + message);
                    broadcastMessage(message, nickname, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                System.out.println("Клієнт відключився: " + socket.getInetAddress().getHostAddress());
                ExitMessage(nickname, this);
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }
    }
}
