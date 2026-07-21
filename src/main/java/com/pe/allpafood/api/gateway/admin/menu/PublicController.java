package com.pe.allpafood.api.gateway.admin.menu;

import com.pe.allpafood.api.transaction.catalog.bussiness.IMenusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/public/menu")
@RequiredArgsConstructor
@Slf4j
public class PublicController {
    private final IMenusService menuService;
    @GetMapping("/image/{fileName}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String fileName) {
        log.debug("getImage {}",fileName);
        FileSystemResource fileBytes = menuService.getMenuImage(fileName);

        MediaType mediaType;
        try {
            Path path = fileBytes.getFile().toPath();
            String mimeType = Files.probeContentType(path);

            // Solo aceptar tipos MIME que empiecen con "image/"
            if (mimeType != null && mimeType.startsWith("image/")) {
                mediaType = MediaType.parseMediaType(mimeType);
            } else {
                log.warn("Tipo MIME no permitido: {}", mimeType);
                return ResponseEntity
                        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .build();
            }
        } catch (IOException e) {
            log.error("Error al determinar el tipo MIME del archivo: {}", fileName, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(fileBytes);
    }
}
