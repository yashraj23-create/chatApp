import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm");

        System.out.println("client started .....");

        Socket socket = new Socket("localhost", 38297);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        BufferedReader userInput = new BufferedReader(
                new InputStreamReader(System.in)
        );

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String name1;

        while (true) {
            System.out.println("Enter your name : ");

            name1 = userInput.readLine();

            if (name1 == null) {
                System.out.println("Input stream closed");
                break;
            }

            out.println(name1);

            String ret = in.readLine();

            if (ret == null) {
                System.out.println("Server disconnected");
                socket.close();
                break;
            }

            if (ret.equalsIgnoreCase("Accepted")) {
                System.out.println("you joined group chat");
                break;
            } else {
                System.out.println(ret);
            }
        }
        String name = name1;
        // Sender thread
        new Thread(() -> {
            try {
                String input;
                while (running) {

                    System.out.println("Type name of user you want to send message privately " +
                            " or type public if you want to broadcast mes" +
                            "sage to all the other user");

                    String Mode = userInput.readLine();

                    if(!Mode.equalsIgnoreCase("public")){
                        System.out.print(STR."\{name} : ");
                        input = userInput.readLine();

                        if (input == null || input.equalsIgnoreCase("no")) {
                            running = false;
                            socket.close();
                            break;
                        }

                        out.println(STR."\{now.format(formatter)}|\{Mode}|\{name}|\{input}");
                    }else{
                        System.out.print(STR."\{name} : ");
                        input = userInput.readLine();

                        if (input == null || input.equalsIgnoreCase("no")) {
                            running = false;
                            socket.close();
                            break;
                        }

                        out.println(STR."[ \{now} ]|\{Mode}|\{name}|\{input}");
                    }

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
