-- Executar conectado como SYSTEM no PDB XEPDB1
-- sqlplus SYSTEM/sua_senha@//localhost:1521/XEPDB1

CREATE USER english_memory IDENTIFIED BY english_memory;
GRANT CONNECT, RESOURCE TO english_memory;
GRANT UNLIMITED TABLESPACE TO english_memory;

-- Verificar se foi criado
SELECT username, account_status FROM dba_users WHERE username = 'ENGLISH_MEMORY';
