import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Chat {

    private static final int PORT = 6789;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private List<Socket> clientSockets;
    private Map<String, Socket> usernameToSocket;
    private Map<String, ChatGroup> groupNameToGroup;
    private Map<String, User> usernameToUser;

    public Chat() {
        try {
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newFixedThreadPool(15);
            clientSockets = new ArrayList<>();
            usernameToSocket = new HashMap<>();
            groupNameToGroup = new HashMap<>();
            usernameToUser = new HashMap<>();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Servidor iniciado. Esperando clientes...");
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket);
                clientSockets.add(clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(String username, Socket socket) {
        User user = new User(username, socket);
        usernameToSocket.put(username, socket);
        usernameToUser.put(username, user);
        System.out.println("Usuario '" + username + "' conectado desde " + socket.getInetAddress());
    }

    public void createGroup(String groupName, String username) {
        ChatGroup newGroup = new ChatGroup();
        groupNameToGroup.put(groupName, newGroup);
        System.out.println("Nuevo grupo '" + groupName + "' creado.");
        addUserToGroup(groupName, username);
    }

    public void sendMessageToGroup(String groupName, String senderUsername, String message) {
        ChatGroup group = groupNameToGroup.get(groupName);
        if (group != null) {
            for (User user : group.getUsers()) {
                if (!user.getUsername().equals(senderUsername)) {
                    sendMessageToUser(user.getUsername(), senderUsername, "[" + groupName + "] " + message);
                }
            }
        } else {
            System.out.println("El grupo " + groupName + " no existe.");
        }
    }

    public void sendMessageToUser(String recipientUsername, String senderUsername,String message) {
        Socket recipientSocket = usernameToSocket.get(recipientUsername);
        if (recipientSocket != null) {
            try {
                PrintWriter out = new PrintWriter(recipientSocket.getOutputStream(), true);
                out.println(senderUsername + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El usuario " + recipientUsername + " no está conectado.");
        }
    }

    public void sendVoiceMessageToUser(String recipientUsername, String senderUsername, byte[] audioData) {
        Socket recipientSocket = usernameToSocket.get(recipientUsername);
        if (recipientSocket != null) {
            try {
                OutputStream outputStream = recipientSocket.getOutputStream();
                // Primero, enviar la longitud de los datos de audio
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeInt(audioData.length);
                // Luego, enviar los datos de audio
                outputStream.write(audioData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El usuario " + recipientUsername + " no está conectado.");
        }
    }


    public void addUserToGroup(String groupName, String username) {
        ChatGroup group = groupNameToGroup.get(groupName);
        if (group != null) {
            for (User user : group.getUsers()) {
                if (user.getUsername().equals(username)) {
                    System.out.println("El usuario ya está en el grupo.");
                    return;
                }
            }
            Socket socket = usernameToSocket.get(username);
            if (socket != null) {
                User user = new User(username, socket);
                group.addUser(user);
                System.out.println("Usuario '" + username + "' añadido al grupo '" + groupName + "'.");
            } else {
                System.out.println("Usuario '" + username + "' no encontrado.");
            }
        } else {
            System.out.println("El grupo '" + groupName + "' no existe.");
        }
    }

    public User getUserByUsername(String username) {
        return usernameToUser.get(username);
    }

    public ChatGroup getGroupByName(String groupName) {
        return groupNameToGroup.get(groupName);
    }
}