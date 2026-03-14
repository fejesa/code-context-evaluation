package io.crunch;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataProcess {

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
                        HashMap::new
                ));
    }
}
