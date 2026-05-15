import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SendingThread implements Runnable{
    Socket socket;
    PrintWriter out;
    BufferedReader userInput;
    ChatServer server;
    public SendingThread(Socket socket,ChatServer server) throws IOException {
        this.socket = socket;
        this.server = server;

        out = new PrintWriter(
                socket.getOutputStream(), true);

        userInput = new BufferedReader(
                new InputStreamReader(System.in)
        );
    }

    @Override
    public void run() {
        System.out.println("Server : " );
        try {
            String msg =  userInput.readLine();
            server.broadcast1(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
