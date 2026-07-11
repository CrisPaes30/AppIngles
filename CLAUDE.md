# CLAUDE.md — Contexto operacional do AppIngles

Doc voltado pra mim (Claude) entre sessões — evita redescobrir infra/decisões já tomadas.
Para stack, estrutura de código e padrões de desenvolvimento, ver `PROJETO.md` e `README.md`.

## Ambiente de produção (homelab, `docker-compose.yml` na raiz)

Roda tudo em Docker no computador de casa do Cristian. Serviços:

| Serviço | Container | Papel |
|---|---|---|
| `oracle` | oracle-homelab | Oracle Free, schema `homelab` |
| `backend` | backend-homelab | Spring Boot, profile `prod`, sem porta exposta no host |
| `caddy` | caddy-homelab | Reverse proxy — dois listeners (ver abaixo) |
| `ngrok` | ngrok-homelab | **Exposição pública atual** — ver seção própria |
| `duckdns` | duckdns-homelab | Legado, mantido rodando mas não é mais o caminho de entrada (ver "DuckDNS quebrado") |
| `java17` | java17-homelab | Container solto pra rodar `mvn` manual, não faz parte do app |

Frontend **não** roda em Docker — é deployado na **Vercel** a partir do push em `main` (auto-deploy).

### Como aplicar mudanças

```bash
# Backend mudou (Java) → precisa rebuild da imagem:
docker compose build backend && docker compose up -d backend

# Só mudou Caddyfile/docker-compose.yml → recriar/restart o serviço afetado:
docker compose up -d <serviço>     # ou: docker compose restart <serviço>

# Frontend → só precisa git push (Vercel auto-deploy). Confirmar que pegou:
curl -s https://app-ingles-flax.vercel.app/ | grep -oE '/assets/index-[^"]+\.js'
# baixar esse bundle e grep pela env var esperada pra confirmar que o build novo foi publicado
```

`backend/target/` às vezes fica com dono `root` (sobra de build via Docker) — se `mvn` local reclamar de permissão, é isso; buildar via `docker run maven:...` em vez de `sudo chown` (sudo aqui não tem TTY interativo).

## Exposição pública: ngrok (não é mais DuckDNS)

**`DUCKDNS_SUBDOMAIN`/porta 80/443 no roteador não funciona de fora da rede.** Confirmado com teste de conectividade externa real (check-host.net, 3 nós em países diferentes: Canadá, Alemanha, Índia) — todos deram `Connection timed out` na porta 443. É CGNAT ou firewall do provedor: o roteador nunca recebe a conexão. Funciona só pra quem tá na mesma rede WiFi (hairpin NAT local mascara o problema — cuidado, testar "funciona pra mim" na mesma rede NÃO prova que funciona de fora).

Fix atual: **ngrok**, domínio estático grátis reservado na conta (não muda em restart):

```
https://interfilamentary-dax-unblamable.ngrok-free.dev
```

- `NGROK_AUTHTOKEN` / `NGROK_STATIC_DOMAIN` no `.env`.
- Serviço `ngrok` no compose conecta em `caddy:8090` (HTTP puro, sem TLS — o ngrok já termina TLS na borda).
- Caddy mantém o site HTTPS antigo (`{$DUCKDNS_SUBDOMAIN}.duckdns.org`) no `Caddyfile` só por não ter motivo pra tirar; ele não é mais o caminho de entrada real.

**Pegadinha do ngrok free tier — `ERR_NGROK_6024`:** requisições com User-Agent de navegador batem numa página HTML de aviso ("Visit Site") em vez de chegar no backend, porque o ngrok filtra por navegador real (`curl` sem UA de browser passa direto, e isso já mascarou esse bug uma vez nos meus testes manuais). Sem CORS na página de aviso → o axios do frontend reporta como `Network Error`. Fix: toda chamada HTTP do frontend manda o header `ngrok-skip-browser-warning: true` (já configurado em `frontend/src/services/api.ts` e `clientLog.service.ts` — se algum dia trocar de instância axios ou criar uma chamada fora dessas duas, replicar o header).

Se um dia migrar pra domínio próprio de verdade (Cloudflare Tunnel é a alternativa mais robusta — não depende de porta aberta nem de domínio estático de plano free): o domínio `appcursoingles.com.br` já foi comprado no registro.br (2026-07-10) mas nunca teve os nameservers apontados pra Cloudflare — ficou pra trás quando pivotamos pra ngrok por simplicidade.

## `.env` (raiz do repo, gitignored — nunca commitar)

Template em `.env.example`. Chaves relevantes que não são óbvias:

| Chave | Nota |
|---|---|
| `AI_PROVIDER` / `DICTIONARY_PROVIDER` | Só aceita `openai` — os providers `mock` foram **removidos do código** (não é só trocar o valor, a classe não existe mais). Ver "Providers de IA" abaixo. |
| `OPENAI_API_KEY` | Real, ativa. |
| `CORS_ALLOWED_ORIGINS` | Domínio da Vercel. Backend usa `WebMvcConfigurer` (`CorsConfig.java`), não `.cors()` do Spring Security. |
| `NGROK_AUTHTOKEN` / `NGROK_STATIC_DOMAIN` | Ver seção ngrok. |
| `DUCKDNS_*` | Legado, ver acima. |

Existe também um arquivo solto `backend/env` (sem ponto) que foi **apagado** — tinha uma cópia da API key da OpenAI e uma senha de banco desatualizada (`lecos`, que é a senha de dev local, não a de prod). Se reaparecer, é resquício antigo, não usar.

## Providers de IA — só OpenAI, mocks removidos

`MockAiExerciseService` e `MockDictionaryProvider` foram deletados (não existem mais no código). Motivo: usuário via retornos tipo `"(mock) tradução de..."` em produção porque o `.env` tinha `AI_PROVIDER=mock`. Agora:
- Sem a env var setada, o app **não sobe** (`AiExerciseService`/`DictionaryProvider` não teriam bean — fail-fast intencional, não usar fallback silencioso).
- `AbstractIntegrationTest` (testes de integração) usa `app.ai.provider=openai` via `@TestPropertySource` — se algum teste precisar rodar offline/sem custo de API, isso vai precisar de um mock de novo (reavaliar se vale a pena).

## Diagnóstico remoto: `POST /api/client-log`

Endpoint público (sem auth, `SecurityConfig` permitAll) criado porque um usuário externo não conseguia logar e a UI só mostrava "Erro inesperado" — sem esse endpoint, não dava pra saber por quê sem acesso ao celular da pessoa.

- Frontend manda automaticamente pra esse endpoint qualquer falha de login (`AuthContext.tsx`, `LoginPage.tsx`) **e qualquer chamada de API que falhe sem resposta** (`api.ts`, branch `apiMessage == null` — cobre rede/CORS/bundle desatualizado).
- Cai no log do backend com prefixo `[CLIENT]`. Pra acompanhar em tempo real:
  ```bash
  docker logs -f backend-homelab | grep CLIENT
  ```
- Foi exatamente esse log que revelou o bug do `ERR_NGROK_6024` (o diagnóstico mostrava `errorCode=ERR_NETWORK`, `requestSentNoResponse=true`, `baseURL` já correto — descartando VITE_API_URL errado e apontando pra bloqueio de rede/CORS específico daquela chamada).

## Bugs corrigidos que valem lembrar o padrão

**Recriar palavra deletada dava 500** (`VocabularyServiceImpl`): delete é soft-delete (`active=false`), mas a constraint `UNIQUE(USER_ID, WORD)` no Liquibase não considera `active`. `create()` agora procura um registro inativo com mesma word+user e **reativa** (reset dos campos) em vez de inserir — mesmo padrão replicado pra `ReviewSchedule`/`Progress` associados, que têm a mesma classe de problema. Se aparecer um bug parecido em outra entidade com soft-delete + unique constraint, é o mesmo padrão.

**Ícones PWA quebrados**: `frontend/public/icons/*.png` eram placeholders de 1×1px — o app aparecia como "instalável" mas a instalação falhava silenciosamente porque os ícones declarados no manifest não eram válidos. Ícones reais gerados via Pillow.

## Ao testar via API diretamente (curl/sqlplus)

Se precisar criar usuário/dado de teste direto no banco pra validar algo (ex.: não tem fluxo de login por email/senha, só Google OAuth via Firebase — não dá pra automatizar signup), **limpar depois**:

```bash
docker exec -i oracle-homelab sqlplus -S homelab/<ORACLE_APP_PASSWORD>@//localhost:1521/FREEPDB1
```

Já aconteceu de deixar um `qa-test-*@example.com` esquecido no banco de prod — sempre `DELETE` (progress, review_schedules, study_sessions, vocabulary_words, users, nessa ordem por FK) e `COMMIT` no final do teste.
