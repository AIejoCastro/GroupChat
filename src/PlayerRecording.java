import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class PlayerRecording {
    private AudioFormat format;
    private byte[] audioData;
    private SourceDataLine sourceDataLine;
    private ByteArrayInputStream byteArrayInputStream;

    public PlayerRecording(AudioFormat format) {
        this.format = format;
    }

    public void initiateAudio(byte[] audioData) {
        try {
            this.audioData = audioData;
            byteArrayInputStream = new ByteArrayInputStream(audioData);
            sourceDataLine = AudioSystem.getSourceDataLine(format);
            sourceDataLine.open(format);
            sourceDataLine.start();
            playAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playAudio() {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = byteArrayInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
            byteArrayInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}