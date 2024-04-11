import java.io.Serializable;

public class AudioMessage implements Serializable {
    private String recipient;
    private byte[] audioData;

    public AudioMessage(String recipient, byte[] audioData) {
        this.recipient = recipient;
        this.audioData = audioData;
    }

    public String getRecipient() {
        return recipient;
    }

    public byte[] getAudioData() {
        return audioData;
    }
}
