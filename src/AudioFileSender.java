import java.io.*;
import java.net.*;

public class AudioFileSender {

    private static final int BUFFER_SIZE = 1024; // Tamaño del buffer de lectura/escritura

    public static void sendAudioFile(String filePath, String recipient, InetAddress serverAddress, int serverPort) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             DatagramSocket socket = new DatagramSocket()) {

            // Lee el archivo de audio
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverAddress, serverPort);
                socket.send(packet);
            }

            // Envío del mensaje de finalización del archivo
            byte[] endMessage = "END_OF_FILE".getBytes();
            DatagramPacket endPacket = new DatagramPacket(endMessage, endMessage.length, serverAddress, serverPort);
            socket.send(endPacket);

            System.out.println("Archivo de audio enviado correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filePath = "./audios/audio.wav";
        String recipient = "NombreDestinatario";
        String serverHost = "localhost";
        int serverPort = 9876;

        try {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            sendAudioFile(filePath, recipient, serverAddress, serverPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
