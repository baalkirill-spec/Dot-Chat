from __future__ import annotations

import hashlib
import secrets


class VerificationCodeService:
    def __init__(self, code_length: int = 5) -> None:
        self._code_length = code_length

    def generate_code(self) -> str:
        lower_bound = 10 ** (self._code_length - 1)
        upper_bound = (10**self._code_length) - 1
        return str(secrets.randbelow(upper_bound - lower_bound) + lower_bound)

    def hash_code(self, session_id: str, code: str) -> str:
        digest = hashlib.sha256()
        digest.update(f"{session_id}:{code}".encode("utf-8"))
        return digest.hexdigest()

    def verify(self, session_id: str, code: str, code_hash: str) -> bool:
        return secrets.compare_digest(self.hash_code(session_id, code), code_hash)
