import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Chat chatServer;
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private Map<String, Socket> usernameToSocket;
    private AudioRecorder audioRecorder;

    public ClientHandler(Socket clientSocket, Chat chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
        this.usernameToSocket = chatServer.getUsernameToSocket();
        audioRecorder = new AudioRecorder();
    }

    @Override
    public void run() {
        try {
            setupStreams();

            String username = in.readLine();
            user = new User(username, clientSocket);
            chatServer.addUser(username, clientSocket);

            String message;
            out.println("Bienvenido al chat, las instrucciones para usarlo están en el README");

            while ((message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    private void processMessage(String message) {
        if (message.startsWith("msg: ")) {
            handleMessage(message);
        } else if (message.startsWith("msggroup: ")) {
            handleGroupMessage(message);
        } else if (message.startsWith("creategroup: ")) {
            handleCreateGroup(message);
        } else if (message.startsWith("joingroup: ")) {
            handleJoinGroup(message);
        } else if (message.startsWith("history")) {
            handleHistoryRequest();
        } else if (message.startsWith("audio: ")) {
            handleAudioMsg(message);
        } else {
            out.println("No existe ese comando");
        }
    }

    private void handleMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String recipient = parts[1];
            String messageContent = parts[2];
            chatServer.sendMessageToUser(recipient, user.getUsername(), messageContent);
            Message messageSend = new Message(user.getUsername(), recipient, messageContent);
            addMessageToUserHistory(user.getUsername(), messageSend);
            User recipientUser = chatServer.getUserByUsername(recipient);
            if (recipientUser != null) {
                recipientUser.addMessageToHistory(messageSend);
            }
        }
    }

    private void handleGroupMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String groupName = parts[1];
            String messageContent = parts[2];
            if (!userInGroup(user.getUsername(), groupName)) {
                out.println("No pertenece a este grupo, no puedes enviar mensajes");
            } else {
                chatServer.sendMessageToGroup(groupName, user.getUsername(), messageContent);
                Message messageSend = new Message(user.getUsername(), "Grupo-" + groupName, messageContent);
                addMessageToUserHistory(user.getUsername(), messageSend);
                ChatGroup recipientGroup = chatServer.getGroupByName(groupName);
                if (recipientGroup != null) {
                    System.out.println(messageSend);
                    for (User groupUser : recipientGroup.getUsers()) {
                        groupUser.addMessageToHistory(messageSend);
                    }
                }
            }
        }
    }

    private void handleCreateGroup(String message) {
        String[] parts = message.split(" ", 2);
        String groupName = parts[1];
        if (parts.length == 2) {
            chatServer.createGroup(groupName, user.getUsername());
        }
    }

    private void handleJoinGroup(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String groupName = parts[1];
            String usernameToAdd = parts[2];
            chatServer.addUserToGroup(groupName, usernameToAdd);
        }
    }

    private void handleHistoryRequest() {
        List<Message> messageHistory = user.getMessageHistory();
        out.println("Historial de mensajes:");
        for (Message msg : messageHistory) {
            out.println(msg.toString());
        }
    }

    private void handleAudioMsg(String message) {
        String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
            String recipient = parts[1];
            // Inicia la grabación de audio
            audioRecorder.startRecording();
            // Espera durante unos segundos para grabar el audio
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            audioRecorder.stopRecording();
            byte[] audioData = audioRecorder.getRecordedAudio();
            chatServer.sendAudioToUser(recipient, user.getUsername(), audioData);
        } else {
            System.out.println("Formato de mensaje de audio incorrecto. Debe ser 'audio: NombreDestinatario'.");
        }
    }

    public void addMessageToUserHistory(String username, Message messageToSend) {
        User senderUser = chatServer.getUserByUsername(username);
        if (senderUser != null) {
            senderUser.addMessageToHistory(messageToSend);
        }
    }

    public boolean userInGroup(String username, String groupName) {
        ChatGroup group = chatServer.getGroupByName(groupName);
        boolean result = false;
        if (group != null) {
            for (User groupUser : group.getUsers()) {
                if (groupUser.getUsername().equals(username)){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}