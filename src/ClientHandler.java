import java.io.*;
import java.net.Socket;
import java.util.Queue;

public class ClientHandler implements Runnable {

    String name;
    Socket socket;
    ChatServer server;
    BufferedReader in;
    PrintWriter out;
    static final String RESET  = "\u001B[0m";
    static final String YELLOW = "\u001B[33m";
    static final String CYAN   = "\u001B[36m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void sendControl(String msg) {
        out.println("---##" + msg);
    }

    public void send1Control(String msg) {
        out.println("---q11" + msg);
    }

    public void sendC2Control(String msg) {
        out.println("---q12" + msg);
    }

    public void sendC3Control(String msg) {
        out.println("---q13" + msg);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String input = in.readLine();
                if (input == null) return;
                input = input.trim();

                if (input.isBlank()) {
                    sendMessage("Name cannot be empty");
                    continue;
                }

                if (server.duplicates(input)) {
                    sendMessage("Username already taken, try another");
                } else {
                    this.name = input;
                    sendMessage("ACCEPTED");
                    System.out.println(STR."\{GREEN}[+] \{name} connected\{RESET}");

                    Queue<String> pending = server.offlineQueue.get(name.toLowerCase());
                    if (pending != null && !pending.isEmpty()) {
                        sendMessage(STR."\{YELLOW}[SERVER] You have \{pending.size()} unread message(s):\{RESET}");
                        while (!pending.isEmpty()) {
                            sendMessage(pending.poll());
                        }
                        server.offlineQueue.remove(name.toLowerCase());
                    }
                    break;
                }
            }

                server.broadcast(STR."\{YELLOW}[SERVER] \{name} joined the chat.\{RESET}", this);

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("CMD|")) {
                    handleCommand(line.substring(4));
                    continue;
                }

                if(line.startsWith("/GM")) {
                    String[] part = line.substring(4).split("\\|");
                    String msg = STR."\{part[0]} \{part[1]} : \{part[3]}";
                    server.GroupMessage(part[2],msg);
                }

                if(line.startsWith("/group messages ")){
                  String[] part = line.split("\\|");
                  String groupName = part[0].substring(16).trim();
                  server.sendM(groupName,part[1]);

                }

                if(line.startsWith("/GC")){
                    server.LogPrinter("hey this group creation has started");
                    int end = line.indexOf(" ", 4);

                  String groupName = line.substring(4,end);

                  String names[] = line.substring(end).split(" ");

                  server.AddTo(groupName,names);
                   server.LogPrinter("hey names are being send");
                  for(String Nm : names){
                      server.SendingMsg(Nm);
                  }
                  continue;
                }

                String[] parts = line.split("\\|", 5);
                if (parts.length < 5) continue;

                String time      = parts[0];
                String mode      = parts[1];
                String sender    = parts[2];
                String recipient = parts[3];
                String body      = parts[4];

                if (mode.equalsIgnoreCase("public")) {
                    String formatted = formatPublic(time, sender, body);
                    server.broadcast(formatted, this);
                    server.addToHistory(formatted);

                } else if (mode.equalsIgnoreCase("dm")) {
                    String formatted = formatPrivate(time, sender, recipient, body);
                    boolean delivered = server.sendPrivateTo(recipient, formatted);

                    if (delivered) {
                        sendMessage(formatted);
                    } else {
                        sendMessage(STR."\{YELLOW}[SERVER] \{recipient} is offline. Message queued.\{RESET}");
                    }
                    server.addToHistory(formatted);
                }
            }

        } catch (Exception e) {
        } finally {
            cleanup();
        }
    }

    private void handleCommand(String cmd) {
        if(cmd.startsWith("GROUP|")){
            String group = cmd.substring(6);
            if(server.groupName(group)){
                sendC2Control("valid group name");
            }else{
                sendC2Control("group name already taken");
            }
        }

        if(cmd.startsWith("group|")){
            String group = cmd.substring(6);
            if(server.groupName1(group)){
                sendC3Control("valid group name");
            }else{
                sendC3Control("incorrect groupName");
            }
        }

        if (cmd.startsWith("check|")) {
            String target = cmd.substring(6).trim();
            if (server.searchname(target)) {
                sendControl("valid");
            } else {
                sendControl("not exist");
            }
        }

        else if(cmd.startsWith("CHECK|")){
            String names[] = cmd.substring(6).trim().split("\\s+");
            boolean get = server.CheckNames(names);
            if(!get){
                server.LogPrinter("username doesn't exist");
                send1Control("username doesn't exist");
            }else{
                server.LogPrinter("valid1");
                send1Control("valid1");
            }
        }

        else if (cmd.equalsIgnoreCase("online")) {
            StringBuilder sb = new StringBuilder();
            sb.append(YELLOW).append("[SERVER] Online users: ").append(RESET);
            for (ClientHandler c : ChatServer.clients) {
                if (c.name != null) {
                    sb.append(c.name.equals(this.name) ? STR."\{GREEN}\{c.name} (you)\{RESET}" : c.name);
                    sb.append(" ");
                }
            }
            sendMessage(sb.toString().trim());

        } else if (cmd.equalsIgnoreCase("history")) {
            var history = server.getHistory();
            if (history.isEmpty()) {
                sendMessage(STR."\{YELLOW}[SERVER] No messages yet.\{RESET}");
            } else {
                sendMessage(STR."\{YELLOW}[SERVER] Last \{history.size()} messages:\{RESET}");
                history.forEach(this::sendMessage);
            }

        } else if (cmd.equalsIgnoreCase("quit")) {
            cleanup();
        }
    }

    private String formatPublic(String time, String sender, String body) {
        int id = ChatServer.messageCounter.incrementAndGet();
        return String.format(STR."\{CYAN}[#%d]\{RESET} [%s] \{GREEN}%s\{RESET} → all: %s",
                id, time, sender, body);
    }

    private String formatPrivate(String time, String sender, String recipient, String body) {
        int id = ChatServer.messageCounter.incrementAndGet();
        return String.format(STR."\{CYAN}[#%d]\{RESET} [%s] \u001B[35m%s\{RESET} → \u001B[35m%s\{RESET}: %s",
                id, time, sender, recipient, body);
    }

    private void cleanup() {
        ChatServer.clients.remove(this);
        if (name != null) {
            System.out.println(STR."\{RED}[-] \{name} disconnected\{RESET}");
            server.broadcast(STR."\{YELLOW}[SERVER] \{name} left the chat.\{RESET}", this);
        }
        try { socket.close(); } catch (Exception ignored) {}
    }
}
