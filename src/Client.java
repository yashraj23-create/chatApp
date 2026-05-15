import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Client {

    static volatile boolean running = true;

    public static synchronized void safePrint(String msg) {
        System.out.print("\r");
        System.out.println(msg);
        System.out.flush();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("client started .....");

        Socket socket = new Socket("localhost", 38297);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        BufferedReader userInput = new BufferedReader(
                new InputStreamReader(System.in)
        );

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Enter your name before conversation : ");
        String name = userInput.readLine();
        // Sender thread
        new Thread(() -> {
            try {
                String input;
                while (running) {
                    System.out.print(name + " : ");
                    input = userInput.readLine();

                    if (input == null || input.equalsIgnoreCase("no")) {
                        running = false;
                        socket.close();
                        break;
                    }

                    out.println(name  + " : " + input);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Receiver thread
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    safePrint(msg);
                }
            } catch (Exception e) {
                System.out.println("Disconnected from server");
            } finally {
                running = false;
                try { socket.close(); } catch (Exception ignored) {}
            }
        }).start();
    }
}
