import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group {

    private final String name;
    private final Set<String> members;
    private final List<String> messages;

    public Group(String name) {
        this.name = name;
        this.members = new HashSet<>();
        this.messages = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public boolean contains(String member) {
        return members.contains(member);
    }

    public void addMessage(String msg) {
        messages.add(msg);
    }

    public List<String> getMessages() {
        return messages;
    }

}