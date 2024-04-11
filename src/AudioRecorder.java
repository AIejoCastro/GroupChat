import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class AudioRecorder {
    private TargetDataLine line;
    private ByteArrayOutputStream byteArrayOutputStream;
    private boolean isRecording;

    public AudioRecorder() {
        this.isRecording = false;
    }

    public void startRecording() {
        try {
            // Configura la línea de captura de audio desde el micrófono
            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);

            // Inicia la captura de audio desde el micrófono
            line.start();
            isRecording = true;

            // Inicializa el buffer para almacenar los datos de audio
            byteArrayOutputStream = new ByteArrayOutputStream();

            // Crea un hilo para leer datos de audio desde la línea de captura y escribirlos en el buffer
            Thread recordThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while (isRecording) {
                    bytesRead = line.read(buffer, 0, buffer.length);
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            });
            recordThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            if (isRecording) {
                // Detiene la captura de audio desde el micrófono
                line.stop();
                line.close();
                isRecording = false;

                // Detiene el hilo de grabación
                byteArrayOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getRecordedAudio() {
        // Retorna los datos de audio grabados como un arreglo de bytes
        return byteArrayOutputStream.toByteArray();
    }

    public void reset() {
        // Reinicia el estado del AudioRecorder para futuras grabaciones
        line = null;
        byteArrayOutputStream = null;
        isRecording = false;
    }
}
