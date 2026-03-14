package io.crunch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataProcess {

    /**
     * Selects the most relevant processing queues for the next dispatch cycle.
     *
     * <p>This method is part of the warehouse dispatch optimization logic. In the logistics
     * domain, multiple processing queues represent loading stations where packages are
     * prepared for shipment. Each queue is represented by an {@link AdditionalData} object
     * containing operational metrics such as queue size, estimated waiting time, and
     * throughput characteristics.</p>
     *
     * <p>The goal of this method is to determine which stations should be prioritized
     * during the next scheduling window. The decision is primarily based on the
     * {@code throughputRatio}, which expresses how efficiently a station is currently
     * processing items relative to its capacity.</p>
     *
     * <p>The algorithm performs the following high-level steps:</p>
     * <ol>
     *   <li>Ensures that a default "fallback" queue entry exists in the dataset.
     *       This entry represents a synthetic station used by the scheduling system
     *       whenever no suitable real station is available.</li>
     *   <li>Ignores queues that are currently inactive or empty.</li>
     *   <li>Ranks the remaining queues based on their throughput efficiency.</li>
     *   <li>Selects up to three candidate queues that should be considered by the
     *       dispatcher.</li>
     * </ol>
     *
     * <p>Queues with higher throughput ratios are generally considered more suitable
     * for immediate dispatch because they are expected to process packages faster
     * and reduce overall waiting times in the system.</p>
     *
     * <p>The resulting map preserves the ranking order so that downstream components
     * (such as the shipment scheduler) can iterate through the candidates in the
     * recommended priority sequence.</p>
     *
     * <p><b>Important:</b> The input map may be augmented with an internally generated
     * fallback entry representing the system queue. Callers should therefore not assume
     * that the original map instance remains unchanged after invocation.</p>
     *
     * @param data
     *     a map containing queue identifiers mapped to their corresponding
     *     {@link AdditionalData} metrics; the key typically represents the
     *     loading station identifier used by the warehouse control system
     *
     * @return
     *     a new {@link LinkedHashMap} containing at most three prioritized queues,
     *     ordered by their calculated processing efficiency
     */
    public Map<Long, AdditionalData> addData(Map<Long, AdditionalData> data) {
        var prepareAdditionalDataForResult = new PrepareAdditionalDataForResult();
        data.put(-1L, prepareAdditionalDataForResult.prepareAdditionalData(null));
        return data.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().getQueueSize() > 0)
                .sorted(Map.Entry.comparingByValue(
                        java.util.Comparator.comparingDouble(AdditionalData::getThroughputRatio)
                ))
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
