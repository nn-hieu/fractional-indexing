package io.github.nnhieu.fractionalindexing.api;

import java.util.List;

public interface FractionalIndexer {

    String first();

    String after(String value);

    String before(String value);

    String between(String left, String right);

    List<String> rebalance(int size);

    static DefaultFractionalIndexer.Builder builder() {
        return new DefaultFractionalIndexer.Builder();
    }

    static FractionalIndexer defaultInstance() {
        return builder().build();
    }
}
