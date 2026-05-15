import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    Socket socket;
    ChatServer server;
    BufferedReader in;
    PrintWriter out;
    public ClientHandler(Socket socket,ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void SendChat(String msg){
        out.println(msg);
    }

    public void sendMessage(String msg){
        out.println(msg);
    }

    @Override
    public void run() {
        try {

            String msg;

            while ((msg = in.readLine()) != null) {

                System.out.println(msg);
                server.broadcast(msg, this);
            }

        } catch (Exception e) {

            System.out.println("Client disconnected");
        } finally {

            ChatServer.clients.remove(this);

            try {
                socket.close();
            } catch (Exception ignored) {
            }

        }
    }
}