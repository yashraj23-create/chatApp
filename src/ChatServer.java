import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer{

    static List<ClientHandler> clients =
            new ArrayList<>();

    public void broadcast(String msg, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(msg);
                }
            }
        }
    }

    public void broadcast1(String msg) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                    client.SendChat(msg);
                }
            }
        }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(38297);
        System.out.println("Server started...");
        ChatServer server = new ChatServer();
        while (true) {

            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            ClientHandler clientHandler = new ClientHandler(socket,server);
            clients.add(clientHandler);

            SendingThread sendingThread = new SendingThread(socket,server);



            new Thread(clientHandler).start();

            new Thread(sendingThread).start();

        }
    }

}