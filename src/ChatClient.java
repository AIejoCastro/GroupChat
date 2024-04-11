import java.io.*;
import java.net.*;

public class ChatClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private AudioRecorder audioRecorder;

    public ChatClient() {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            serverPort = 9876;
            audioRecorder = new AudioRecorder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAudioMessage(String recipient) {
        try {
            // Inicia la grabación de audio
            audioRecorder.startRecording();

            // Espera durante unos segundos para grabar audio
            Thread.sleep(5000); // Grabará durante 5 segundos

            // Detiene la grabación de audio
            audioRecorder.stopRecording();

            // Obtiene los datos de audio grabados
            byte[] audioData = audioRecorder.getRecordedAudio();

            // Envía el mensaje de audio al servidor
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(new AudioMessage(recipient, audioData));
            objectOutputStream.flush();
            byte[] messageData = outputStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(messageData, messageData.length, serverAddress, serverPort);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Debe especificar el nombre del destinatario.");
            return;
        }
        String recipient = args[0];
        ChatClient client = new ChatClient();
        client.sendAudioMessage(recipient);
    }
}
