package io.crunch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataProcess_Sonnet4_6_Test {

    private DataProcess dataProcess;

    @BeforeEach
    void setUp() {
        dataProcess = new DataProcess();
    }

    // Helper to create a valid AdditionalData with given queueSize and throughputRatio
    private AdditionalData data(int queueSize, double throughputRatio) {
        return new AdditionalData(0, queueSize, 10, throughputRatio, 0.0, 60);
    }

    @Test
    void emptyMapReturnsEmptyResult() {
        Map<Long, AdditionalData> input = new HashMap<>();
        Map<Long, AdditionalData> result = dataProcess.addData(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void nullValuesAreFilteredOut() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, null);
        input.put(2L, null);
        Map<Long, AdditionalData> result = dataProcess.addData(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void zeroQueueSizeIsFilteredOut() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(0, 1.0));
        Map<Long, AdditionalData> result = dataProcess.addData(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void negativeQueueSizeIsFilteredOut() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(-5, 1.0));
        Map<Long, AdditionalData> result = dataProcess.addData(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void singleValidEntryIsReturned() {
        AdditionalData entry = data(5, 0.8);
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, entry);
        Map<Long, AdditionalData> result = dataProcess.addData(input);
        assertEquals(1, result.size());
        assertEquals(entry, result.get(1L));
    }

    @Test
    void resultSortedByThroughputRatioAscending() {
        Map<Long, AdditionalData> input = new HashMap<>();
        AdditionalData low  = data(5, 0.2);
        AdditionalData mid  = data(5, 0.5);
        AdditionalData high = data(5, 0.9);
        input.put(1L, high);
        input.put(2L, low);
        input.put(3L, mid);

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        // Collect result keys in iteration order (HashMap — order not guaranteed),
        // so verify the three expected entries are present and throughput values are correct.
        assertEquals(3, result.size());
        assertEquals(low,  result.get(2L));
        assertEquals(mid,  result.get(3L));
        assertEquals(high, result.get(1L));
    }

    @Test
    void resultIsLimitedToThreeEntries() {
        Map<Long, AdditionalData> input = new HashMap<>();
        for (long i = 1; i <= 6; i++) {
            input.put(i, data(5, i * 0.1));
        }
        Map<Long, AdditionalData> result = dataProcess.addData(input);
        assertEquals(3, result.size());
    }

    @Test
    void limitKeepsLowestThroughputRatioEntries() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(5, 0.1));
        input.put(2L, data(5, 0.2));
        input.put(3L, data(5, 0.3));
        input.put(4L, data(5, 0.9));
        input.put(5L, data(5, 1.0));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
        assertTrue(result.containsKey(3L));
        assertFalse(result.containsKey(4L));
        assertFalse(result.containsKey(5L));
    }

    @Test
    void inputMapIsMutatedWithFallbackEntry() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(5, 0.5));

        dataProcess.addData(input);

        assertTrue(input.containsKey(-1L), "Input map should contain the fallback entry with key -1");
    }

    @Test
    void fallbackEntryIsNotInResult() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(5, 0.5));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertFalse(result.containsKey(-1L), "Fallback entry (key -1) must not appear in result");
    }

    @Test
    void preExistingFallbackKeyIsOverwrittenAndNotInResult() {
        // If the caller already has a -1L entry with a positive queueSize,
        // it should be overwritten with the sentinel fallback and filtered from the result.
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(-1L, data(10, 0.5));
        input.put(1L, data(5, 0.8));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertFalse(result.containsKey(-1L));
        assertEquals(1, result.size());
    }

    @Test
    void exactlyThreeValidEntriesAllReturned() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(3, 0.3));
        input.put(2L, data(3, 0.6));
        input.put(3L, data(3, 0.9));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
    }

    @Test
    void mixOfValidAndInvalidEntriesReturnsOnlyValid() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, data(5, 0.5));   // valid
        input.put(2L, data(0, 0.5));   // filtered: queueSize == 0
        input.put(3L, null);            // filtered: null value
        input.put(4L, data(-1, 0.5));  // filtered: queueSize < 0

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
    }
}
