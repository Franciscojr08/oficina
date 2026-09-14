#!/usr/bin/env python3
"""
Simula o JWT que a Lambda `auth-token` (repo oficina-lambda) emite, para testar
a aplicacao localmente sem precisar da AWS. So biblioteca padrao do Python.

Uso:
    python3 gerar-token-cliente.py <cpf-so-digitos> [cliente_id] [nome] [segredo]

O segredo precisa ser o mesmo SECURITY_JWT_SECRET/jwt_secret usado nos dois lados.
"""
import base64
import hashlib
import hmac
import json
import sys
import time

SEGREDO_PADRAO = "jwt-docker-secret-123456789012345678901234567890"
EXPIRACAO_SEGUNDOS = 7200


def b64url(dados: bytes) -> str:
    return base64.urlsafe_b64encode(dados).rstrip(b"=").decode()


def gerar_token(cpf: str, cliente_id: int, nome: str, segredo: str) -> str:
    agora = int(time.time())
    header = {"alg": "HS256"}
    payload = {
        "sub": cpf,
        "tipo": "CLIENTE",
        "clienteId": cliente_id,
        "nome": nome,
        "role": "CLIENTE",
        "iat": agora,
        "exp": agora + EXPIRACAO_SEGUNDOS,
    }
    h = b64url(json.dumps(header, separators=(",", ":")).encode())
    p = b64url(json.dumps(payload, separators=(",", ":")).encode())
    conteudo = f"{h}.{p}".encode()
    assinatura = hmac.new(segredo.encode(), conteudo, hashlib.sha256).digest()
    return f"{h}.{p}.{b64url(assinatura)}"


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    cpf = sys.argv[1]
    cliente_id = int(sys.argv[2]) if len(sys.argv) > 2 else 1
    nome = sys.argv[3] if len(sys.argv) > 3 else "Cliente Teste"
    segredo = sys.argv[4] if len(sys.argv) > 4 else SEGREDO_PADRAO
    print(gerar_token(cpf, cliente_id, nome, segredo))
