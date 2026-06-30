package com.diabecare.infrastructure.pdf;

import com.diabecare.domain.model.ReportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfReportAdapter")
class PdfReportAdapterTest {

    @Mock
    private MedicalReportPdfGenerator generator;

    private PdfReportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PdfReportAdapter(generator);
    }

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("delega la generación al generador y retorna sus bytes")
        void delegatesGenerationToGeneratorAndReturnsItsBytes() {
            ReportData data = mock(ReportData.class);
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 7);
            byte[] expectedBytes = new byte[]{1, 2, 3};

            when(generator.generate(data, from, to)).thenReturn(expectedBytes);

            byte[] result = adapter.generate(data, from, to);

            assertThat(result).isEqualTo(expectedBytes);
            verify(generator).generate(data, from, to);
        }
    }
}