from fastapi import FastAPI

from app.routes.accounts import router as accounts_router


app = FastAPI(title="Dot Chat Admin Panel Service", version="0.1.0")
app.include_router(accounts_router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
