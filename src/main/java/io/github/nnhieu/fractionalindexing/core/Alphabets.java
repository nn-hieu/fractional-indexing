package io.github.nnhieu.fractionalindexing.core;

public final class Alphabets {

    public static final Alphabet DIGITS = new DefaultAlphabet("0123456789");

    public static final Alphabet UPPERCASE = new DefaultAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public static final Alphabet LOWERCASE = new DefaultAlphabet("abcdefghijklmnopqrstuvwxyz");

    public static final Alphabet ALPHANUMERIC_UPPER = new DefaultAlphabet("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public static final Alphabet ALPHANUMERIC_LOWER = new DefaultAlphabet("0123456789abcdefghijklmnopqrstuvwxyz");

    public static final Alphabet LETTERS = new DefaultAlphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

    public static final Alphabet ALPHANUMERIC = new DefaultAlphabet("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

    private Alphabets() {
    }

    public static Alphabet of(String customChars) {
        return new DefaultAlphabet(customChars);
    }
}
