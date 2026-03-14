package io.crunch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataProcess_No_JavaDoc_Sonnet_4_6_Test {

    private DataProcess dataProcess;

    @BeforeEach
    void setUp() {
        dataProcess = new DataProcess();
    }

    // --- Sentinel entry behavior ---

    @Test
    void addData_alwaysAddsSentinelKeyToInputMap() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 2, 0.5, 0.0, 20));

        dataProcess.addData(input);

        assertTrue(input.containsKey(-1L), "Sentinel entry -1 must be added to the input map");
    }

    @Test
    void addData_sentinelHasAllDefaultValues() {
        Map<Long, AdditionalData> input = new HashMap<>();

        dataProcess.addData(input);

        AdditionalData sentinel = input.get(-1L);
        assertNotNull(sentinel);
        assertEquals(QueueDataDefaults.NO_QUEUE_SIZE, sentinel.getQueueSize());
        assertEquals(QueueDataDefaults.NO_EXPECTED_WAIT_TIME, sentinel.getExpectedWaitTimeSeconds());
        assertEquals(QueueDataDefaults.NO_TOTAL_SLOTS, sentinel.getTotalSlots());
        assertEquals(QueueDataDefaults.NO_THROUGHPUT, sentinel.getThroughputRatio());
        assertEquals(QueueDataDefaults.NO_OFFSET, sentinel.getOffsetRatio());
        assertEquals(QueueDataDefaults.NO_MAX_WAIT_TIME, sentinel.getMaxWaitTimeSeconds());
    }

    @Test
    void addData_overwritesExistingMinusOneKey() {
        AdditionalData original = new AdditionalData(99, 99, 99, 99.0, 99.0, 99);
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(-1L, original);

        dataProcess.addData(input);

        AdditionalData sentinel = input.get(-1L);
        assertEquals(QueueDataDefaults.NO_QUEUE_SIZE, sentinel.getQueueSize(),
                "Existing -1 entry must be overwritten with default values");
    }

    @Test
    void addData_sentinelIsNotIncludedInResult() {
        Map<Long, AdditionalData> input = new HashMap<>();
        // Only the sentinel will be in the map after processing (empty input)
        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertFalse(result.containsKey(-1L), "Sentinel entry must not appear in the result");
    }

    // --- Filtering behavior ---

    @Test
    void addData_emptyInputReturnsEmptyResult() {
        Map<Long, AdditionalData> result = dataProcess.addData(new HashMap<>());

        assertTrue(result.isEmpty());
    }

    @Test
    void addData_nullValuesAreFiltered() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, null);
        input.put(2L, new AdditionalData(5, 3, 2, 0.5, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertFalse(result.containsKey(1L), "Null value entries must be filtered out");
        assertTrue(result.containsKey(2L));
    }

    @Test
    void addData_zeroQueueSizeIsFiltered() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 0, 2, 0.5, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertTrue(result.isEmpty(), "Entry with queueSize == 0 must be filtered out");
    }

    @Test
    void addData_negativeQueueSizeIsFiltered() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, -3, 2, 0.5, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertTrue(result.isEmpty(), "Entry with queueSize < 0 must be filtered out");
    }

    @Test
    void addData_positiveQueueSizeIsIncluded() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 1, 2, 0.5, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertTrue(result.containsKey(1L), "Entry with queueSize > 0 must be included");
    }

    @Test
    void addData_allEntriesWithNonPositiveQueueSizeReturnsEmpty() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 0, 2, 0.1, 0.0, 10));
        input.put(2L, new AdditionalData(5, -1, 2, 0.2, 0.0, 10));
        input.put(3L, null);

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertTrue(result.isEmpty());
    }

    // --- Limit behavior ---

    @Test
    void addData_fewerThanThreeValidEntriesReturnsAll() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 2, 2, 0.3, 0.0, 10));
        input.put(2L, new AdditionalData(5, 4, 2, 0.6, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
    }

    @Test
    void addData_exactlyThreeValidEntriesReturnsAll() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 1, 2, 0.1, 0.0, 10));
        input.put(2L, new AdditionalData(5, 2, 2, 0.2, 0.0, 10));
        input.put(3L, new AdditionalData(5, 3, 2, 0.3, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
    }

    @Test
    void addData_moreThanThreeValidEntriesLimitsToThree() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 1, 2, 0.1, 0.0, 10));
        input.put(2L, new AdditionalData(5, 2, 2, 0.2, 0.0, 10));
        input.put(3L, new AdditionalData(5, 3, 2, 0.3, 0.0, 10));
        input.put(4L, new AdditionalData(5, 4, 2, 0.4, 0.0, 10));
        input.put(5L, new AdditionalData(5, 5, 2, 0.5, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
    }

    // --- Sorting / selection behavior ---

    @Test
    void addData_returnsThreeEntriesWithLowestThroughputRatio() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 1, 2, 0.1, 0.0, 10)); // lowest
        input.put(2L, new AdditionalData(5, 2, 2, 0.2, 0.0, 10)); // 2nd lowest
        input.put(3L, new AdditionalData(5, 3, 2, 0.3, 0.0, 10)); // 3rd lowest
        input.put(4L, new AdditionalData(5, 4, 2, 0.9, 0.0, 10)); // excluded
        input.put(5L, new AdditionalData(5, 5, 2, 0.8, 0.0, 10)); // excluded

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
        assertTrue(result.containsKey(3L));
        assertFalse(result.containsKey(4L), "Entry with higher throughputRatio must be excluded");
        assertFalse(result.containsKey(5L), "Entry with higher throughputRatio must be excluded");
    }

    @Test
    void addData_highThroughputRatioEntryExcludedWhenLimitReached() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 1, 2, 1.0, 0.0, 10)); // highest — cut off
        input.put(2L, new AdditionalData(5, 2, 2, 0.1, 0.0, 10));
        input.put(3L, new AdditionalData(5, 3, 2, 0.2, 0.0, 10));
        input.put(4L, new AdditionalData(5, 4, 2, 0.3, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
        assertFalse(result.containsKey(1L), "Entry with throughputRatio=1.0 must be cut off");
    }

    @Test
    void addData_singleValidEntryReturnsThatEntry() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(7L, new AdditionalData(5, 3, 2, 0.5, 0.0, 10));
        input.put(8L, new AdditionalData(5, 0, 2, 0.1, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(7L));
    }

    // --- Result map independence ---

    @Test
    void addData_returnsNewMapNotSameReferenceAsInput() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(5, 3, 2, 0.5, 0.0, 10));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertNotSame(input, result, "Result must be a new map, not the same reference as input");
    }
}
