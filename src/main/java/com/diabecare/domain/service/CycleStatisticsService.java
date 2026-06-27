package com.diabecare.domain.service;

import com.diabecare.domain.model.MenstrualCycle;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;

public class CycleStatisticsService {

    private static final int MIN_VALID_CYCLE_LENGTH = 21;
    private static final int MAX_VALID_CYCLE_LENGTH = 45;

    public Integer calculateAverageCycleLength(List<MenstrualCycle> history) {
        if (history.size() < 2) return null;

        OptionalDouble avg = java.util.stream.IntStream.range(0, history.size() - 1)
                .mapToObj(i -> ChronoUnit.DAYS.between(
                        history.get(i + 1).getStartDate(),
                        history.get(i).getStartDate()))
                .filter(days -> days >= MIN_VALID_CYCLE_LENGTH && days <= MAX_VALID_CYCLE_LENGTH)
                .mapToDouble(Long::doubleValue)
                .average();

        return avg.isPresent() ? (int) Math.round(avg.getAsDouble()) : null;
    }

    public Integer calculateAveragePeriodLength(List<MenstrualCycle> history) {
        List<Integer> completedLengths = history.stream()
                .map(MenstrualCycle::getActualPeriodLengthDays)
                .filter(length -> length != null && length >= 1 && length <= 14)
                .toList();

        if (completedLengths.isEmpty()) return null;

        double avg = completedLengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return (int) Math.round(avg);
    }
}