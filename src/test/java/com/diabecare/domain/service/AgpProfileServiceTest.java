package com.diabecare.domain.service;

import com.diabecare.domain.model.AgpHourlyBucket;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AgpProfileService")
class AgpProfileServiceTest {

    private final AgpProfileService service = new AgpProfileService();
    private final UUID patientId = UUID.randomUUID();

    @Test
    @DisplayName("retorna 24 buckets, uno por cada hora del día, incluso sin lecturas")
    void returns24BucketsOneForEachHourEvenWithoutReadings() {
        List<AgpHourlyBucket> buckets = service.buildHourlyProfile(List.of());

        assertThat(buckets).hasSize(24);
        assertThat(buckets).allSatisfy(b -> assertThat(b.getReadingCount()).isZero());
    }

    @Test
    @DisplayName("agrupa las lecturas por hora del día sin importar la fecha")
    void groupsReadingsByHourOfDayRegardlessOfDate() {
        GlucoseReading day1 = readingAt(LocalDateTime.of(2026, 1, 1, 8, 0), 100);
        GlucoseReading day2 = readingAt(LocalDateTime.of(2026, 1, 15, 8, 30), 120);

        List<AgpHourlyBucket> buckets = service.buildHourlyProfile(List.of(day1, day2));

        AgpHourlyBucket hour8 = buckets.stream().filter(b -> b.getHour() == 8).findFirst().orElseThrow();
        assertThat(hour8.getReadingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("calcula la mediana correctamente para un número impar de lecturas")
    void calculatesMedianCorrectlyForOddNumberOfReadings() {
        List<GlucoseReading> readings = List.of(
                readingAt(LocalDateTime.of(2026, 1, 1, 8, 0), 90),
                readingAt(LocalDateTime.of(2026, 1, 2, 8, 0), 100),
                readingAt(LocalDateTime.of(2026, 1, 3, 8, 0), 110));

        List<AgpHourlyBucket> buckets = service.buildHourlyProfile(readings);
        AgpHourlyBucket hour8 = buckets.stream().filter(b -> b.getHour() == 8).findFirst().orElseThrow();

        assertThat(hour8.getMedian()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("los percentiles son monótonos (p10 <= p25 <= mediana <= p75 <= p90)")
    void percentilesAreMonotonic() {
        List<GlucoseReading> readings = List.of(
                readingAt(LocalDateTime.of(2026, 1, 1, 8, 0), 70),
                readingAt(LocalDateTime.of(2026, 1, 2, 8, 0), 90),
                readingAt(LocalDateTime.of(2026, 1, 3, 8, 0), 110),
                readingAt(LocalDateTime.of(2026, 1, 4, 8, 0), 130),
                readingAt(LocalDateTime.of(2026, 1, 5, 8, 0), 200));

        List<AgpHourlyBucket> buckets = service.buildHourlyProfile(readings);
        AgpHourlyBucket hour8 = buckets.stream().filter(b -> b.getHour() == 8).findFirst().orElseThrow();

        assertThat(hour8.getP10()).isLessThanOrEqualTo(hour8.getP25());
        assertThat(hour8.getP25()).isLessThanOrEqualTo(hour8.getMedian());
        assertThat(hour8.getMedian()).isLessThanOrEqualTo(hour8.getP75());
        assertThat(hour8.getP75()).isLessThanOrEqualTo(hour8.getP90());
    }

    @Test
    @DisplayName("retorna percentiles nulos para una hora sin ninguna lectura")
    void returnsNullPercentilesForAnHourWithNoReadings() {
        GlucoseReading reading = readingAt(LocalDateTime.of(2026, 1, 1, 8, 0), 100);

        List<AgpHourlyBucket> buckets = service.buildHourlyProfile(List.of(reading));
        AgpHourlyBucket hour3 = buckets.stream().filter(b -> b.getHour() == 3).findFirst().orElseThrow();

        assertThat(hour3.getMedian()).isNull();
        assertThat(hour3.getReadingCount()).isZero();
    }

    private GlucoseReading readingAt(LocalDateTime measuredAt, int value) {
        return GlucoseReading.create(
                patientId, BigDecimal.valueOf(value), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, measuredAt, null, null);
    }
}
