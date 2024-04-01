import javax.imageio.IIOException;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Chat chatServer;

    public ClientHandler(Socket clientSocket, Chat chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
    }

    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            String username = in.readLine();
            User user = new User(username, clientSocket);
            chatServer.addUser(username, clientSocket);

        } catch (IIOException e) {
            e.printStackTrace();
        }
    }
}
