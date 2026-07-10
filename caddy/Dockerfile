# Build customizado do Caddy com o plugin DNS do DuckDNS, necessário para o
# desafio ACME DNS-01 (evita depender de conectividade de entrada na porta
# 80/443 para emitir certificado — só precisa alcançar a API do DuckDNS).
FROM caddy:2-builder-alpine AS builder
RUN xcaddy build --with github.com/caddy-dns/duckdns

FROM caddy:2-alpine
COPY --from=builder /usr/bin/caddy /usr/bin/caddy
