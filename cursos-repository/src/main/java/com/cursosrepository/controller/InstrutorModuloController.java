package com.cursosrepository.controller;

import com.cursosrepository.model.Atividade;
import com.cursosrepository.model.Curso;
import com.cursosrepository.model.Modulo;
import com.cursosrepository.model.Submissao;
import com.cursosrepository.model.UsuarioSessao;
import com.cursosrepository.repository.AtividadeRepository;
import com.cursosrepository.repository.CursoRepository;
import com.cursosrepository.repository.ModuloRepository;
import com.cursosrepository.repository.SubmissaoRepository;
import com.cursosrepository.service.ArquivoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/instrutor/modulos")
public class InstrutorModuloController {

    private final ModuloRepository moduloRepo;
    private final CursoRepository cursoRepo;
    private final ArquivoService arquivoService;
    private final AtividadeRepository atividadeRepo;
    private final SubmissaoRepository submissaoRepo;

    public InstrutorModuloController(ModuloRepository moduloRepo, CursoRepository cursoRepo,
                                     ArquivoService arquivoService, AtividadeRepository atividadeRepo,
                                     SubmissaoRepository submissaoRepo) {
        this.moduloRepo = moduloRepo;
        this.cursoRepo = cursoRepo;
        this.arquivoService = arquivoService;
        this.atividadeRepo = atividadeRepo;
        this.submissaoRepo = submissaoRepo;
    }

    private UsuarioSessao usuario(HttpSession session) {
        return (UsuarioSessao) session.getAttribute("usuario");
    }

    private Modulo moduloDoInstrutor(Long moduloId, UsuarioSessao usuario) {
        Modulo modulo = moduloRepo.findById(moduloId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Módulo não encontrado"));
        if (!usuario.getId().equals(modulo.getCursoInstrutorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este módulo pertence a outro instrutor");
        }
        return modulo;
    }

    private Curso cursoDoInstrutor(Long cursoId, UsuarioSessao usuario) {
        Curso curso = cursoRepo.findById(cursoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado"));
        if (!usuario.getId().equals(curso.getInstrutorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este curso pertence a outro instrutor");
        }
        return curso;
    }

    @GetMapping("/novo")
    public String novoForm(@RequestParam Long cursoId, HttpSession session, Model model) {
        Curso curso = cursoDoInstrutor(cursoId, usuario(session));
        Modulo modulo = new Modulo();
        modulo.setCursoId(cursoId);
        modulo.setOrdem(moduloRepo.findByCursoId(cursoId).size() + 1);
        model.addAttribute("modulo", modulo);
        model.addAttribute("cursoTitulo", curso.getTitulo());
        return "instrutor/modulo-form";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, HttpSession session, Model model) {
        Modulo modulo = moduloDoInstrutor(id, usuario(session));
        List<Atividade> atividades = atividadeRepo.findByModuloId(id);
        Map<Long, Integer> contagemSubmissoes = new HashMap<>();
        for (Atividade at : atividades) {
            contagemSubmissoes.put(at.getId(), submissaoRepo.countByAtividadeId(at.getId()));
        }
        model.addAttribute("modulo", modulo);
        model.addAttribute("arquivos", arquivoService.listarPorModulo(id));
        model.addAttribute("atividades", atividades);
        model.addAttribute("contagemSubmissoes", contagemSubmissoes);
        model.addAttribute("novaAtividade", new Atividade());
        return "instrutor/modulo-view";
    }

    @GetMapping("/{moduloId}/atividades/{atividadeId}/submissoes")
    public String verSubmissoes(@PathVariable Long moduloId, @PathVariable Long atividadeId,
                                HttpSession session, Model model) {
        Modulo modulo = moduloDoInstrutor(moduloId, usuario(session));
        Atividade atividade = atividadeRepo.findById(atividadeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada"));
        model.addAttribute("modulo", modulo);
        model.addAttribute("atividade", atividade);
        model.addAttribute("submissoes", submissaoRepo.findByAtividadeId(atividadeId));
        return "instrutor/submissoes";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model) {
        Modulo modulo = moduloDoInstrutor(id, usuario(session));
        model.addAttribute("modulo", modulo);
        model.addAttribute("cursoTitulo", modulo.getCursoTitulo());
        return "instrutor/modulo-form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Modulo modulo, BindingResult result,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        UsuarioSessao usuario = usuario(session);
        Curso curso = cursoDoInstrutor(modulo.getCursoId(), usuario);
        if (result.hasErrors()) {
            model.addAttribute("cursoTitulo", curso.getTitulo());
            return "instrutor/modulo-form";
        }
        if (modulo.getId() != null) {
            moduloDoInstrutor(modulo.getId(), usuario);
        }
        moduloRepo.save(modulo);
        redirect.addFlashAttribute("sucesso", "Módulo salvo com sucesso!");
        return "redirect:/instrutor/cursos/" + modulo.getCursoId();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, HttpSession session,
                          RedirectAttributes redirect) {
        Modulo modulo = moduloDoInstrutor(id, usuario(session));
        moduloRepo.deleteById(id);
        redirect.addFlashAttribute("sucesso", "Módulo excluído.");
        return "redirect:/instrutor/cursos/" + modulo.getCursoId();
    }

    @PostMapping("/{id}/upload")
    public String upload(@PathVariable Long id, @RequestParam("arquivo") MultipartFile file,
                         HttpSession session, RedirectAttributes redirect) throws IOException {
        moduloDoInstrutor(id, usuario(session));
        if (!file.isEmpty()) {
            arquivoService.salvar(id, file);
            redirect.addFlashAttribute("sucesso", "Arquivo enviado com sucesso!");
        }
        return "redirect:/instrutor/modulos/" + id;
    }

    @PostMapping("/{moduloId}/arquivos/{arquivoId}/excluir")
    public String excluirArquivo(@PathVariable Long moduloId, @PathVariable Long arquivoId,
                                 HttpSession session, RedirectAttributes redirect) throws IOException {
        moduloDoInstrutor(moduloId, usuario(session));
        arquivoService.excluir(arquivoId);
        redirect.addFlashAttribute("sucesso", "Arquivo excluído.");
        return "redirect:/instrutor/modulos/" + moduloId;
    }

    @PostMapping("/{moduloId}/atividades/salvar")
    public String salvarAtividade(@PathVariable Long moduloId,
                                  @Valid @ModelAttribute("novaAtividade") Atividade atividade,
                                  BindingResult result, HttpSession session,
                                  RedirectAttributes redirect, Model model) {
        Modulo modulo = moduloDoInstrutor(moduloId, usuario(session));
        if (result.hasErrors()) {
            List<Atividade> atividades = atividadeRepo.findByModuloId(moduloId);
            Map<Long, Integer> contagemSubmissoes = new HashMap<>();
            for (Atividade at : atividades) {
                contagemSubmissoes.put(at.getId(), submissaoRepo.countByAtividadeId(at.getId()));
            }
            model.addAttribute("modulo", modulo);
            model.addAttribute("arquivos", arquivoService.listarPorModulo(moduloId));
            model.addAttribute("atividades", atividades);
            model.addAttribute("contagemSubmissoes", contagemSubmissoes);
            return "instrutor/modulo-view";
        }
        atividade.setModuloId(moduloId);
        atividadeRepo.save(atividade);
        redirect.addFlashAttribute("sucesso", "Atividade criada!");
        return "redirect:/instrutor/modulos/" + moduloId;
    }

    @PostMapping("/{moduloId}/atividades/{atividadeId}/excluir")
    public String excluirAtividade(@PathVariable Long moduloId, @PathVariable Long atividadeId,
                                   HttpSession session, RedirectAttributes redirect) throws IOException {
        moduloDoInstrutor(moduloId, usuario(session));
        // Remove os arquivos enviados pelos alunos antes de excluir a atividade
        for (Submissao s : submissaoRepo.findByAtividadeId(atividadeId)) {
            if (s.getArquivoId() != null) arquivoService.excluir(s.getArquivoId());
        }
        atividadeRepo.deleteById(atividadeId);
        redirect.addFlashAttribute("sucesso", "Atividade excluída.");
        return "redirect:/instrutor/modulos/" + moduloId;
    }
}
