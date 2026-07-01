package io.github.nnhieu.fractionalindexing.core;

public final class IndexGenerator {

    private final Alphabet alphabet;

    public IndexGenerator(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public String generateFirst() {
        int midIndex = this.alphabet.length() / 2;
        return String.valueOf(this.alphabet.charAt(midIndex));
    }

    public String generateBetween(String left, String right) {
        if (left == null || left.isBlank() || right == null || right.isBlank()) {
            throw new IllegalArgumentException("Index cannot be null or blank");
        }
        if (left.compareTo(right) >= 0) {
            throw new IllegalArgumentException("Left index must be less than right index.");
        }
        StringBuilder result = new StringBuilder();
        boolean isLimitedByRight = !right.isEmpty();
        int position = 0;
        while (true) {
            int leftCharIndex = (position < left.length()) ? this.alphabet.indexOf(left.charAt(position)) : 0;
            int rightCharIndex = this.alphabet.length() - 1;
            if (isLimitedByRight) {
                rightCharIndex = (position < right.length()) ? this.alphabet.indexOf(right.charAt(position)) : 0;
            }
            if (leftCharIndex == rightCharIndex) {
                if (leftCharIndex == 0 && position >= left.length() && position >= right.length()) {
                    throw new InvalidIndexException(String.format("Cannot generate between %s and %s", left, right));
                }
                result.append(this.alphabet.charAt(leftCharIndex));
                ++position;
                continue;
            }
            if (rightCharIndex - leftCharIndex > 1) {
                int middleCharIndex = leftCharIndex + (rightCharIndex - leftCharIndex) / 2;
                result.append(this.alphabet.charAt(middleCharIndex));
                break;
            } else {
                result.append(this.alphabet.charAt(leftCharIndex));
                isLimitedByRight = false;
                ++position;
            }
        }
        return result.toString();
    }

    public String generateAfter(String index) {
        if (index == null || index.isBlank()) {
            throw new IllegalArgumentException("Index cannot be null or blank");
        }
        int maxSafeIndex = this.alphabet.length() - 2;

        for (int i = index.length() - 1; i >= 0; i--) {
            char c = index.charAt(i);
            int charIndex = this.alphabet.indexOf(c);

            if (charIndex < maxSafeIndex) {
                char newChar = this.alphabet.charAt(charIndex + 1);
                return index.substring(0, i) + newChar;
            }
        }
        return index + this.alphabet.charAt(1);
    }

    public String generateBefore(String index) {
        if (index == null || index.isEmpty()) {
            throw new IllegalArgumentException("Index cannot be null or blank");
        }
        int lastPosition = index.length() - 1;
        char lastChar = index.charAt(lastPosition);
        int charIndex = this.alphabet.indexOf(lastChar);
        char newLastChar = this.alphabet.charAt(charIndex - 1);
        String candidate = index.substring(0, lastPosition) + newLastChar;
        if (charIndex - 1 == 0) {
            int maxSafeIndex = this.alphabet.length() - 2;
            return candidate + this.alphabet.charAt(maxSafeIndex);
        }
        return candidate;
    }
}
