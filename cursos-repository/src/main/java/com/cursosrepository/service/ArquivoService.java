package com.cursosrepository.service;

import com.cursosrepository.model.Arquivo;
import com.cursosrepository.repository.ArquivoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ArquivoService {

    private final ArquivoRepository repository;
    private final Path uploadDir;

    public ArquivoService(ArquivoRepository repository,
                          @Value("${app.upload.dir:uploads}") String uploadDir) throws IOException {
        this.repository = repository;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public List<Arquivo> listarPorModulo(Long moduloId) {
        return repository.findByModuloId(moduloId);
    }

    public List<Arquivo> listarPorCurso(Long cursoId) {
        return repository.findByCursoId(cursoId);
    }

    public Optional<Arquivo> buscarPorId(Long id) {
        return repository.findById(id);
    }

    /** Salva um arquivo vinculado a um módulo. */
    public Arquivo salvar(Long moduloId, MultipartFile file) throws IOException {
        Arquivo arquivo = gravarNoDisco(file);
        arquivo.setModuloId(moduloId);
        return repository.save(arquivo);
    }

    /** Salva um arquivo vinculado diretamente a um curso (material geral). */
    public Arquivo salvarParaCurso(Long cursoId, MultipartFile file) throws IOException {
        Arquivo arquivo = gravarNoDisco(file);
        arquivo.setCursoId(cursoId);
        return repository.save(arquivo);
    }

    /** Salva um arquivo sem vínculo com módulo ou curso (usado em submissões de atividades). */
    public Arquivo salvarAvulso(MultipartFile file) throws IOException {
        Arquivo arquivo = gravarNoDisco(file);
        return repository.save(arquivo);
    }

    private Arquivo gravarNoDisco(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String storedName = UUID.randomUUID() + extension;

        Path destino = uploadDir.resolve(storedName);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        Arquivo arquivo = new Arquivo();
        arquivo.setNome(originalName);
        arquivo.setCaminho(storedName);
        arquivo.setTipo(detectarTipo(extension));
        arquivo.setTamanho(file.getSize());
        return arquivo;
    }

    public Path resolverCaminho(String storedName) {
        return uploadDir.resolve(storedName).normalize();
    }

    public void excluir(Long id) throws IOException {
        repository.findById(id).ifPresent(a -> {
            try {
                Files.deleteIfExists(uploadDir.resolve(a.getCaminho()));
            } catch (IOException ignored) {}
            repository.deleteById(id);
        });
    }

    private Arquivo.TipoArquivo detectarTipo(String ext) {
        return switch (ext.toLowerCase()) {
            case ".pdf" -> Arquivo.TipoArquivo.PDF;
            case ".mp4", ".avi", ".mov", ".mkv", ".webm" -> Arquivo.TipoArquivo.VIDEO;
            case ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg" -> Arquivo.TipoArquivo.IMAGEM;
            default -> Arquivo.TipoArquivo.OUTRO;
        };
    }
}
