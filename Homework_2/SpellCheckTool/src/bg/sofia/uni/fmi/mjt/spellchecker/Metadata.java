package bg.sofia.uni.fmi.mjt.spellchecker;

public record Metadata(int characters, int words, int mistakes) {
    public String formattedMetadata() {
        return this.characters + " characters, "
                + this.words + " words, "
                + this.mistakes + " spelling issue(s) found";
    }
}
