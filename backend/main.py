"""
MOPOLEE CINEMA & EXCHANGE - Paystack Backend Server (Python FastAPI)

Implements:
1. POST /api/paystack/initialize
2. GET /api/paystack/verify/{reference}
3. POST /api/paystack/webhook
4. GET /api/paystack/history
"""

import os
import hmac
import hashlib
import time
from typing import Optional, Dict, Any, List
from fastapi import FastAPI, HTTPException, Header, Request, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import httpx

app = FastAPI(title="Mopolee Paystack Backend Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

PAYSTACK_SECRET_KEY = os.getenv("PAYSTACK_SECRET_KEY", "")

# In-memory store (use DB in production)
transaction_ledger: Dict[str, Dict[str, Any]] = {}
processed_references = set()

class InitPaymentRequest(BaseModel):
    email: str
    amountKobo: int
    reference: Optional[str] = None
    packageTitle: Optional[str] = "VIP Cinema Pass"
    metadata: Optional[Dict[str, Any]] = None

@app.get("/api/health")
def health_check():
    return {
        "status": "ok",
        "service": "Mopolee Paystack Backend Service (Python)",
        "currency": "NGN",
        "hasSecretKey": bool(PAYSTACK_SECRET_KEY and "your_paystack_secret_key" not in PAYSTACK_SECRET_KEY),
        "totalTransactions": len(transaction_ledger)
    }

@app.post("/api/paystack/initialize")
async def initialize_payment(req: InitPaymentRequest):
    if not req.email or req.amountKobo <= 0:
        raise HTTPException(status_code=400, detail="Valid email and amount in Kobo required.")

    ref = req.reference or f"MOP_{int(time.time())}_{os.urandom(3).hex().upper()}"

    if ref in processed_references:
        raise HTTPException(status_code=409, detail="Payment reference already processed.")

    # If no key, run safe sandbox mode
    if not PAYSTACK_SECRET_KEY or "your_paystack_secret_key" in PAYSTACK_SECRET_KEY:
        data = {
            "authorization_url": f"https://checkout.paystack.com/preview/{ref}",
            "access_code": f"mock_py_{int(time.time())}",
            "reference": ref
        }
        transaction_ledger[ref] = {
            "reference": ref,
            "email": req.email,
            "amountKobo": req.amountKobo,
            "amountNgn": req.amountKobo / 100,
            "packageTitle": req.packageTitle,
            "status": "PENDING",
            "statusMessage": "Payment initialized, awaiting authorization",
            "createdAt": time.time()
        }
        return {"success": True, "message": "Initialized in Test Sandbox Mode", "data": data}

    async with httpx.AsyncClient() as client:
        resp = await client.post(
            "https://api.paystack.co/transaction/initialize",
            headers={"Authorization": f"Bearer {PAYSTACK_SECRET_KEY}"},
            json={
                "email": req.email,
                "amount": req.amountKobo,
                "currency": "NGN",
                "reference": ref,
                "metadata": {"packageTitle": req.packageTitle, "app": "Mopolee Cinema"}
            }
        )
        res_data = resp.json()
        if not resp.is_success or not res_data.get("status"):
            raise HTTPException(status_code=resp.status_code, detail=res_data.get("message", "Paystack initialization failed"))

        transaction_ledger[ref] = {
            "reference": ref,
            "email": req.email,
            "amountKobo": req.amountKobo,
            "amountNgn": req.amountKobo / 100,
            "packageTitle": req.packageTitle,
            "status": "PENDING",
            "statusMessage": "Payment initialized",
            "createdAt": time.time()
        }
        return {"success": True, "message": "Payment initialized successfully", "data": res_data.get("data")}

@app.get("/api/paystack/verify/{reference}")
async def verify_transaction(reference: str, expectedAmount: Optional[int] = Query(None)):
    if reference in transaction_ledger and transaction_ledger[reference].get("status") == "SUCCESSFUL":
        return {
            "verified": True,
            "status": "SUCCESSFUL",
            "isDuplicate": True,
            "message": "Payment previously verified and credited.",
            "transaction": transaction_ledger[reference]
        }

    if not PAYSTACK_SECRET_KEY or "your_paystack_secret_key" in PAYSTACK_SECRET_KEY:
        existing = transaction_ledger.get(reference, {})
        tx = {
            "reference": reference,
            "email": existing.get("email", "mopoleefans@gmail.com"),
            "amountKobo": existing.get("amountKobo", 250000),
            "amountNgn": existing.get("amountKobo", 250000) / 100,
            "currency": "NGN",
            "packageTitle": existing.get("packageTitle", "VIP Cinema Pass"),
            "status": "SUCCESSFUL",
            "statusMessage": "Verified in Python Test Sandbox",
            "channel": "card",
            "gatewayResponse": "Approved",
            "verifiedAt": time.time()
        }
        transaction_ledger[reference] = tx
        processed_references.add(reference)
        return {"verified": True, "status": "SUCCESSFUL", "message": "Payment verified", "transaction": tx}

    async with httpx.AsyncClient() as client:
        resp = await client.get(
            f"https://api.paystack.co/transaction/verify/{reference}",
            headers={"Authorization": f"Bearer {PAYSTACK_SECRET_KEY}"}
        )
        res_data = resp.json()
        if not resp.is_success or not res_data.get("status"):
            raise HTTPException(status_code=400, detail=res_data.get("message", "Verification failed"))

        tx_data = res_data.get("data", {})
        if tx_data.get("status") != "success":
            return {"verified": False, "status": "FAILED", "message": f"Payment incomplete: {tx_data.get('gateway_response')}"}

        if tx_data.get("currency") != "NGN":
            raise HTTPException(status_code=400, detail="Currency must be NGN")

        if expectedAmount and tx_data.get("amount", 0) < expectedAmount:
            raise HTTPException(status_code=400, detail="Amount paid is less than expected package price")

        processed_references.add(reference)
        tx = {
            "reference": tx_data.get("reference"),
            "email": tx_data.get("customer", {}).get("email"),
            "amountKobo": tx_data.get("amount"),
            "amountNgn": tx_data.get("amount", 0) / 100,
            "currency": tx_data.get("currency"),
            "packageTitle": tx_data.get("metadata", {}).get("packageTitle", "VIP Cinema Pass"),
            "status": "SUCCESSFUL",
            "statusMessage": "Verified via Paystack Server API",
            "channel": tx_data.get("channel", "card"),
            "gatewayResponse": tx_data.get("gateway_response", "Approved"),
            "paidAt": tx_data.get("paid_at"),
            "verifiedAt": time.time()
        }
        transaction_ledger[reference] = tx
        return {"verified": True, "status": "SUCCESSFUL", "message": "Payment verified successfully", "transaction": tx}

@app.post("/api/paystack/webhook")
async def paystack_webhook(request: Request, x_paystack_signature: Optional[str] = Header(None)):
    body = await request.body()
    if PAYSTACK_SECRET_KEY and x_paystack_signature:
        computed = hmac.new(PAYSTACK_SECRET_KEY.encode(), body, hashlib.sha512).hexdigest()
        if computed != x_paystack_signature:
            raise HTTPException(status_code=401, detail="Invalid signature")

    event = await request.json()
    if event.get("event") == "charge.success":
        data = event.get("data", {})
        ref = data.get("reference")
        if ref and ref not in processed_references:
            processed_references.add(ref)
            transaction_ledger[ref] = {
                "reference": ref,
                "email": data.get("customer", {}).get("email"),
                "amountKobo": data.get("amount"),
                "amountNgn": data.get("amount", 0) / 100,
                "currency": data.get("currency"),
                "status": "SUCCESSFUL",
                "statusMessage": "Verified via Paystack Webhook",
                "verifiedAt": time.time()
            }
    return {"status": "success"}

@app.get("/api/paystack/history")
def payment_history():
    return {"success": True, "transactions": list(transaction_ledger.values())}
