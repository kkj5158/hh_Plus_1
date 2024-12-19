package io.hhplus.tdd.point.record;

import lombok.ToString;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
