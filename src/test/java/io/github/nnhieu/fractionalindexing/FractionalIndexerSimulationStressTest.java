package io.github.nnhieu.fractionalindexing;

import io.github.nnhieu.fractionalindexing.api.FractionalIndexer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FractionalIndexerSimulationStressTest {

    // =========================================================================
    // TEST CONFIGURATION
    // =========================================================================

    private static final int INITIAL_ELEMENTS_COUNT = 100_000;
    private static final int CHAOS_ITERATIONS = 1_000_000;
    private static final int PROBABILITY_REORDER = 60;
    private static final int PROBABILITY_INSERT = 25;
    private static final int PROBABILITY_DELETE = 15;

    private static final long RANDOM_SEED = 42L;

    private static class Task implements Comparable<Task> {
        private final String id;
        private String index;
        private final String name;

        public Task(String id, String index, String name) {
            this.id = id;
            this.index = index;
            this.name = name;
        }

        public String getIndex() {
            return this.index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        @Override
        public int compareTo(Task other) {
            return this.index.compareTo(other.getIndex());
        }

        @Override
        public String toString() {
            return String.format(
                    "Index: %-15s | Length: %-3d | Task: %s",
                    this.index,
                    this.index.length(),
                    this.name
            );
        }
    }

    @Test
    public void stressTestRandomTaskOperations() {
        FractionalIndexer indexer = FractionalIndexer.defaultInstance();
        List<Task> tasks = new ArrayList<>();
        Random random = new Random(RANDOM_SEED);

        System.out.println("--- STEP 1: INITIALIZING " + INITIAL_ELEMENTS_COUNT + " ITEMS ---");

        String currentIndex = indexer.first();

        for (int i = 0; i < INITIAL_ELEMENTS_COUNT; i++) {
            tasks.add(new Task("TASK_" + i, currentIndex, "Task " + i));
            currentIndex = indexer.after(currentIndex);
        }

        System.out.println("Initial item count: " + tasks.size());
        System.out.println();

        System.out.println("--- STEP 2: RUNNING RANDOM OPERATIONS (" + CHAOS_ITERATIONS + " OPERATIONS) ---");

        int insertCount = 0;
        int totalProbability = PROBABILITY_REORDER + PROBABILITY_INSERT + PROBABILITY_DELETE;

        for (int i = 0; i < CHAOS_ITERATIONS; i++) {
            if (tasks.size() < 2) {
                break;
            }

            int action = random.nextInt(totalProbability);

            if (action < PROBABILITY_REORDER) {

                int fromIdx = random.nextInt(tasks.size());
                Task itemToMove = tasks.remove(fromIdx);

                int toIdx = random.nextInt(tasks.size() + 1);

                String newIndex;

                if (toIdx == 0) {
                    newIndex = indexer.before(tasks.get(0).getIndex());
                } else if (toIdx == tasks.size()) {
                    newIndex = indexer.after(tasks.get(tasks.size() - 1).getIndex());
                } else {
                    newIndex = indexer.between(
                            tasks.get(toIdx - 1).getIndex(),
                            tasks.get(toIdx).getIndex()
                    );
                }

                itemToMove.setIndex(newIndex);
                tasks.add(toIdx, itemToMove);

            } else if (action < (PROBABILITY_REORDER + PROBABILITY_INSERT)) {

                int insertPos = random.nextInt(tasks.size() + 1);

                String newIndex;

                if (insertPos == 0) {
                    newIndex = indexer.before(tasks.get(0).getIndex());
                } else if (insertPos == tasks.size()) {
                    newIndex = indexer.after(tasks.get(tasks.size() - 1).getIndex());
                } else {
                    newIndex = indexer.between(
                            tasks.get(insertPos - 1).getIndex(),
                            tasks.get(insertPos).getIndex()
                    );
                }

                insertCount++;
                tasks.add(
                        insertPos,
                        new Task("NEW_" + insertCount, newIndex, "New Task " + insertCount)
                );

            } else {

                int removeIdx = random.nextInt(tasks.size());
                tasks.remove(removeIdx);
            }
        }

        System.out.println("Completed " + CHAOS_ITERATIONS + " operations.");
        System.out.println();

        System.out.println("--- STEP 3: STATISTICS BEFORE REBALANCING ---");

        Collections.sort(tasks);

        printStatistics(tasks);

        System.out.println();
        System.out.println("--- STEP 4: REBALANCING ALL INDICES ---");

        long startTime = System.currentTimeMillis();

        List<String> balancedIndices = indexer.rebalance(tasks.size());

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setIndex(balancedIndices.get(i));
        }

        long endTime = System.currentTimeMillis();

        System.out.println(
                "Rebalanced "
                        + tasks.size()
                        + " items in "
                        + (endTime - startTime)
                        + " ms"
        );

        System.out.println();

        System.out.println("--- STEP 5: STATISTICS AFTER REBALANCING ---");

        printStatistics(tasks);
    }

    private void printStatistics(List<Task> tasks) {

        int maxLength = 0;
        int totalLength = 0;
        String longestIndex = "";

        for (Task task : tasks) {
            int len = task.getIndex().length();

            totalLength += len;

            if (len > maxLength) {
                maxLength = len;
                longestIndex = task.getIndex();
            }
        }

        System.out.println("=========================================");
        System.out.println("Total items           : " + tasks.size());
        System.out.println("Longest index length  : " + maxLength + " characters");
        System.out.println("Longest index         : " + longestIndex);
        System.out.println("Average index length  : "
                + String.format("%.2f", (double) totalLength / tasks.size())
                + " characters");
        System.out.println("=========================================");
    }
}