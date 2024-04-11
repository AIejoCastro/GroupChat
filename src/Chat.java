import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Chat {

    private static final int PORT = 6789;
    private ServerSocket serverSocket;
    private DatagramSocket datagramSocket;
    private ExecutorService threadPool;
    private List<Socket> clientSockets;
    private DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
    private Map<String, Socket> usernameToSocket;
    private Map<String, ChatGroup> groupNameToGroup;
    private Map<String, User> usernameToUser;

    public Chat() {
        try {
            serverSocket = new ServerSocket(PORT);
            datagramSocket = new DatagramSocket(PORT);
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
        System.out.println("Servidor iniciado. Esperando clientes... \n");
        System.out.println("Lista de comandos: \n" +
                "msg: [nombre_usuario] [nombre_destinatario] - Enviar mensaje a un usuario\n" +
                "msggroup: [nombre_grupo] [mensaje] - Enviar mensaje a un grupo\n" +
                "creategroup: [nombre_grupo] - Crear un grupo\n" +
                "joingroup: [nombre_grupo] [nombre_usuario] - Unirse a un grupo\n" +
                "audio: [usuario] - Enviar archivo de audio a un usuario\n");
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

    public void sendAudioToUser(String recipientUsername, String senderUsername, byte[] audioData) {
        Socket recipientSocket = usernameToSocket.get(recipientUsername);
        if (recipientSocket != null) {
            try {
                PrintWriter out = new PrintWriter(recipientSocket.getOutputStream(), true);
                out.println(senderUsername + ": audio");

                // Dividir los datos de audio en fragmentos más pequeños
                int chunkSize = 1024; // Tamaño de cada fragmento
                int offset = 0;
                while (offset < audioData.length) {
                    int length = Math.min(chunkSize, audioData.length - offset);
                    byte[] chunk = Arrays.copyOfRange(audioData, offset, offset + length);

                    // Envía el fragmento de audio al cliente
                    DatagramPacket sendPacket = new DatagramPacket(chunk, length, recipientSocket.getInetAddress(), recipientSocket.getPort());
                    datagramSocket.send(sendPacket);

                    offset += length;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El usuario " + recipientUsername + " no está conectado.");
        }
    }

    public User getUserByUsername(String username) {
        return usernameToUser.get(username);
    }

    public ChatGroup getGroupByName(String groupName) {
        return groupNameToGroup.get(groupName);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public List<Socket> getClientSockets() {
        return clientSockets;
    }

    public void setClientSockets(List<Socket> clientSockets) {
        this.clientSockets = clientSockets;
    }

    public DatagramPacket getReceivePacket() {
        return receivePacket;
    }

    public void setReceivePacket(DatagramPacket receivePacket) {
        this.receivePacket = receivePacket;
    }

    public Map<String, Socket> getUsernameToSocket() {
        return usernameToSocket;
    }

    public void setUsernameToSocket(Map<String, Socket> usernameToSocket) {
        this.usernameToSocket = usernameToSocket;
    }

    public Map<String, ChatGroup> getGroupNameToGroup() {
        return groupNameToGroup;
    }

    public void setGroupNameToGroup(Map<String, ChatGroup> groupNameToGroup) {
        this.groupNameToGroup = groupNameToGroup;
    }

    public Map<String, User> getUsernameToUser() {
        return usernameToUser;
    }

    public void setUsernameToUser(Map<String, User> usernameToUser) {
        this.usernameToUser = usernameToUser;
    }
}