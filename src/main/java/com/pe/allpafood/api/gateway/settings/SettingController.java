package com.pe.allpafood.api.gateway.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.allpafood.api.transaction.setting.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {
    private final SettingRepository settingRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/{name}")
    public ResponseEntity<List<Map<String, Object>>> getByName(
            @PathVariable String name
    ) throws JsonProcessingException {

        String result = settingRepository.findByName(name);

        if (result == null || result.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> settings = objectMapper.readValue(
                result,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        return ResponseEntity.ok(settings);
    }
}
