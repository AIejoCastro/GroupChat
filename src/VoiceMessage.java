public class VoiceMessage extends Message {
    private byte[] audioData;

    public VoiceMessage(String sender, String recipient, byte[] audioData) {
        super(sender, recipient, ""); // Llamada al constructor de Message
        this.audioData = audioData;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }
}
