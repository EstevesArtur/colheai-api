# ColheAí · API REST (Domain Driven Design Using Java)

**Global Solution 2026/2 · CitizenTech · FIAP · Since Young · Turma 1TDSPV-2026**

| Integrante | RM |
|---|---|
| Artur Bianchini Esteves (representante) | RM569450 |
| João Vitor Oliveira Fontenele Martins | RM570783 |

| Link | Endereço |
|---|---|
| API publicada (Render) | https://colheai-api.onrender.com (teste: [`/status`](https://colheai-api.onrender.com/status)) |
| Repositório | https://github.com/EstevesArtur/colheai-api |
| Vídeo (YouTube, não listado) | _[a incluir: o vídeo mostra o front-end integrado a esta API]_ |

> No plano gratuito do Render, a API "dorme" depois de 15 minutos sem uso e leva cerca de 1 a 2 minutos para acordar. Abra `/status` e espere responder antes de testar.

---

## O que a API resolve para o cidadão

Desde 2026, toda prefeitura precisa gastar **pelo menos 45% do repasse federal do PNAE** (a merenda escolar) comprando **direto da agricultura familiar** (Lei 15.226/2025). A cidade de São Paulo não cumpriu nem os antigos 30%: a auditoria do TCM-SP registrou 26,2% em 2018 e 26,8% em 2019.

O **ColheAí** conecta os dois lados:

- **O agricultor familiar** vê as chamadas públicas abertas, os itens de cada uma (produto, quantidade e preço publicado) e envia a sua proposta. A API já aplica a regra do PNAE: **cada CAF vende no máximo R$ 40 mil por ano para a mesma prefeitura** (R$ 40 mil por família, no caso de grupos).
- **A EEx (Entidade Executora), que é a prefeitura**, acompanha no painel quanto já contratou da agricultura familiar e quanto falta para chegar aos 45%.
- **O estudante da escola pública**, o beneficiado final, recebe alimento fresco e local na merenda.

Esta API é o back-end consumido pelo front-end React do grupo.

> Dados: os nomes das prefeituras e os códigos IBGE são reais. Agricultores, CAF, chamadas, preços, repasses e propostas são **simulados** para demonstração.

---

## Tecnologias

Java 21 · Spring Boot 4.1.1 (`spring-boot-starter-webmvc`, `-jdbc`, `-validation`) · JDBC com `DataSource` e `PreparedStatement` (sem JPA, Hibernate ou Spring Data) · Oracle Database (driver `ojdbc17`) · ModelMapper 3.2.0 · Bean Validation · Docker (multi-stage) · Render.

## Estrutura do projeto

```
colheai-api/
├── Dockerfile              build multi-stage (JDK 21 + Maven -> JRE 21)
├── render.yaml             descrição do serviço no Render
├── pom.xml
├── sql/colheai_ddd_tabelas.sql   tabelas tb_*, sequences sq_tb_* e carga inicial
├── postman/                coleção do Postman com todas as rotas
├── testes/                 matriz de testes com curl e as evidências de execução
└── src/main/java/br/com/fiap/colheai/
    ├── model/       entidades do domínio (EntidadeExecutora, ChamadaPublica, ItemChamada, Produto, Agricultor, Proposta)
    ├── dao/         acesso ao banco com JDBC, um DAO por entidade
    ├── dto/         entrada (com Bean Validation) e saída da API
    ├── controller/  rotas REST e status HTTP
    ├── exception/   exceções do domínio
    ├── handler/     tratamento global de erros (JSON padronizado)
    └── config/      CORS e ModelMapper
```

## Modelo e relacionamentos

Seis tabelas no Oracle (`tb_<entidade>`, chave `cd_<entidade>`, sequence `sq_tb_<entidade>`), com cinco relacionamentos 1:N:

- `tb_entidade_executora` 1:N `tb_chamada_publica`
- `tb_chamada_publica` 1:N `tb_item_chamada`
- `tb_produto` 1:N `tb_item_chamada`
- `tb_item_chamada` 1:N `tb_proposta`
- `tb_agricultor` 1:N `tb_proposta`

## Regra de negócio

Uma proposta é recusada com **409** quando, somada ao que o agricultor já comprometeu com a mesma EEx no mesmo ano (propostas ENVIADA ou CONTRATADA), passaria do teto de **R$ 40.000,00 × número de famílias do CAF**. Chegar exatamente no teto é permitido. A soma é feita em SQL e a verificação fica no próprio model (`Agricultor.verificarLimite`), que lança a exceção própria `LimiteVendaExcedidoException`.

Outras regras (também 409): proposta ou item para chamada que não está ABERTA, CAF já cadastrado, oferta repetida do mesmo agricultor para o mesmo item e remoção de registro que tem filhos.

## Endpoints

| Recurso | Rotas |
|---|---|
| Saúde | `GET /status` |
| EEx (Entidade Executora) | `POST/GET /entidades` · `GET/PUT/DELETE /entidades/{id}` · `GET/POST /entidades/{id}/chamadas` · `GET /entidades/{id}/meta` |
| Chamada pública | `GET /chamadas` (filtro `?status=ABERTA`) · `GET/PUT/DELETE /chamadas/{id}` · `GET/POST /chamadas/{id}/itens` |
| Item da chamada | `GET /itens` · `GET/PUT/DELETE /itens/{id}` · `GET/POST /itens/{id}/propostas` |
| Proposta | `GET /propostas` · `GET/PUT/DELETE /propostas/{id}` |
| Agricultor | `POST/GET /agricultores` · `GET/PUT/DELETE /agricultores/{id}` · `GET /agricultores/{id}/propostas` |
| Produto | `POST/GET /produtos` · `GET/PUT/DELETE /produtos/{id}` |

Status usados: **201 + Location** (cadastro), **200** (consulta e atualização), **204** (remoção), **400** (dados inválidos ou JSON malformado), **404** (id ou rota inexistente), **405** (método não suportado), **409** (regra de negócio ou registro com filhos) e **500** (erro inesperado, com mensagem genérica). Todo erro volta no mesmo JSON:

```json
{ "erro": "Dados inválidos", "detalhe": "Corrija os campos indicados e envie de novo",
  "campos": { "quantidade": "Quantidade deve ser maior que zero" } }
```

Exemplo de proposta (`POST /itens/9/propostas`):

```json
{ "agricultor": { "id": 9 }, "quantidade": 1000 }
```

## Como rodar

### 1. Banco de dados
No Oracle SQL Developer, conectado ao schema do grupo, abra `sql/colheai_ddd_tabelas.sql` e execute com **F5** (Executar Script). O script pode ser executado várias vezes: ele apaga e recria só as tabelas `tb_*` e as sequences `sq_tb_*` dele.

### 2. Variáveis de ambiente
Nenhuma credencial fica no código. Configure (no IntelliJ: Run > Edit Configurations > Environment variables; no Render: Environment):

| Variável | Para quê |
|---|---|
| `DB_URL` | URL JDBC do Oracle (ex.: `jdbc:oracle:thin:@servidor:1521:SID`) |
| `DB_USERNAME` | usuário do banco |
| `DB_PASSWORD` | senha do banco |
| `PORT` | opcional; o Render define sozinho. Sem ela, a API usa a porta 8080 |

O arquivo `.env.example` lista os nomes, sem valores.

### 3. Executar
- **IntelliJ:** abra a pasta do projeto, configure as variáveis acima e rode `ColheaiApplication`. A API sobe em `http://localhost:8080`.
- **Docker:** `docker build -t colheai-api .` e depois `docker run -p 8080:8080 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... colheai-api`.

### 4. Testar
- **Postman:** importe `postman/ColheAi_API.postman_collection.json`. Troque a variável `baseUrl` para a URL do Render quando for testar a API publicada.
- **curl:** `bash testes/matriz_curl.sh https://colheai-api.onrender.com` roda os 105 casos (cada rota com cada status esperado).
- **Evidências:** em `testes/` estão as saídas reais da matriz e da coleção do Postman, local e no Render (`evidencia_render_matriz_curl.txt`: 105 de 105; `evidencia_render_postman_newman.txt`: 121 de 121).

## Deploy

A API roda no **Render** como **Web Service com Docker** (plano Free), a partir deste repositório: **https://colheai-api.onrender.com**. O Render constrói a imagem pelo `Dockerfile` da raiz e injeta `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. O health check usa `GET /status`, que não consulta o banco. O `render.yaml` descreve a mesma configuração.

## Uso de IA

Usamos o **Claude (Anthropic, claude.ai)** como apoio técnico durante toda a disciplina:

- **Leitura do material:** leitura do repositório do professor (Apostila16) e do edital, para listar os padrões da aula e as divergências com o edital. Quando os dois divergiam, seguimos o edital.
- **Proposta e decisões:** proposta de escopo, endpoints e regra de negócio. O grupo decidiu: tabelas `tb_` próprias (D6), seis entidades e a regra dos R$ 40 mil no model.
- **Código:** geração do código no estilo da aula, revisado por nós.
- **Testes:** os testes (DAOs no Oracle, matriz com curl e coleção do Postman) apontaram erros reais, que foram corrigidos. Dois exemplos: o erro de API devolvido em HTML para navegador e a ordem das chaves do JSON no Spring Boot 4.

Nada foi aceito sem rodar. As evidências de execução estão em `testes/`. A documentação técnica em PDF (diagrama de classes, DER, endpoints, regras, deploy) acompanha a entrega no Portal.
