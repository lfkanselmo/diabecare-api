package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.SearchFoodUseCase;
import com.diabecare.presentation.dto.response.FoodResponse;
import com.diabecare.presentation.mapper.FoodPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
public class FoodController {

    private final SearchFoodUseCase searchFoodUseCase;
    private final FoodPresentationMapper mapper;

    @GetMapping("/search")
    public ResponseEntity<List<FoodResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(
                searchFoodUseCase.search(query)
                        .stream().map(mapper::toResponse).toList()
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FoodResponse>> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(
                searchFoodUseCase.findByCategory(category)
                        .stream().map(mapper::toResponse).toList()
        );
    }
}