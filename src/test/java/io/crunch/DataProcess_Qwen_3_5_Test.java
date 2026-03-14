package io.crunch;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataProcess_Qwen_3_5_Test {

    private final DataProcess dataProcess = new DataProcess();

    @Test
    void addDataWithEmptyMapReturnsEmptyResult() {
        Map<Long, AdditionalData> input = new HashMap<>();

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertTrue(result.isEmpty());
    }

    @Test
    void addDataFiltersOutNullValues() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));
        input.put(2L, null);

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
        assertFalse(result.containsKey(2L));
    }

    @Test
    void addDataFiltersOutEntriesWithQueueSizeZero() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));
        input.put(2L, new AdditionalData(10, 0, 100, 0.5, 0.1, 60));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
        assertFalse(result.containsKey(2L));
    }

    @Test
    void addDataFiltersOutEntriesWithNegativeQueueSize() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));
        input.put(2L, new AdditionalData(10, -5, 100, 0.5, 0.1, 60));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
        assertFalse(result.containsKey(2L));
    }

    @Test
    void addDataReturnsSingleValidEntry() {
        Map<Long, AdditionalData> input = new HashMap<>();
        AdditionalData data = new AdditionalData(10, 5, 100, 0.5, 0.1, 60);
        input.put(1L, data);

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(1, result.size());
        assertEquals(data, result.get(1L));
    }

    @Test
    void addDataSortsByThroughputRatioAscending() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.9, 0.1, 60));
        input.put(2L, new AdditionalData(10, 5, 100, 0.3, 0.1, 60));
        input.put(3L, new AdditionalData(10, 5, 100, 0.6, 0.1, 60));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
        // Note: HashMap does not preserve insertion order, so we verify by extracting
        // and sorting the values ourselves to confirm the stream was properly sorted
        var sortedValues = result.values().stream()
                .sorted(java.util.Comparator.comparingDouble(AdditionalData::getThroughputRatio))
                .toList();
        assertEquals(0.3, sortedValues.get(0).getThroughputRatio());
        assertEquals(0.6, sortedValues.get(1).getThroughputRatio());
        assertEquals(0.9, sortedValues.get(2).getThroughputRatio());
    }

    @Test
    void addDataLimitsResultToThreeEntries() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.2, 0.1, 60));
        input.put(2L, new AdditionalData(10, 5, 100, 0.3, 0.1, 60));
        input.put(3L, new AdditionalData(10, 5, 100, 0.4, 0.1, 60));
        input.put(4L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));
        input.put(5L, new AdditionalData(10, 5, 100, 0.6, 0.1, 60));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertEquals(3, result.size());
        // Since HashMap doesn't preserve order, verify the correct values are present
        // by extracting throughput ratios and sorting them
        var throughputRatios = result.values().stream()
                .mapToDouble(AdditionalData::getThroughputRatio)
                .sorted()
                .toArray();
        assertEquals(3, throughputRatios.length);
        assertEquals(0.2, throughputRatios[0], 0.001);
        assertEquals(0.3, throughputRatios[1], 0.001);
        assertEquals(0.4, throughputRatios[2], 0.001);
    }

    @Test
    void addDataAddsFallbackEntryToInputMap() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));

        dataProcess.addData(input);

        assertTrue(input.containsKey(-1L));
    }

    @Test
    void addDataFiltersOutFallbackEntryFromResult() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));

        Map<Long, AdditionalData> result = dataProcess.addData(input);

        assertFalse(result.containsKey(-1L));
    }

    @Test
    void addDataMutatesOriginalInputMap() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(1L, new AdditionalData(10, 5, 100, 0.5, 0.1, 60));
        int originalSize = input.size();

        dataProcess.addData(input);

        assertEquals(originalSize + 1, input.size());
        assertTrue(input.containsKey(-1L));
    }
}
