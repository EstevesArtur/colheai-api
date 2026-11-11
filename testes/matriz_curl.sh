#!/bin/bash
# ============================================================================
# ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
# Since Young - 1TDSPV-2026
# Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
# ----------------------------------------------------------------------------
# MATRIZ DE TESTES COM CURL: cada endpoint x status HTTP esperado.
# Para cada requisicao mostra o metodo, a rota, o status esperado, o status
# obtido, o Location (quando ha) e o corpo da resposta.
#
# Como rodar (com a API no ar e o banco recem-carregado pelo script SQL):
#   bash testes/matriz_curl.sh                       -> API local (localhost:8080)
#   bash testes/matriz_curl.sh https://SUA-API.onrender.com   -> API no Render
# O teste do 500 (banco fora do ar) so roda localmente: veja o final do arquivo.
# Os registros criados pelo teste sao removidos no proprio teste.
# ============================================================================

API="${1:-http://localhost:8080}"
OK=0; FALHA=0; N=0
CORPO=""

req() {  # req METODO ROTA STATUS_ESPERADO [JSON] [args extras do curl]
    local metodo="$1" rota="$2" esperado="$3" dados="$4"; shift 4
    N=$((N + 1))
    local args=(-s -o /tmp/corpo.txt -D /tmp/cabecalho.txt -w '%{http_code}' -X "$metodo" "$API$rota")
    if [ -n "$dados" ]; then args+=(-H 'Content-Type: application/json' --data "$dados"); fi
    local obtido
    obtido=$(curl "${args[@]}" "$@")
    CORPO=$(cat /tmp/corpo.txt)
    local local_hdr
    local_hdr=$(grep -i '^location:' /tmp/cabecalho.txt | tr -d '\r' | cut -d' ' -f2)
    local marca="OK"
    if [ "$obtido" == "$esperado" ]; then OK=$((OK + 1)); else FALHA=$((FALHA + 1)); marca="FALHOU"; fi
    printf '%03d | %-6s %-38s | esperado %s | obtido %s | %s\n' "$N" "$metodo" "$rota" "$esperado" "$obtido" "$marca"
    if [ -n "$local_hdr" ]; then echo "      Location: $local_hdr"; fi
    if [ -n "$CORPO" ]; then echo "      ${CORPO:0:420}"; fi
}

id_do_corpo() { python3 -c "import json,sys; print(json.loads(sys.argv[1])['id'])" "$CORPO"; }

titulo() { echo; echo "=== $1"; }

titulo "SAUDE"
req GET /status 200 ""

titulo "ENTIDADE EXECUTORA (EEx)"
req GET /entidades 200 ""
req GET /entidades/1 200 ""
req GET /entidades/999 404 ""
req GET /entidades/abc 400 ""
req POST /entidades 201 '{"nome":"Prefeitura de Juquitiba - Secretaria de Educação","municipio":"Juquitiba","uf":"SP","codigoIbge":3526209,"repasseAnual":180000.00,"anoRepasse":2026}'
EEX=$(id_do_corpo)
req POST /entidades 400 '{"nome":"","municipio":"Juquitiba","uf":"sp","codigoIbge":123,"repasseAnual":0,"anoRepasse":2025}'
req POST /entidades 409 '{"nome":"Outra SP","municipio":"São Paulo","uf":"SP","codigoIbge":3550308,"repasseAnual":1000,"anoRepasse":2026}'
req PUT /entidades/$EEX 200 '{"nome":"Prefeitura de Juquitiba - SME","municipio":"Juquitiba","uf":"SP","codigoIbge":3526209,"repasseAnual":185000.00,"anoRepasse":2026}'
req PUT /entidades/999 404 '{"nome":"X","municipio":"X","uf":"SP","codigoIbge":3526209,"repasseAnual":1,"anoRepasse":2026}'
req PUT /entidades/$EEX 400 '{"nome":"X","municipio":"X","uf":"SP","codigoIbge":3526209,"repasseAnual":-5,"anoRepasse":2026}'
req PUT /entidades/$EEX 409 '{"nome":"X","municipio":"X","uf":"SP","codigoIbge":3550308,"repasseAnual":1,"anoRepasse":2026}'
req GET /entidades/1/chamadas 200 ""
req GET /entidades/999/chamadas 404 ""
req POST /entidades/$EEX/chamadas 201 '{"numeroEdital":"001/2026","objeto":"Hortaliças para as escolas de Juquitiba","dataAbertura":"2026-11-10","dataEncerramento":"2026-12-10","status":"ABERTA"}'
CHAMADA=$(id_do_corpo)
req POST /entidades/$EEX/chamadas 400 '{"numeroEdital":"002/2026","objeto":"Período invertido","dataAbertura":"2026-12-10","dataEncerramento":"2026-11-10","status":"ABERTA"}'
req POST /entidades/999/chamadas 404 '{"numeroEdital":"002/2026","objeto":"EEx inexistente","dataAbertura":"2026-11-10","dataEncerramento":"2026-12-10","status":"ABERTA"}'
req POST /entidades/$EEX/chamadas 409 '{"numeroEdital":"001/2026","objeto":"Edital repetido na mesma EEx","dataAbertura":"2026-11-10","dataEncerramento":"2026-12-10","status":"ABERTA"}'
req GET /entidades/1/meta 200 ""
req GET /entidades/3/meta 200 ""
req GET /entidades/999/meta 404 ""

titulo "CHAMADA PUBLICA"
req GET /chamadas 200 ""
req GET '/chamadas?status=ABERTA' 200 ""
req GET /chamadas/$CHAMADA 200 ""
req GET /chamadas/999 404 ""
req PUT /chamadas/$CHAMADA 200 '{"numeroEdital":"001/2026","objeto":"Hortaliças e frutas para as escolas de Juquitiba","dataAbertura":"2026-11-10","dataEncerramento":"2026-12-15","status":"ABERTA"}'
req PUT /chamadas/999 404 '{"numeroEdital":"001/2026","objeto":"X","dataAbertura":"2026-11-10","dataEncerramento":"2026-12-15","status":"ABERTA"}'
req PUT /chamadas/$CHAMADA 400 '{"numeroEdital":"001/2026","objeto":"X","dataAbertura":"2026-11-10","dataEncerramento":"2026-12-15","status":"CANCELADA"}'
req PATCH /chamadas/1 405 '{"status":"ABERTA"}'
req GET /chamadas/3/itens 200 ""
req GET /chamadas/999/itens 404 ""
req POST /chamadas/$CHAMADA/itens 201 '{"produto":{"id":2},"quantidadeDemandada":3000,"precoUnitario":3.40}'
ITEM_EEX=$(id_do_corpo)
req POST /chamadas/$CHAMADA/itens 400 '{"quantidadeDemandada":3000,"precoUnitario":0}'
req POST /chamadas/1/itens 409 '{"produto":{"id":6},"quantidadeDemandada":1000,"precoUnitario":4.00}'
req POST /chamadas/$CHAMADA/itens 404 '{"produto":{"id":999},"quantidadeDemandada":1000,"precoUnitario":4.00}'
req POST /chamadas/$CHAMADA/itens 409 '{"produto":{"id":2},"quantidadeDemandada":10,"precoUnitario":3.40}'
req POST /chamadas/999/itens 404 '{"produto":{"id":6},"quantidadeDemandada":1000,"precoUnitario":4.00}'
# item de mandioca a R$ 4,00 na chamada 3 (SP, ABERTA): 12.200 / 4,00 = 3.050 exatos, bom para testar o limite
req POST /chamadas/3/itens 201 '{"produto":{"id":6},"quantidadeDemandada":5000,"precoUnitario":4.00}'
ITEM_SP=$(id_do_corpo)

titulo "ITEM DA CHAMADA"
req GET /itens 200 ""
req GET /itens/$ITEM_EEX 200 ""
req GET /itens/999 404 ""
req PUT /itens/$ITEM_EEX 200 '{"produto":{"id":2},"quantidadeDemandada":3500,"precoUnitario":3.40}'
req PUT /itens/999 404 '{"produto":{"id":2},"quantidadeDemandada":3500,"precoUnitario":3.40}'
req PUT /itens/$ITEM_EEX 400 '{"produto":{"id":2},"quantidadeDemandada":-1,"precoUnitario":3.40}'
req GET /itens/7/propostas 200 ""
req GET /itens/999/propostas 404 ""

titulo "PROPOSTA E A REGRA DOS R\$ 40 MIL (Maria, CAF SIM-CAF-PF-0001: R\$ 27.800,00 comprometidos em SP em 2026, restam R\$ 12.200,00)"
req GET /entidades/1/meta 200 ""
req POST /itens/$ITEM_SP/propostas 409 '{"agricultor":{"id":1},"quantidade":3050.01}'
echo "      ^ 3.050,01 x R\$ 4,00 = R\$ 12.200,04: passa R\$ 0,04 do teto -> 409"
req POST /itens/$ITEM_SP/propostas 201 '{"agricultor":{"id":1},"quantidade":3050}'
PROPOSTA=$(id_do_corpo)
echo "      ^ 3.050 x R\$ 4,00 = R\$ 12.200,00: chega exatamente no teto de R\$ 40.000,00 -> 201"
req POST /itens/9/propostas 409 '{"agricultor":{"id":1},"quantidade":1}'
echo "      ^ teto atingido: qualquer valor a mais nesta EEx em 2026 e recusado"
req POST /itens/7/propostas 409 '{"agricultor":{"id":3},"quantidade":1}'
echo "      ^ a cooperativa (25 familias, teto R\$ 1 milhao) ja ofertou o item 7: oferta repetida"
req POST /itens/5/propostas 409 '{"agricultor":{"id":9},"quantidade":100}'
echo "      ^ item 5 e da chamada 2, FRACASSADA"
req POST /itens/$ITEM_SP/propostas 400 '{"quantidade":0}'
req POST /itens/999/propostas 404 '{"agricultor":{"id":9},"quantidade":100}'
req POST /itens/$ITEM_SP/propostas 404 '{"agricultor":{"id":999},"quantidade":100}'
req POST /itens/$ITEM_SP/propostas 400 '{"agricultor":{"id":9},"quantidade":"muito"}'
req GET /propostas 200 ""
req GET /propostas/$PROPOSTA 200 ""
req GET /propostas/999 404 ""
req PUT /propostas/$PROPOSTA 200 '{"quantidade":3050,"status":"CONTRATADA"}'
echo "      ^ a propria proposta fica fora da soma: continua no teto e e contratada (dataContrato = hoje)"
req PUT /propostas/$PROPOSTA 409 '{"quantidade":3100,"status":"CONTRATADA"}'
req PUT /propostas/$PROPOSTA 400 '{"quantidade":3050,"status":"APROVADA"}'
req PUT /propostas/999 404 '{"quantidade":3050,"status":"ENVIADA"}'
req GET /agricultores/1/propostas 200 ""
req GET /entidades/1/meta 200 ""
echo "      ^ contratado de SP subiu de R\$ 122.800,00 para R\$ 135.000,00"
req DELETE /propostas/$PROPOSTA 204 ""
req DELETE /propostas/$PROPOSTA 404 ""

titulo "AGRICULTOR"
req GET /agricultores 200 ""
req GET /agricultores/1 200 ""
req GET /agricultores/999 404 ""
req POST /agricultores 201 '{"nome":"Cooperativa Verde Juquitiba","caf":"SIM-CAF-PJ-0200","tipoFornecedor":"GRUPO_FORMAL","quantidadeFamilias":8,"municipio":"Juquitiba","uf":"SP","organico":true,"telefone":"1140001234"}'
AGRICULTOR=$(id_do_corpo)
req POST /agricultores 409 '{"nome":"Outra Maria","caf":"SIM-CAF-PF-0001","tipoFornecedor":"INDIVIDUAL","quantidadeFamilias":1,"municipio":"São Paulo","uf":"SP","organico":false}'
req POST /agricultores 400 '{"nome":"Fulano","caf":"SIM-CAF-PF-0300","tipoFornecedor":"INDIVIDUAL","quantidadeFamilias":3,"municipio":"São Paulo","uf":"SP","organico":false,"telefone":"abc"}'
req PUT /agricultores/$AGRICULTOR 200 '{"nome":"Cooperativa Verde de Juquitiba","caf":"SIM-CAF-PJ-0200","tipoFornecedor":"GRUPO_FORMAL","quantidadeFamilias":9,"municipio":"Juquitiba","uf":"SP","organico":true,"telefone":"1140001234"}'
req PUT /agricultores/$AGRICULTOR 409 '{"nome":"X","caf":"SIM-CAF-PF-0001","tipoFornecedor":"GRUPO_FORMAL","quantidadeFamilias":9,"municipio":"Juquitiba","uf":"SP","organico":true}'
req PUT /agricultores/999 404 '{"nome":"X","caf":"SIM-CAF-PF-0999","tipoFornecedor":"INDIVIDUAL","quantidadeFamilias":1,"municipio":"X","uf":"SP","organico":false}'
req GET /agricultores/999/propostas 404 ""
req DELETE /agricultores/1 409 ""
req DELETE /agricultores/$AGRICULTOR 204 ""

titulo "PRODUTO"
req GET /produtos 200 ""
req GET /produtos/1 200 ""
req GET /produtos/999 404 ""
req POST /produtos 201 '{"nome":"Abobrinha","categoria":"HORTALICA","unidadeMedida":"KG"}'
PRODUTO=$(id_do_corpo)
req POST /produtos 409 '{"nome":"Tomate","categoria":"HORTALICA","unidadeMedida":"KG"}'
req POST /produtos 400 '{"nome":"Pão","categoria":"PADARIA","unidadeMedida":"KG"}'
req PUT /produtos/$PRODUTO 200 '{"nome":"Abobrinha italiana","categoria":"HORTALICA","unidadeMedida":"KG"}'
req PUT /produtos/999 404 '{"nome":"X","categoria":"HORTALICA","unidadeMedida":"KG"}'
req DELETE /produtos/4 409 ""
req DELETE /produtos/$PRODUTO 204 ""

titulo "REMOCOES (204 e 409 de registro com filhos) - devolve o banco ao estado da carga"
req DELETE /itens/7 409 ""
req DELETE /itens/$ITEM_SP 204 ""
req DELETE /chamadas/3 409 ""
req DELETE /itens/$ITEM_EEX 204 ""
req DELETE /chamadas/$CHAMADA 204 ""
req DELETE /entidades/1 409 ""
req DELETE /entidades/$EEX 204 ""
req DELETE /entidades/$EEX 404 ""

titulo "CASOS TRANSVERSAIS"
req GET /rota-que-nao-existe 404 ""
req GET /rota-que-nao-existe 404 "" -H 'Accept: text/html'
echo "      ^ cliente que aceita SO HTML: o erro continua vindo em JSON"
req GET /chamadas/999 404 "" -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
echo "      ^ cabecalho Accept de um navegador de verdade: erro em JSON"
req GET /chamadas 406 "" -H 'Accept: text/html'
echo "      ^ pedir a lista SO em HTML: 406, tambem em JSON (a API so fala JSON)"
req POST /entidades 400 '{"nome": "sem fechar a chave"'
req POST /entidades 415 "" -H 'Content-Type: text/plain' --data 'nome=Prefeitura'
req DELETE /entidades 405 ""
req OPTIONS /chamadas 200 "" -H 'Origin: https://colheai.vercel.app' -H 'Access-Control-Request-Method: POST'
echo "      CORS: $(grep -i '^access-control-allow-origin' /tmp/cabecalho.txt | tr -d '\r')"

echo
echo "RESULTADO: $OK de $N com o status esperado, $FALHA falha(s)."
