package io.crunch;

import java.util.Objects;

public class PrepareAdditionalDataForResult {

    public AdditionalData prepareAdditionalData(AdditionalData additionalData) {
        return Objects.requireNonNullElseGet(
            additionalData,
            () ->
                new AdditionalData(
                    QueueDataDefaults.NO_EXPECTED_WAIT_TIME,
                    QueueDataDefaults.NO_QUEUE_SIZE,
                    QueueDataDefaults.NO_TOTAL_SLOTS,
                    QueueDataDefaults.NO_THROUGHPUT,
                    QueueDataDefaults.NO_OFFSET,
                    QueueDataDefaults.NO_MAX_WAIT_TIME));
    }
}
