package com.cursosrepository.controller;

import com.cursosrepository.model.Arquivo;
import com.cursosrepository.service.ArquivoService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Path;

@Controller
@RequestMapping("/arquivos")
public class ArquivoDownloadController {

    private final ArquivoService arquivoService;

    public ArquivoDownloadController(ArquivoService arquivoService) {
        this.arquivoService = arquivoService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {
        Arquivo arquivo = arquivoService.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Arquivo não encontrado: " + id));

        Path path = arquivoService.resolverCaminho(arquivo.getCaminho());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + arquivo.getNome() + "\"")
            .body(resource);
    }
}
