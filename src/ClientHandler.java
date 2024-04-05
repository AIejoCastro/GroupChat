import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Chat chatServer;
    private BufferedReader in;
    private PrintWriter out;
    private User user;

    public ClientHandler(Socket clientSocket, Chat chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
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
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String command = parts[0].trim();
            String content = parts[1].trim();

            if (command.equals("msg")) {
                handleMessage(content);
            } else if (command.equals("msggroup")) {
                handleGroupMessage(content);
            } else if (command.equals("creategroup")) {
                handleCreateGroup(content);
            } else if (command.equals("joingroup")) {
                handleJoinGroup(content);
            } else if (command.equals("record")) {
                handleVoiceMessage(content);
            } else if (command.equals("history")) {
                handleHistoryRequest();
            } else {
                out.println("Comando desconocido");
            }
        } else {
            out.println("Formato de mensaje incorrecto");
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

    private void handleVoiceMessage(String message) {
        String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
            String recipient = parts[0];
            int duration = Integer.parseInt(parts[1]);

            // Imprimir el mensaje de inicio de grabación
            out.println("Grabando durante " + duration + " segundos...");

            // Iniciar la captura de audio desde el micrófono
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            RecordAudio recorder = new RecordAudio(format, duration, byteArrayOutputStream);
            Thread recorderThread = new Thread(recorder);
            recorderThread.start();

            // Esperar a que la grabación termine
            try {
                recorderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Obtener los datos de audio capturados
            byte[] audioData = byteArrayOutputStream.toByteArray();

            // Enviar el mensaje de voz al destinatario
            chatServer.sendMessageToUser(recipient, user.getUsername(), Base64.getEncoder().encodeToString(audioData));
            VoiceMessage voiceMessage = new VoiceMessage(user.getUsername(), recipient, audioData);
            addVoiceMessageToUserHistory(user.getUsername(), voiceMessage);

            // Agregar el mensaje de voz a la historia del usuario
            User recipientUser = chatServer.getUserByUsername(recipient);
            if (recipientUser != null) {
                recipientUser.addVoiceMessageToHistory(voiceMessage);
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
            out.println("From: " + msg.getFrom() + " / " + "To: " + msg.getTo() + " / " + "Message: " + msg.getMessage());
        }
    }

    public void addMessageToUserHistory(String username, Message messageToSend) {
        User senderUser = chatServer.getUserByUsername(username);
        if (senderUser != null) {
            senderUser.addMessageToHistory(messageToSend);
        }
    }

    public void addVoiceMessageToUserHistory(String username, VoiceMessage voiceMessage) {
        User senderUser = chatServer.getUserByUsername(username);
        if (senderUser != null) {
            senderUser.addVoiceMessageToHistory(voiceMessage);
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