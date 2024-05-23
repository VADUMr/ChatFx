package com.example.chatfx;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    static Database db;

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
            client.sendMessage(nickname + ": " + message);
        }
    }

    static void comandMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    static void ExitMessage(String nickname, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage("Користувач " + nickname + " віключився!");
            }
        }
    }
    static void showHelpMessage(ClientHandler sender) {
        sender.sendMessage("Доступні команди:");
        sender.sendMessage("/help - показати цю інструкцію");
        sender.sendMessage("/show - показати останні 50 повідомлень");
        sender.sendMessage("/search <ключове_слово> - знайти повідомлення за ключовим словом");
        sender.sendMessage("/del <id> - видалити повідомлення за його ідентифікатором");
        sender.sendMessage("/online - виводить користувачів що онлайн");
            // Додайте інші команди, якщо вони є
    }
    static void showOnlineMessage(ClientHandler sender) {
        sender.sendMessage("Список користувачів онлайн:");
        for (ClientHandler client : clients) {
            sender.sendMessage(client.nickname);
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

                nickname = reader.readLine();
                System.out.println("Клієнт " + socket.getInetAddress().getHostAddress() + " має нік: " + nickname);

                new Thread(() -> {
                    // Отримати останні 50 повідомлень з бази даних
                    List<Message> last50Messages = Database.getLast50Messages();

                    // Відправити останні 50 повідомлень клієнту
                    for (Message message : last50Messages) {
                        writer.println(message.getNickname() + ": " + message.getText());
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    if (message.equals("/help")) {
                        showHelpMessage(this);
                    }else if (message.equals("/online")) {
                        showOnlineMessage(this);
                    }else if (message.equals("/show")) {//через іфки можна і далі робити логіку
                        writer.println("/Del");
                        System.out.println(nickname + "Використав /show");
                        new Thread(() -> {
                            List<Message> last50Messages = Database.getLast50Messages();
                            for (Message messages : last50Messages) {
                                writer.println(messages.getId() + ": " + messages.getNickname() + ": " + messages.getText());
                            }
                        }).start();
                    } else if (message.startsWith("/del")) {
                        int messageId = Integer.parseInt(message.substring(5).trim());
                        System.out.println(nickname + "Видаливив повідомлення під id = " + messageId);
                        new Thread(() -> {
                            boolean deleted = Database.deleteMessageById(messageId);
                            if (deleted) {
                                comandMessage("/Del");
                                    List<Message> last50Messages = Database.getLast50Messages();
                                    for (Message messages : last50Messages) {
                                        broadcastMessage(messages.getText(),messages.getNickname(),this);
                                    }
                                writer.println("Повідомлення успішно видалено.");
                            } else {
                                writer.println("Не вдалося видалити повідомлення. Перевірте коректність ідентифікатора.");
                            }
                        }).start();
                    }else if (message.startsWith("/search")) {
                        String keyword = message.substring(8).trim();
                        new Thread(() -> {
                            List<Message> searchResults = Database.searchMessages(keyword);
                            if (!searchResults.isEmpty()) {
                                writer.println("Результати пошуку для '" + keyword + "':");
                                for (Message m : searchResults) {
                                    writer.println(m.getId() + ": " + m.getNickname() + ": " + m.getText());
                                }
                            } else {
                                writer.println("Повідомлень, що містять '" + keyword + "', не знайдено.");
                            }
                        }).start();
                    } else {
                        System.out.println(nickname + ": " + message);
                        Database.logMessage(nickname, message, socket.getInetAddress().getHostAddress());
                        broadcastMessage(message, nickname, this);
                    }
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
                //Database.logMessage(nickname, "Клієнт відключився: ", socket.getInetAddress().getHostAddress());
                ExitMessage(nickname, this);
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }
    }
}