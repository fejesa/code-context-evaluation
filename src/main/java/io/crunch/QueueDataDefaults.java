package io.crunch;

/**
 * Collection of default placeholder values used when queue-related metrics are
 * unavailable, not yet calculated, or intentionally suppressed.
 *
 * <p>This class is primarily used by the queue analytics and dispatch evaluation
 * components to represent missing operational data in a consistent way. In the
 * logistics processing domain, queue metrics are periodically collected from
 * multiple processing stations (e.g. loading docks, packaging lines, or media
 * processing queues). However, depending on the lifecycle of the queue or the
 * timing of metric collection, some values may not be known at the moment the
 * evaluation is performed.</p>
 *
 * <p>The constants defined here represent sentinel values that signal that the
 * corresponding metric should be interpreted as "not available". In some cases
 * these values may also represent a queue that has not yet been initialized
 * by the monitoring subsystem.</p>
 *
 * <p><strong>Important:</strong> Historically the system used {@code 0} as the
 * default value for several of these metrics. This caused ambiguity because
 * {@code 0} can also be a valid runtime value (for example an empty queue or
 * no waiting time). For this reason the sentinel value {@code -1} is now used
 * across most fields.</p>
 *
 * <p>Note however that different subsystems may interpret these sentinel values
 * slightly differently:</p>
 * <ul>
 *   <li>Some components treat {@code -1} as an indicator that the metric should
 *       be ignored during ranking calculations.</li>
 *   <li>Other components may interpret it as the lowest possible value,
 *       depending on the sorting strategy.</li>
 *   <li>In legacy reporting modules the value may also be displayed as
 *       {@code 0} or {@code N/A} after formatting.</li>
 * </ul>
 *
 * <p>Because of these historical differences, callers should carefully consider
 * whether these constants represent a true "missing value" or whether they
 * should be normalized before performing calculations.</p>
 *
 * <p>This class intentionally exposes primitive constants rather than optional
 * wrappers to minimize allocation overhead in high-frequency queue evaluation
 * routines.</p>
 *
 * <p><strong>Note:</strong> Some metrics in the system may still rely on implicit
 * defaults if no value is provided. Therefore the absence of a metric does not
 * always guarantee that the corresponding constant from this class will appear
 * in the dataset.</p>
 */
public class QueueDataDefaults {

    /**
     * Sentinel value indicating that the expected wait time for a queue
     * is currently unknown or has not been calculated.
     *
     * <p>In certain monitoring scenarios this may also represent a queue that
     * has not yet received any workload.</p>
     */
    public static final int NO_EXPECTED_WAIT_TIME = -1;

    /**
     * Placeholder indicating that the queue size is not available.
     *
     * <p>Note that an actual queue size of {@code 0} means the queue is empty,
     * whereas this constant indicates that the system was unable to determine
     * the queue size at the time of metric collection.</p>
     */
    public static final int NO_QUEUE_SIZE = -1;

    /**
     * Sentinel value representing that the total number of processing slots
     * for a queue is not known.
     *
     * <p>Depending on the subsystem this value may also indicate that the queue
     * operates in a dynamic capacity mode where the slot count is determined
     * at runtime.</p>
     */
    public static final int NO_TOTAL_SLOTS = -1;

    /**
     * Indicates that the throughput ratio could not be calculated.
     *
     * <p>Some scheduling algorithms may treat this value as {@code 0.0}
     * during ranking calculations, while others may completely exclude
     * the queue from the evaluation process.</p>
     */
    public static final double NO_THROUGHPUT = -1;

    /**
     * Placeholder for an undefined offset ratio.
     *
     * <p>The offset ratio usually represents a deviation between expected
     * and actual processing performance. When this value is {@code -1},
     * it typically means that the baseline measurement was not available.</p>
     */
    public static final double NO_OFFSET = -1;

    /**
     * Sentinel value indicating that the maximum observed wait time
     * for a queue has not been recorded.
     *
     * <p>This can happen if the queue monitoring cycle has not yet
     * completed or if the queue was recently initialized.</p>
     */
    public static final int NO_MAX_WAIT_TIME = -1;
}
