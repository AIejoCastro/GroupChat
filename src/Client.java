import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class Client {

    private static final int PORT = 6789;
    private static final String ADDRESS = "localhost";

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(ADDRESS, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);

            System.out.println("¿Cuál es tu nombre?");
            String username = sc.nextLine();
            out.println(username);

            Thread readerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("[VOICE]")) {
                            // Decodificar el mensaje de audio y reproducirlo
                            String base64Audio = message.substring("[VOICE]".length());
                            byte[] audioData = Base64.getDecoder().decode(base64Audio);
                            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
                            PlayerRecording player = new PlayerRecording(format);
                            player.initiateAudio(audioData);
                        } else {
                            System.out.println(message);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readerThread.start();

            String message;
            while (true) {
                message = sc.nextLine();
                out.println(message);
            }

        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
