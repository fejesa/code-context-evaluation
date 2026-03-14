package io.crunch;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

public class DataProcessTest {

    @Test
    public void testAddDataFiltersAndLimits() {
        // Prepare input map with several entries
        Map<Long, AdditionalData> input = new HashMap<>();
        // entry that should be included (queueSize > 0)
        input.put(1L, new AdditionalData(10, 5, 2, 0.5, 0.0, 20));
        // another includable entry with lower throughputRatio
        input.put(2L, new AdditionalData(5, 10, 3, 0.2, 0.0, 15));
        // includable entry with higher throughputRatio
        input.put(3L, new AdditionalData(8, 3, 1, 0.8, 0.0, 25));
        // entry that should be filtered out (queueSize <= 0)
        input.put(4L, new AdditionalData(12, 0, 2, 0.1, 0.0, 30));
        // includable entry, third lowest throughputRatio
        input.put(5L, new AdditionalData(7, 7, 2, 0.3, 0.0, 18));

        DataProcess processor = new DataProcess();
        Map<Long, AdditionalData> result = processor.addData(input);

        // Expect only three entries (limit 3) with queueSize > 0
        assertEquals(3, result.size(), "Result should contain exactly three entries");
        // Expected keys after sorting by throughputRatio: 2,5,1
        assertTrue(result.containsKey(2L), "Key 2 should be present");
        assertTrue(result.containsKey(5L), "Key 5 should be present");
        assertTrue(result.containsKey(1L), "Key 1 should be present");
        // Ensure filtered entry is absent
        assertFalse(result.containsKey(4L), "Key 4 should be filtered out due to queueSize <= 0");
        // Verify ordering indirectly via throughputRatio values
        assertTrue(result.get(2L).getThroughputRatio() <= result.get(5L).getThroughputRatio(), "Throughput ratio should be ordered");
        assertTrue(result.get(5L).getThroughputRatio() <= result.get(1L).getThroughputRatio(), "Throughput ratio should be ordered");
    }

    @Test
    public void testAddDataWithEmptyMap() {
        Map<Long, AdditionalData> input = new HashMap<>();
        DataProcess processor = new DataProcess();
        Map<Long, AdditionalData> result = processor.addData(input);
        // No entries have queueSize > 0, so result should be empty
        assertTrue(result.isEmpty(), "Result should be empty for empty input");
        // The original map should now contain the sentinel -1 entry
        assertTrue(input.containsKey(-1L), "Original map should contain the -1 sentinel entry after processing");
    }

    @Test
    public void testOriginalMapMutation() {
        Map<Long, AdditionalData> input = new HashMap<>();
        input.put(10L, new AdditionalData(3, 2, 1, 0.4, 0.0, 10));
        DataProcess processor = new DataProcess();
        processor.addData(input);
        // Verify the sentinel entry was added
        assertTrue(input.containsKey(-1L), "Sentinel entry -1 should be added to the original map");
        // Verify the sentinel's queueSize is the default (NO_QUEUE_SIZE = -1)
        AdditionalData sentinel = input.get(-1L);
        assertEquals(QueueDataDefaults.NO_QUEUE_SIZE, sentinel.getQueueSize(), "Sentinel should have default queue size");
    }
}
