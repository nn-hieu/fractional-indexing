package io.github.nnhieu.fractionalindexing.api;

import io.github.nnhieu.fractionalindexing.core.Alphabet;
import io.github.nnhieu.fractionalindexing.core.Alphabets;
import io.github.nnhieu.fractionalindexing.core.IndexGenerator;
import io.github.nnhieu.fractionalindexing.core.IndexValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultFractionalIndexer implements FractionalIndexer {

    private final IndexGenerator generator;
    private final IndexValidator validator;

    private DefaultFractionalIndexer(Alphabet alphabet) {
        this.generator = new IndexGenerator(alphabet);
        this.validator = new IndexValidator(alphabet);
    }

    @Override
    public String first() {
        return this.generator.generateFirst();
    }

    @Override
    public String after(String index) {
        this.validateInput(index);
        return this.generator.generateAfter(index);
    }

    @Override
    public String before(String index) {
        this.validateInput(index);
        return this.generator.generateBefore(index);
    }

    @Override
    public String between(String left, String right) {
        this.validateInput(left);
        this.validateInput(right);
        return this.generator.generateBetween(left, right);
    }

    @Override
    public List<String> rebalance(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        List<String> result = new ArrayList<>(size);
        this.generateBalancedRecursive(size, null, null, result);
        return result;
    }

    private void generateBalancedRecursive(int count, String left, String right, List<String> result) {
        if (count == 0) {
            return;
        }
        if (count == 1) {
            result.add(this.generateForBoundary(left, right));
            return;
        }

        int mid = count / 2;
        String midValue = this.generateForBoundary(left, right);

        this.generateBalancedRecursive(mid, left, midValue, result);
        result.add(midValue);
        this.generateBalancedRecursive(count - mid - 1, midValue, right, result);
    }

    private String generateForBoundary(String left, String right) {
        if (left == null && right == null) {
            return this.generator.generateFirst();
        } else if (left == null) {
            return this.generator.generateBefore(right);
        } else if (right == null) {
            return this.generator.generateAfter(left);
        } else {
            return this.generator.generateBetween(left, right);
        }
    }

    private void validateInput(String index) {
        Objects.requireNonNull(index);
        this.validator.validate(index);
    }

    public static class Builder {

        private static final Alphabet DEFAULT_ALPHABET = Alphabets.ALPHANUMERIC;

        private Alphabet alphabet;

        public Builder alphabet(Alphabet alphabet) {
            this.alphabet = alphabet;
            return this;
        }

        public FractionalIndexer build() {
            if (this.alphabet == null) {
                this.alphabet = DEFAULT_ALPHABET;
            }
            return new DefaultFractionalIndexer(this.alphabet);
        }
    }
}
