CREATE DATABASE IF NOT EXISTS cursosdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cursosdb;

CREATE TABLE IF NOT EXISTS instrutor (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    senha      VARCHAR(255),
    bio        TEXT,
    criado_em  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS curso (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo         VARCHAR(255) NOT NULL,
    descricao      TEXT,
    categoria      VARCHAR(100),
    carga_horaria  INT,
    instrutor_id   BIGINT,
    criado_em      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_curso_instrutor FOREIGN KEY (instrutor_id) REFERENCES instrutor(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS modulo (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    curso_id   BIGINT NOT NULL,
    titulo     VARCHAR(255) NOT NULL,
    descricao  TEXT,
    ordem      INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_modulo_curso FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS aluno (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    senha      VARCHAR(255),
    criado_em  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS matricula (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    aluno_id      BIGINT NOT NULL,
    curso_id      BIGINT NOT NULL,
    matriculado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mat_aluno FOREIGN KEY (aluno_id) REFERENCES aluno(id) ON DELETE CASCADE,
    CONSTRAINT fk_mat_curso FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE,
    UNIQUE KEY uq_matricula (aluno_id, curso_id)
);

-- Um arquivo pertence a um modulo OU diretamente a um curso (material geral)
CREATE TABLE IF NOT EXISTS arquivo (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    modulo_id   BIGINT,
    curso_id    BIGINT,
    nome        VARCHAR(255) NOT NULL,
    caminho     VARCHAR(500) NOT NULL,
    tipo        ENUM('PDF','VIDEO','IMAGEM','OUTRO') NOT NULL DEFAULT 'OUTRO',
    tamanho     BIGINT,
    enviado_em  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_arquivo_modulo FOREIGN KEY (modulo_id) REFERENCES modulo(id) ON DELETE CASCADE,
    CONSTRAINT fk_arquivo_curso  FOREIGN KEY (curso_id)  REFERENCES curso(id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS atividade (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    modulo_id   BIGINT NOT NULL,
    titulo      VARCHAR(255) NOT NULL,
    descricao   TEXT,
    prazo       DATE,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ativ_modulo FOREIGN KEY (modulo_id) REFERENCES modulo(id) ON DELETE CASCADE
);

-- Resposta de um aluno a uma atividade (trabalho enviado)
CREATE TABLE IF NOT EXISTS submissao (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    atividade_id BIGINT NOT NULL,
    aluno_id     BIGINT NOT NULL,
    arquivo_id   BIGINT,
    comentario   TEXT,
    enviado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_atividade FOREIGN KEY (atividade_id) REFERENCES atividade(id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_aluno     FOREIGN KEY (aluno_id)     REFERENCES aluno(id)     ON DELETE CASCADE,
    CONSTRAINT fk_sub_arquivo   FOREIGN KEY (arquivo_id)   REFERENCES arquivo(id)   ON DELETE SET NULL,
    UNIQUE KEY uq_submissao (atividade_id, aluno_id)
);

CREATE TABLE IF NOT EXISTS forum_topico (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    curso_id    BIGINT NOT NULL,
    autor_id    BIGINT,
    autor_tipo  VARCHAR(20) NOT NULL DEFAULT 'ALUNO',
    autor_nome  VARCHAR(150) NOT NULL DEFAULT 'Anônimo',
    titulo      VARCHAR(255) NOT NULL,
    conteudo    TEXT NOT NULL,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topico_curso FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS forum_resposta (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    topico_id   BIGINT NOT NULL,
    autor_id    BIGINT,
    autor_tipo  VARCHAR(20) NOT NULL DEFAULT 'ALUNO',
    autor_nome  VARCHAR(150) NOT NULL DEFAULT 'Anônimo',
    conteudo    TEXT NOT NULL,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resp_topico FOREIGN KEY (topico_id) REFERENCES forum_topico(id) ON DELETE CASCADE
);

-- ============================================================
-- Migracoes para bancos criados na versao anterior do schema.
-- Falham silenciosamente (continue-on-error) quando ja aplicadas.
-- ============================================================

ALTER TABLE instrutor ADD COLUMN senha VARCHAR(255);
ALTER TABLE aluno ADD COLUMN senha VARCHAR(255);

ALTER TABLE forum_topico DROP FOREIGN KEY fk_topico_aluno;
ALTER TABLE forum_topico CHANGE aluno_id autor_id BIGINT;
ALTER TABLE forum_topico ADD COLUMN autor_tipo VARCHAR(20) NOT NULL DEFAULT 'ALUNO';
ALTER TABLE forum_topico ADD COLUMN autor_nome VARCHAR(150) NOT NULL DEFAULT 'Anônimo';
UPDATE forum_topico ft LEFT JOIN aluno a ON ft.autor_id = a.id SET ft.autor_nome = COALESCE(a.nome, 'Anônimo') WHERE ft.autor_nome = 'Anônimo';

ALTER TABLE forum_resposta DROP FOREIGN KEY fk_resp_aluno;
ALTER TABLE forum_resposta CHANGE aluno_id autor_id BIGINT;
ALTER TABLE forum_resposta ADD COLUMN autor_tipo VARCHAR(20) NOT NULL DEFAULT 'ALUNO';
ALTER TABLE forum_resposta ADD COLUMN autor_nome VARCHAR(150) NOT NULL DEFAULT 'Anônimo';
UPDATE forum_resposta fr LEFT JOIN aluno a ON fr.autor_id = a.id SET fr.autor_nome = COALESCE(a.nome, 'Anônimo') WHERE fr.autor_nome = 'Anônimo';

-- Materiais em nivel de curso
ALTER TABLE arquivo MODIFY modulo_id BIGINT NULL;
ALTER TABLE arquivo ADD COLUMN curso_id BIGINT;
ALTER TABLE arquivo ADD CONSTRAINT fk_arquivo_curso FOREIGN KEY (curso_id) REFERENCES curso(id) ON DELETE CASCADE;
