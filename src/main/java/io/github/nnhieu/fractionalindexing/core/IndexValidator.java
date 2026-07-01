package io.github.nnhieu.fractionalindexing.core;

public final class IndexValidator {

    private final Alphabet alphabet;

    public IndexValidator(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public void validate(String index) {
        if (index == null || index.isBlank()) {
            throw new InvalidIndexException("Index cannot be null or empty");
        }
        for (int i = 0; i < index.length(); ++i) {
            if (this.alphabet.indexOf(index.charAt(i)) < 0) {
                throw new InvalidIndexException("Index contains invalid character");
            }
        }
        char lastChar = index.charAt(index.length() - 1);
        if (lastChar == this.alphabet.charAt(0)) {
            throw new InvalidIndexException("Index cannot end with the minimum character of the alphabet");
        }
        if (lastChar == this.alphabet.charAt(this.alphabet.length() - 1)) {
            throw new InvalidIndexException("Index cannot end with the maximum character of the alphabet");
        }
    }
}
