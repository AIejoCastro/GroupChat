import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User {

    private String username;
    private Socket socket;
    private List<Message> messageHistory;

    public User(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
        this.messageHistory = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public List<Message> getMessageHistory() {
        return messageHistory;
    }

    public void setMessageHistory(List<Message> messageHistory) {
        this.messageHistory = messageHistory;
    }

    public void addMessageToHistory(Message message) {
        messageHistory.add(message);
    }
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", socket=" + socket +
                ", messageHistory=" + messageHistory +
                '}';
    }
}