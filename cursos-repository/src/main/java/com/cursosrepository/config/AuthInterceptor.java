package com.cursosrepository.config;

import com.cursosrepository.model.UsuarioSessao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege as rotas dos portais: /instrutor/** exige sessão de instrutor,
 * /aluno/** exige sessão de aluno e /forum/** e /arquivos/** exigem
 * qualquer usuário logado.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        UsuarioSessao usuario = session != null
            ? (UsuarioSessao) session.getAttribute("usuario")
            : null;

        String path = request.getRequestURI();

        if (path.startsWith("/instrutor")) {
            if (usuario == null || !usuario.isInstrutor()) {
                response.sendRedirect("/instrutor/login");
                return false;
            }
        } else if (path.startsWith("/aluno")) {
            if (usuario == null || !usuario.isAluno()) {
                response.sendRedirect("/aluno/login");
                return false;
            }
        } else if (usuario == null) {
            response.sendRedirect("/");
            return false;
        }
        return true;
    }
}
