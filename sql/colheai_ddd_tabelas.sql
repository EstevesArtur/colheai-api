-- ============================================================================
-- COLHEAI | SCRIPT DO BANCO DA API JAVA (tabelas e sequences)
-- Global Solution 2026/2 . CitizenTech . Domain Driven Design Using Java
-- ----------------------------------------------------------------------------
-- Since Young . Turma 1TDSPV-2026 . FIAP
--   Artur Bianchini Esteves               RM569450  (representante)
--   Joao Vitor Oliveira Fontenele Martins RM570783
-- ----------------------------------------------------------------------------
-- PARA QUE SERVE
--   Cria as 6 tabelas e as 6 sequences que a API do ColheAi usa, e carrega os
--   dados iniciais. A API liga a agricultura familiar a merenda escolar (PNAE):
--   chamadas publicas da EEx (Entidade Executora, a prefeitura), itens de cada
--   chamada, propostas dos agricultores e o painel da meta de 45%.
--
-- POR QUE UM SCRIPT PROPRIO (decisao D6 do grupo)
--   O modelo de dados e o mesmo da disciplina de Banco de Dados (mesmas
--   colunas, mesmos tipos, mesmas regras e mesmos dados). Muda so o padrao de
--   nomes, que segue o exigido nesta disciplina:
--     tabela....: tb_<entidade>
--     chave.....: cd_<entidade>
--     sequence..: sq_tb_<entidade>
--     colunas...: prefixo cd_, nm_, ds_, dt_, vl_, qt_, nr_, st_, in_ (S/N)
--     constraint: pk_, fk_, uk_, ck_
--   As tabelas T_CLH_* da disciplina de BD ficam no mesmo schema e nao sao
--   tocadas por este script.
--
-- DADOS
--   Nomes das prefeituras e codigos IBGE sao reais. Todo o resto e SIMULADO
--   (repasses, agricultores, CAF, telefones, chamadas, itens, precos e
--   propostas). E o estado final da carga do BD: a chamada 5 homologada, as
--   propostas dela contratadas (contrato fixo em 09/11/2026) e a proposta 8
--   removida. Nao ha CPF, endereco, data de nascimento nem sexo (LGPD).
--
-- COMO EXECUTAR
--   Oracle SQL Developer, conexao do grupo, "Executar Script" (F5) no arquivo
--   inteiro. Codificacao UTF-8 (Ferramentas > Preferencias > Ambiente >
--   Codificacao) para os acentos. Pode ser executado varias vezes: a secao 1
--   apaga e recria somente as tabelas tb_* e sequences sq_tb_* deste script.
--
-- ORDEM DO ARQUIVO
--   1. Limpeza (DROP)       2. Sequences      3. Tabelas na ordem das FKs
--   4. Comentarios          5. Carga + COMMIT 6. Conferencia
-- ============================================================================

SET DEFINE OFF;
SET SERVEROUTPUT ON;

-- ----------------------------------------------------------------------------
-- 1. LIMPEZA
-- Remove as tabelas (da filha para a mae) e as sequences deste script.
-- O bloco ignora so o erro "objeto nao existe" (ORA-00942 para tabela e
-- ORA-02289 para sequence), que acontece na primeira execucao.
-- ----------------------------------------------------------------------------
DECLARE
    PROCEDURE remover(p_comando IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_comando;
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE NOT IN (-942, -2289) THEN
                RAISE;
            END IF;
    END remover;
BEGIN
    remover('DROP TABLE tb_proposta CASCADE CONSTRAINTS');
    remover('DROP TABLE tb_item_chamada CASCADE CONSTRAINTS');
    remover('DROP TABLE tb_chamada_publica CASCADE CONSTRAINTS');
    remover('DROP TABLE tb_produto CASCADE CONSTRAINTS');
    remover('DROP TABLE tb_agricultor CASCADE CONSTRAINTS');
    remover('DROP TABLE tb_entidade_executora CASCADE CONSTRAINTS');
    remover('DROP SEQUENCE sq_tb_proposta');
    remover('DROP SEQUENCE sq_tb_item_chamada');
    remover('DROP SEQUENCE sq_tb_chamada_publica');
    remover('DROP SEQUENCE sq_tb_produto');
    remover('DROP SEQUENCE sq_tb_agricultor');
    remover('DROP SEQUENCE sq_tb_entidade_executora');
    DBMS_OUTPUT.PUT_LINE('Limpeza concluida: tabelas tb_* e sequences sq_tb_* do ColheAi removidas.');
END;
/

-- ----------------------------------------------------------------------------
-- 2. SEQUENCES
-- Uma por tabela. A API usa sq_tb_<entidade>.nextval no INSERT (como na aula).
-- A carga abaixo usa codigos fixos de 1 a 99, entao as sequences comecam em
-- 100 e um cadastro feito pela API nunca colide com a carga.
-- ----------------------------------------------------------------------------
CREATE SEQUENCE sq_tb_entidade_executora START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_agricultor         START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_produto            START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_chamada_publica    START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_item_chamada       START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_proposta           START WITH 100 INCREMENT BY 1 NOCACHE;

-- ----------------------------------------------------------------------------
-- 3. TABELAS (na ordem das FKs: primeiro quem nao depende de ninguem)
-- Relacionamentos 1:N:
--   tb_entidade_executora 1:N tb_chamada_publica
--   tb_chamada_publica    1:N tb_item_chamada
--   tb_produto            1:N tb_item_chamada
--   tb_item_chamada       1:N tb_proposta
--   tb_agricultor         1:N tb_proposta
-- ----------------------------------------------------------------------------
CREATE TABLE tb_entidade_executora (
    cd_entidade        NUMBER(6)     NOT NULL,
    nm_entidade        VARCHAR2(120) NOT NULL,
    nm_municipio       VARCHAR2(60)  NOT NULL,
    cd_uf              CHAR(2)       NOT NULL,
    cd_ibge_municipio  NUMBER(7)     NOT NULL,
    vl_repasse_anual   NUMBER(14,2)  NOT NULL,
    nr_ano_repasse     NUMBER(4)     NOT NULL,
    CONSTRAINT pk_entidade_executora PRIMARY KEY (cd_entidade),
    CONSTRAINT uk_entidade_ibge      UNIQUE (cd_ibge_municipio),
    CONSTRAINT ck_entidade_ibge      CHECK (cd_ibge_municipio BETWEEN 1000000 AND 9999999),
    CONSTRAINT ck_entidade_repasse   CHECK (vl_repasse_anual > 0),
    CONSTRAINT ck_entidade_ano       CHECK (nr_ano_repasse BETWEEN 2026 AND 2099)
);

CREATE TABLE tb_agricultor (
    cd_agricultor      NUMBER(6)     NOT NULL,
    nm_agricultor      VARCHAR2(120) NOT NULL,
    nr_caf             VARCHAR2(25)  NOT NULL,
    ds_tipo_fornecedor VARCHAR2(14)  NOT NULL,
    qt_familia_caf     NUMBER(4)     DEFAULT 1 NOT NULL,
    nm_municipio       VARCHAR2(60)  NOT NULL,
    cd_uf              CHAR(2)       NOT NULL,
    nm_distrito        VARCHAR2(60),
    in_organico        CHAR(1)       DEFAULT 'N' NOT NULL,
    nr_telefone        VARCHAR2(11),
    dt_cadastro        DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_agricultor          PRIMARY KEY (cd_agricultor),
    -- o limite de venda do PNAE e por CAF: nao pode haver dois cadastros com o mesmo
    CONSTRAINT uk_agricultor_caf      UNIQUE (nr_caf),
    CONSTRAINT ck_agricultor_tipo     CHECK (ds_tipo_fornecedor IN ('INDIVIDUAL', 'GRUPO_INFORMAL', 'GRUPO_FORMAL')),
    -- individual = 1 familia; grupo = 2 ou mais (o teto e 40000 x familias)
    CONSTRAINT ck_agricultor_familia  CHECK ((ds_tipo_fornecedor = 'INDIVIDUAL' AND qt_familia_caf = 1)
                                          OR (ds_tipo_fornecedor <> 'INDIVIDUAL' AND qt_familia_caf >= 2)),
    CONSTRAINT ck_agricultor_organico CHECK (in_organico IN ('S', 'N')),
    CONSTRAINT ck_agricultor_telefone CHECK (LENGTH(nr_telefone) IN (10, 11))
);

CREATE TABLE tb_produto (
    cd_produto         NUMBER(6)     NOT NULL,
    nm_produto         VARCHAR2(60)  NOT NULL,
    ds_categoria       VARCHAR2(15)  NOT NULL,
    ds_unidade_medida  VARCHAR2(7)   NOT NULL,
    CONSTRAINT pk_produto           PRIMARY KEY (cd_produto),
    CONSTRAINT uk_produto_nome      UNIQUE (nm_produto),
    CONSTRAINT ck_produto_categoria CHECK (ds_categoria IN ('HORTALICA', 'FRUTA', 'RAIZ_TUBERCULO', 'GRAO', 'ORIGEM_ANIMAL', 'PROCESSADO')),
    CONSTRAINT ck_produto_unidade   CHECK (ds_unidade_medida IN ('KG', 'MACO', 'UNIDADE', 'DUZIA', 'LITRO'))
);

CREATE TABLE tb_chamada_publica (
    cd_chamada         NUMBER(6)     NOT NULL,
    cd_entidade        NUMBER(6)     NOT NULL,
    nr_edital          VARCHAR2(20)  NOT NULL,
    ds_objeto          VARCHAR2(200) NOT NULL,
    dt_abertura        DATE          NOT NULL,
    dt_encerramento    DATE          NOT NULL,
    st_chamada         VARCHAR2(10)  DEFAULT 'ABERTA' NOT NULL,
    CONSTRAINT pk_chamada_publica  PRIMARY KEY (cd_chamada),
    CONSTRAINT fk_chamada_entidade FOREIGN KEY (cd_entidade)
        REFERENCES tb_entidade_executora (cd_entidade),
    CONSTRAINT uk_chamada_edital   UNIQUE (cd_entidade, nr_edital),
    CONSTRAINT ck_chamada_periodo  CHECK (dt_encerramento > dt_abertura),
    CONSTRAINT ck_chamada_status   CHECK (st_chamada IN ('ABERTA', 'EM_ANALISE', 'HOMOLOGADA', 'FRACASSADA'))
);

CREATE TABLE tb_item_chamada (
    cd_item_chamada    NUMBER(6)     NOT NULL,
    cd_chamada         NUMBER(6)     NOT NULL,
    cd_produto         NUMBER(6)     NOT NULL,
    qt_demandada       NUMBER(10,2)  NOT NULL,
    vl_preco_unitario  NUMBER(8,2)   NOT NULL,
    CONSTRAINT pk_item_chamada         PRIMARY KEY (cd_item_chamada),
    CONSTRAINT fk_item_chamada         FOREIGN KEY (cd_chamada)
        REFERENCES tb_chamada_publica (cd_chamada),
    CONSTRAINT fk_item_produto         FOREIGN KEY (cd_produto)
        REFERENCES tb_produto (cd_produto),
    -- um produto aparece uma vez so em cada chamada
    CONSTRAINT uk_item_chamada_produto UNIQUE (cd_chamada, cd_produto),
    CONSTRAINT ck_item_qt              CHECK (qt_demandada > 0),
    CONSTRAINT ck_item_preco           CHECK (vl_preco_unitario > 0)
);

-- O valor da proposta NAO e guardado: e calculado (qt_ofertada x preco do item).
-- O contrato fica na propria proposta (status CONTRATADA + dt_contrato).
CREATE TABLE tb_proposta (
    cd_proposta        NUMBER(6)     NOT NULL,
    cd_item_chamada    NUMBER(6)     NOT NULL,
    cd_agricultor      NUMBER(6)     NOT NULL,
    qt_ofertada        NUMBER(10,2)  NOT NULL,
    dt_envio           DATE          NOT NULL,
    st_proposta        VARCHAR2(10)  DEFAULT 'ENVIADA' NOT NULL,
    dt_contrato        DATE,
    CONSTRAINT pk_proposta             PRIMARY KEY (cd_proposta),
    CONSTRAINT fk_proposta_item        FOREIGN KEY (cd_item_chamada)
        REFERENCES tb_item_chamada (cd_item_chamada),
    CONSTRAINT fk_proposta_agricultor  FOREIGN KEY (cd_agricultor)
        REFERENCES tb_agricultor (cd_agricultor),
    -- o agricultor oferta cada item uma vez so
    CONSTRAINT uk_proposta_item_agric  UNIQUE (cd_item_chamada, cd_agricultor),
    CONSTRAINT ck_proposta_qt          CHECK (qt_ofertada > 0),
    CONSTRAINT ck_proposta_status      CHECK (st_proposta IN ('ENVIADA', 'CONTRATADA', 'RECUSADA')),
    CONSTRAINT ck_proposta_contrato    CHECK ((st_proposta = 'CONTRATADA' AND dt_contrato IS NOT NULL)
                                           OR (st_proposta <> 'CONTRATADA' AND dt_contrato IS NULL)),
    CONSTRAINT ck_proposta_dt_contrato CHECK (dt_contrato >= dt_envio)
);
-- A regra dos R$ 40 mil por CAF, por ano civil e por EEx NAO e um CHECK: o
-- CHECK olha uma linha so, e a regra soma varias propostas. Ela fica na API
-- (Agricultor.verificarLimite), que devolve o erro 409.

-- ----------------------------------------------------------------------------
-- 4. COMENTARIOS
-- ----------------------------------------------------------------------------
COMMENT ON TABLE tb_entidade_executora IS 'EEx (Entidade Executora) do PNAE: prefeitura que recebe o repasse federal e compra a merenda. Base do painel da meta de 45%.';
COMMENT ON TABLE tb_agricultor IS 'Fornecedor da agricultura familiar identificado pelo CAF: individual ou grupo. Teto anual por EEx = 40000 x qt_familia_caf.';
COMMENT ON TABLE tb_produto IS 'Alimento comprado nas chamadas, com categoria e unidade de medida.';
COMMENT ON TABLE tb_chamada_publica IS 'Edital de chamada publica da EEx para comprar da agricultura familiar, com periodo e status.';
COMMENT ON TABLE tb_item_chamada IS 'Item da chamada: produto, quantidade demandada e preco de aquisicao publicado pela EEx.';
COMMENT ON TABLE tb_proposta IS 'Proposta do agricultor para um item da chamada. Valor = qt_ofertada x preco do item. CONTRATADA = contrato assinado.';

-- ----------------------------------------------------------------------------
-- 5. CARGA (codigos fixos de 1 a 99; dados SIMULADOS, exceto prefeituras e IBGE)
-- ----------------------------------------------------------------------------
-- 5.1 EEx (Entidade Executora) - 3 registros. Repasse de SP na ordem de grandeza do repasse federal de 2018; demais simulados.
INSERT INTO tb_entidade_executora (cd_entidade, nm_entidade, nm_municipio, cd_uf, cd_ibge_municipio, vl_repasse_anual, nr_ano_repasse)
VALUES (1, 'Prefeitura de São Paulo - SME/CODAE', 'São Paulo', 'SP', 3550308, 95000000.00, 2026);
INSERT INTO tb_entidade_executora (cd_entidade, nm_entidade, nm_municipio, cd_uf, cd_ibge_municipio, vl_repasse_anual, nr_ano_repasse)
VALUES (2, 'Prefeitura de Embu-Guaçu - Secretaria de Educação', 'Embu-Guaçu', 'SP', 3515103, 400000.00, 2026);
INSERT INTO tb_entidade_executora (cd_entidade, nm_entidade, nm_municipio, cd_uf, cd_ibge_municipio, vl_repasse_anual, nr_ano_repasse)
VALUES (3, 'Prefeitura de Itapecerica da Serra - Secretaria de Educação', 'Itapecerica da Serra', 'SP', 3522208, 220000.00, 2026);

-- 5.2 Agricultor - 10 registros (nomes, CAF e telefones ficticios).
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (1, 'Maria Aparecida dos Santos', 'SIM-CAF-PF-0001', 'INDIVIDUAL', 1, 'São Paulo', 'SP', 'Parelheiros', 'S', '11999990001', TO_DATE('12/01/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (2, 'José Carlos Ferreira', 'SIM-CAF-PF-0002', 'INDIVIDUAL', 1, 'São Paulo', 'SP', 'Marsilac', 'N', '11999990002', TO_DATE('15/01/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (3, 'Cooperativa Agroecológica Sul Paulistana', 'SIM-CAF-PJ-0003', 'GRUPO_FORMAL', 25, 'São Paulo', 'SP', 'Parelheiros', 'S', '1159990003', TO_DATE('20/01/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (4, 'Ana Lúcia Nascimento', 'SIM-CAF-PF-0004', 'INDIVIDUAL', 1, 'São Paulo', 'SP', 'Grajaú', 'S', NULL, TO_DATE('02/02/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (5, 'Associação dos Produtores Rurais de Embu-Guaçu', 'SIM-CAF-PJ-0005', 'GRUPO_FORMAL', 12, 'Embu-Guaçu', 'SP', NULL, 'N', '1159990005', TO_DATE('10/02/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (6, 'Grupo Mulheres da Terra', 'SIM-CAF-PF-0006', 'GRUPO_INFORMAL', 6, 'Embu-Guaçu', 'SP', NULL, 'S', '11999990006', TO_DATE('18/02/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (7, 'Paulo Henrique Oliveira', 'SIM-CAF-PF-0007', 'INDIVIDUAL', 1, 'Itapecerica da Serra', 'SP', NULL, 'N', '11999990007', TO_DATE('03/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (8, 'Tereza Cristina Moura', 'SIM-CAF-PF-0008', 'INDIVIDUAL', 1, 'Itapecerica da Serra', 'SP', NULL, 'N', '11999990008', TO_DATE('05/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (9, 'Benedito Ramos', 'SIM-CAF-PF-0009', 'INDIVIDUAL', 1, 'São Paulo', 'SP', 'Marsilac', 'N', NULL, TO_DATE('14/09/2026', 'DD/MM/YYYY'));
INSERT INTO tb_agricultor (cd_agricultor, nm_agricultor, nr_caf, ds_tipo_fornecedor, qt_familia_caf, nm_municipio, cd_uf, nm_distrito, in_organico, nr_telefone, dt_cadastro)
VALUES (10, 'Luana Pereira da Silva', 'SIM-CAF-PF-0010', 'INDIVIDUAL', 1, 'Juquitiba', 'SP', NULL, 'S', '11999990010', TO_DATE('04/11/2026', 'DD/MM/YYYY'));

-- 5.3 Produto - 10 registros.
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (1, 'Alface crespa', 'HORTALICA', 'UNIDADE');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (2, 'Couve-manteiga', 'HORTALICA', 'MACO');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (3, 'Cheiro-verde', 'HORTALICA', 'MACO');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (4, 'Tomate', 'HORTALICA', 'KG');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (5, 'Banana nanica', 'FRUTA', 'KG');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (6, 'Mandioca', 'RAIZ_TUBERCULO', 'KG');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (7, 'Batata-doce', 'RAIZ_TUBERCULO', 'KG');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (8, 'Caqui', 'FRUTA', 'KG');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (9, 'Feijão carioca', 'GRAO', 'KG');
INSERT INTO tb_produto (cd_produto, nm_produto, ds_categoria, ds_unidade_medida) VALUES (10, 'Ovo caipira', 'ORIGEM_ANIMAL', 'DUZIA');

-- 5.4 Chamada publica - 6 registros. A 3 esta ABERTA ate 27/11/2026; a 5 ja homologada.
INSERT INTO tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, ds_objeto, dt_abertura, dt_encerramento, st_chamada)
VALUES (1, 1, '001/2026', 'Hortaliças e frutas da agricultura familiar - 1º semestre', TO_DATE('02/02/2026', 'DD/MM/YYYY'), TO_DATE('06/03/2026', 'DD/MM/YYYY'), 'HOMOLOGADA');
INSERT INTO tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, ds_objeto, dt_abertura, dt_encerramento, st_chamada)
VALUES (2, 1, '002/2026', 'Grãos e raízes da agricultura familiar - 2º semestre', TO_DATE('01/06/2026', 'DD/MM/YYYY'), TO_DATE('03/07/2026', 'DD/MM/YYYY'), 'FRACASSADA');
INSERT INTO tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, ds_objeto, dt_abertura, dt_encerramento, st_chamada)
VALUES (3, 1, '003/2026', 'Hortifrúti e ovos para entregas de dezembro a março', TO_DATE('26/10/2026', 'DD/MM/YYYY'), TO_DATE('27/11/2026', 'DD/MM/YYYY'), 'ABERTA');
INSERT INTO tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, ds_objeto, dt_abertura, dt_encerramento, st_chamada)
VALUES (4, 2, '001/2026', 'Gêneros alimentícios da agricultura familiar 2026', TO_DATE('09/03/2026', 'DD/MM/YYYY'), TO_DATE('10/04/2026', 'DD/MM/YYYY'), 'HOMOLOGADA');
INSERT INTO tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, ds_objeto, dt_abertura, dt_encerramento, st_chamada)
VALUES (5, 2, '002/2026', 'Hortaliças e ovos - complementação do 2º semestre', TO_DATE('05/10/2026', 'DD/MM/YYYY'), TO_DATE('06/11/2026', 'DD/MM/YYYY'), 'HOMOLOGADA');
INSERT INTO tb_chamada_publica (cd_chamada, cd_entidade, nr_edital, ds_objeto, dt_abertura, dt_encerramento, st_chamada)
VALUES (6, 3, '001/2026', 'Hortifrúti da agricultura familiar 2026', TO_DATE('04/05/2026', 'DD/MM/YYYY'), TO_DATE('05/06/2026', 'DD/MM/YYYY'), 'HOMOLOGADA');

-- 5.5 Item da chamada - 18 registros (precos de aquisicao simulados).
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (1, 1, 1, 20000, 2.50);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (2, 1, 2, 12000, 3.20);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (3, 1, 5, 30000, 5.20);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (4, 1, 3, 8000, 2.80);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (5, 2, 9, 15000, 9.50);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (6, 2, 6, 10000, 4.30);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (7, 3, 1, 25000, 2.60);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (8, 3, 4, 15000, 7.50);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (9, 3, 7, 8000, 4.80);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (10, 3, 10, 3000, 14.00);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (11, 4, 6, 12000, 4.10);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (12, 4, 5, 15000, 5.00);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (13, 4, 2, 5000, 3.00);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (14, 5, 2, 6000, 3.10);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (15, 5, 10, 2000, 13.50);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (16, 6, 4, 8000, 7.20);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (17, 6, 8, 5000, 8.90);
INSERT INTO tb_item_chamada (cd_item_chamada, cd_chamada, cd_produto, qt_demandada, vl_preco_unitario) VALUES (18, 6, 5, 6000, 5.10);

-- 5.6 Proposta - 20 registros. Maria (1) tem R$ 27.800,00 comprometidos em SP em 2026:
--     ainda pode vender R$ 12.200,00 para essa EEx no ano (caso do erro 409 na demonstracao).
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (1, 1, 1, 6000, TO_DATE('20/02/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('16/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (2, 2, 1, 4000, TO_DATE('20/02/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('16/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (3, 1, 3, 14000, TO_DATE('23/02/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('16/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (4, 2, 3, 8000, TO_DATE('23/02/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('16/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (5, 3, 2, 5000, TO_DATE('25/02/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('16/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (6, 4, 4, 3000, TO_DATE('26/02/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('16/03/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (7, 6, 2, 2000, TO_DATE('20/06/2026', 'DD/MM/YYYY'), 'RECUSADA', NULL);
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (9, 7, 3, 15000, TO_DATE('04/11/2026', 'DD/MM/YYYY'), 'ENVIADA', NULL);
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (10, 8, 3, 9000, TO_DATE('04/11/2026', 'DD/MM/YYYY'), 'ENVIADA', NULL);
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (11, 8, 7, 3000, TO_DATE('05/11/2026', 'DD/MM/YYYY'), 'ENVIADA', NULL);
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (12, 11, 5, 8000, TO_DATE('20/03/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('27/04/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (13, 12, 5, 9000, TO_DATE('20/03/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('27/04/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (14, 12, 2, 6000, TO_DATE('24/03/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('27/04/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (15, 13, 6, 4000, TO_DATE('25/03/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('27/04/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (16, 14, 6, 3000, TO_DATE('15/10/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('09/11/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (17, 15, 6, 1500, TO_DATE('15/10/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('09/11/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (18, 16, 7, 4000, TO_DATE('15/05/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('22/06/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (19, 17, 8, 3500, TO_DATE('18/05/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('22/06/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (20, 18, 5, 6000, TO_DATE('20/05/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('22/06/2026', 'DD/MM/YYYY'));
INSERT INTO tb_proposta (cd_proposta, cd_item_chamada, cd_agricultor, qt_ofertada, dt_envio, st_proposta, dt_contrato) VALUES (21, 16, 8, 1500, TO_DATE('25/05/2026', 'DD/MM/YYYY'), 'CONTRATADA', TO_DATE('22/06/2026', 'DD/MM/YYYY'));

COMMIT;

-- ----------------------------------------------------------------------------
-- 6. CONFERENCIA
-- Resultado esperado: 3, 10, 10, 6, 18 e 20 registros (67 no total).
-- ----------------------------------------------------------------------------
SELECT 'tb_entidade_executora' AS tabela, COUNT(*) AS registros FROM tb_entidade_executora
UNION ALL SELECT 'tb_agricultor',      COUNT(*) FROM tb_agricultor
UNION ALL SELECT 'tb_produto',         COUNT(*) FROM tb_produto
UNION ALL SELECT 'tb_chamada_publica', COUNT(*) FROM tb_chamada_publica
UNION ALL SELECT 'tb_item_chamada',    COUNT(*) FROM tb_item_chamada
UNION ALL SELECT 'tb_proposta',        COUNT(*) FROM tb_proposta;
