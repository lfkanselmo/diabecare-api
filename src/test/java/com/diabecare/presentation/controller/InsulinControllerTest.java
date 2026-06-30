package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.CalculateInsulinDoseUseCase;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InsulinController")
class InsulinControllerTest {

    @Mock
    private CalculateInsulinDoseUseCase calculateInsulinDoseUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        InsulinController controller = new InsulinController(calculateInsulinDoseUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/insulin/{patientId}/calculate")
    class Calculate {

        @Test
        @DisplayName("retorna 200 con el resultado del cálculo correctamente mapeado")
        void returns200WithCalculationResultMappedCorrectly() throws Exception {
            var result = new CalculateInsulinDoseUseCase.Result(
                    BigDecimal.valueOf(2.6), BigDecimal.valueOf(6), BigDecimal.valueOf(8.6),
                    "Corrección + dosis de comida");

            when(calculateInsulinDoseUseCase.calculate(any())).thenReturn(result);

            String body = """
                    {"currentGlucose": 250, "carbsToEat": 60, "beforeMeal": true}
                    """;

            mockMvc.perform(post("/api/v1/insulin/{patientId}/calculate", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.correctionDose").value(2.6))
                    .andExpect(jsonPath("$.mealDose").value(6))
                    .andExpect(jsonPath("$.totalDose").value(8.6));
        }

        @Test
        @DisplayName("retorna 400 cuando currentGlucose es menor a 20")
        void returns400WhenCurrentGlucoseBelowMinimum() throws Exception {
            String body = """
                    {"currentGlucose": 10, "carbsToEat": 60, "beforeMeal": true}
                    """;

            mockMvc.perform(post("/api/v1/insulin/{patientId}/calculate", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(calculateInsulinDoseUseCase);
        }

        @Test
        @DisplayName("retorna 400 cuando currentGlucose no se envía")
        void returns400WhenCurrentGlucoseMissing() throws Exception {
            String body = """
                    {"carbsToEat": 60, "beforeMeal": true}
                    """;

            mockMvc.perform(post("/api/v1/insulin/{patientId}/calculate", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}