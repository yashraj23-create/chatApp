import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatServer {
    public static final String RED  = "\u001B[31m";

    public static HashMap<String, Group> groups = new HashMap<>();

    static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    static final AtomicInteger messageCounter = new AtomicInteger(0);

    private final List<String> history = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY = 20;

    final Map<String, Queue<String>> offlineQueue = new ConcurrentHashMap<>();

    public void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.name != null) {
                client.sendMessage(msg);
            }
        }
    }

    public void GroupMessage(String name , String message){
        Group group = groups.get(name);
        Set<String> set = group.getMembers();
        for(String s : set){
            for (ClientHandler client : clients) {
                if (client.name.equalsIgnoreCase(s)) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public boolean groupName(String msg){
        return !groups.containsKey(msg);
    }

    public boolean groupName1(String msg){
        return groups.containsKey(msg);
    }

   public void LogPrinter(String msg){
       System.out.println(STR."\{RED}" + msg);
   }

   void AddTo(String name, String[] mNames){
        Group group = new Group(name);
        for(String s : mNames){
            group.addMember(s);
        }
        groups.put(name,group);
   }

    public void SendingMsg(String name){
        System.out.println("creating group has been called");
        for (ClientHandler client : clients) {
            if (client.name.equalsIgnoreCase(name)) {
                client.sendMessage(" you have been added to group");
            }
        }
    }

    public Boolean CheckNames(String[] name){
        System.out.println("checking group name has been called");
        HashSet<String> h = new HashSet<>();
            for(ClientHandler client : clients){
                h.add(client.name);
            }
            for(String nam : name){
                if(!h.contains(nam)){
                    return false;
                }
            }
        System.out.println("name validation completed");
            return true;
    }

    public boolean sendPrivateTo(String name, String msg) {
        for (ClientHandler client : clients) {
            if (client.name != null && client.name.equalsIgnoreCase(name)) {
                client.sendMessage(msg);
                return true;
            }
        }
        return false;
    }

    public boolean searchname(String name) {
        for (ClientHandler cl : clients) {
            if (cl.name != null && cl.name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public boolean duplicates(String name) {
        for (ClientHandler cl : clients) {
            if (cl.name != null && cl.name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public void addToHistory(String msg) {
        history.add(msg);
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(38297);
        ChatServer server = new ChatServer();

        System.out.println("\u001B[32m[SERVER] Started on port 38297\u001B[0m");
        System.out.println("\u001B[33m[SERVER] Waiting for clients...\u001B[0m");

        ExecutorService pool = Executors.newCachedThreadPool();

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket, server);
            clients.add(handler);
            pool.submit(handler);
        }
    }
}