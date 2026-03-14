package io.crunch;

import lombok.Value;

/**
 * Immutable container for queue-related metrics and calculated ratios.
 */
@Value
public class AdditionalData {

    /** Expected wait time in seconds. */
    int expectedWaitTimeSeconds;

    /** Number of items currently in the queue. */
    int queueSize;

    /** Total number of available slots. */
    int totalSlots;

    /** Current throughput ratio. */
    double throughputRatio;

    /** Current offset ratio. */
    double offsetRatio;

    /** Maximum wait time in seconds. */
    int maxWaitTimeSeconds;
}
