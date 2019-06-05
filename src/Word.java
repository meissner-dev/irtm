public class Word {
    private String text;
    private int position;
    private int sentenceId;

    public Word(String text, int position, int sentenceId) {
        this.text = text;
        this.position = position;
        this.sentenceId = sentenceId;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    public int getSentenceId() {
        return sentenceId;
    }
}
