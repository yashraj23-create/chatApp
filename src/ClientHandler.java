import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    String name;
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

    public void sendPrivate(String message){
        out.println(message);
    }

    public void sendMessage(String msg){
        out.println(msg);
    }

    @Override
    public void run() {
        try {

            while(true) {
                String cli = in.readLine();
                if(cli == null){
                    break;
                }
                boolean exist = server.duplicates(cli);

                if (exist) {
                    out.println("already there");
                } else {
                    out.println("Accepted");
                    this.name = cli;
                    System.out.println(STR."\{name} joined"); break;
                }
            }

            String msg;

            while((msg = in.readLine()) != null){

                String[] arr = msg.split("\\|", 4);

                String time = arr[0];
                String mode = arr[1];
                String sender = arr[2];
                String actualMsg = arr[3];

                String finalMsg =
                        STR."[\{time}] \{sender} : \{actualMsg}";

                if(mode.equalsIgnoreCase("public")){

                    server.broadcast(finalMsg, this);

                } else {

                    server.particular(mode, finalMsg);

                }
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