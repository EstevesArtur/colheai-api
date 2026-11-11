#!/bin/bash
# ============================================================================
# ColheAí - API (GS 2026/2 - Domain Driven Design Using Java)
# Since Young - 1TDSPV-2026
# Artur Bianchini Esteves (RM569450) e Joao Vitor Oliveira Fontenele Martins (RM570783)
# ----------------------------------------------------------------------------
# TESTE DO 500 GENERICO: com a API no ar, o banco e desligado. A API tem de
# responder 500 com mensagem generica, sem SQL, sem nome de tabela e sem stack
# trace. Roda so no ambiente local, com o Oracle XE em Docker (container
# "oraclexe"). O pool espera ate 30 s por uma conexao antes de desistir.
# ============================================================================
API="${1:-http://localhost:8080}"
docker stop oraclexe > /dev/null && echo "Oracle desligado."
echo "GET /status (nao usa o banco):"
curl -s -w '  -> HTTP %{http_code}\n' "$API/status"
echo "GET /chamadas (precisa do banco; aguarda o tempo limite do pool):"
curl -s -w '  -> HTTP %{http_code}\n' "$API/chamadas"
docker start oraclexe > /dev/null && echo "Oracle ligado de novo."
