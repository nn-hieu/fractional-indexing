package io.github.nnhieu.fractionalindexing.core;

import java.util.Arrays;

public class DefaultAlphabet implements Alphabet {

    private final String chars;
    private final int[] charMap;

    public DefaultAlphabet(String chars) {
        if (chars == null || chars.length() < 2) {
            throw new IllegalArgumentException("Alphabet must contain at least 2 characters");
        }
        this.chars = chars;
        this.charMap = new int[Character.MAX_VALUE + 1];
        Arrays.fill(this.charMap, -1);
        for (int i = 0; i < this.chars.length(); ++i) {
            char c = this.chars.charAt(i);
            if (this.charMap[c] != -1) {
                throw new IllegalArgumentException("Alphabet cannot contain duplicate characters");
            }
            this.charMap[c] = i;
        }
    }

    @Override
    public char charAt(int index) {
        return this.chars.charAt(index);
    }

    @Override
    public int indexOf(char c) {
        int index = this.charMap[c];
        if (index == -1) {
            throw new IllegalArgumentException(String.format("Character '%c' not found in alphabet", c));
        }
        return index;
    }

    @Override
    public int length() {
        return this.chars.length();
    }

    @Override
    public String getChars() {
        return this.chars;
    }
}
