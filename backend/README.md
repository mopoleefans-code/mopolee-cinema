# MOPOLEE CINEMA - Paystack Backend Integration Guide

This directory contains the production-ready backend service for **Paystack Payment Integration** for MOPOLEE CINEMA & EXCHANGE.

---

## 🔒 Security Principles

1. **Paystack Secret Key (`sk_test_...` or `sk_live_...`) is ONLY stored on the backend server.**
   - It is **NEVER** embedded into the Android mobile app APK.
2. **Every transaction is strictly verified on the server** using Paystack's official transaction verification endpoint (`https://api.paystack.co/transaction/verify/:reference`).
3. **Idempotency & Replay Protection**: Each reference is checked to prevent duplicate credit processing.
4. **All transactions are in Nigerian Naira (`₦` / NGN)**.

---

## 🚀 Required Backend Environment Variables

| Variable Name | Required | Example Value | Description |
|---|---|---|---|
| `PAYSTACK_SECRET_KEY` | **YES** | `YOUR_SECRET_KEY_HERE` | Your Paystack Secret Key from [Paystack Dashboard](https://dashboard.paystack.com/#/settings/developer) |
| `PAYSTACK_PUBLIC_KEY` | Optional | `YOUR_PUBLIC_KEY_HERE` | Your Paystack Public Key |
| `PORT` | Optional | `5000` | Port for the HTTP server (default: 5000) |
| `CORS_ORIGIN` | Optional | `*` | Allowed CORS origins for browser/API clients |

---

## 📦 How to Deploy in 3 Minutes (Free)

### Option A: Deploy to Render (Recommended - Free)
1. Fork or push this repository to GitHub.
2. Go to [https://render.com](https://render.com) and click **New Web Service**.
3. Select your repository.
4. Settings:
   - **Root Directory**: `backend`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
5. Under **Environment Variables**, add:
   - `PAYSTACK_SECRET_KEY` = your Paystack Secret Key
6. Click **Deploy Web Service**.
7. Copy your assigned URL (e.g., `https://mopolee-paystack-backend.onrender.com`).
8. Paste that URL into `.env` as `PAYSTACK_BACKEND_URL` or in the Android app Settings.

### Option B: Deploy to Railway (Free)
1. Go to [https://railway.app](https://railway.app).
2. Click **New Project** -> **Deploy from GitHub repo**.
3. Set the root directory to `backend`.
4. Add environment variable: `PAYSTACK_SECRET_KEY`.
5. Railway provides an instant HTTPS URL.

### Option C: Run Locally (For Local Testing with Android Emulator)
```bash
cd backend
npm install
cp .env.example .env
# Edit .env with your PAYSTACK_SECRET_KEY
npm start
```
*Note*: In Android Emulator, access your computer localhost using: `http://10.0.2.2:5000`

---

## 📡 API Endpoints

### 1. Health Check
- `GET /api/health`
- Response: Status, NGN currency, total transactions.

### 2. Initialize Payment
- `POST /api/paystack/initialize`
- Body:
  ```json
  {
    "email": "customer@gmail.com",
    "amountKobo": 250000,
    "packageTitle": "VIP Cinema Monthly Pass",
    "reference": "MOP_1730549231_ABCD"
  }
  ```
- Response:
  ```json
  {
    "success": true,
    "data": {
      "authorization_url": "https://checkout.paystack.com/...",
      "access_code": "...",
      "reference": "MOP_1730549231_ABCD"
    }
  }
  ```

### 3. Verify Payment
- `GET /api/paystack/verify/:reference`
- Response:
  ```json
  {
    "verified": true,
    "status": "SUCCESSFUL",
    "transaction": {
      "reference": "MOP_1730549231_ABCD",
      "amountNgn": 2500,
      "currency": "NGN",
      "packageTitle": "VIP Cinema Monthly Pass",
      "status": "SUCCESSFUL"
    }
  }
  ```

### 4. Webhook Listener
- `POST /api/paystack/webhook`
- Handles automatic notifications from Paystack with HMAC SHA512 signature verification.
- In Paystack Dashboard -> Settings -> API Keys & Webhooks:
  Set **Webhook URL** to: `https://your-backend-url/api/paystack/webhook`

### 5. Payment History
- `GET /api/paystack/history`
- Returns all processed transactions with status, amount in ₦, customer email, and timestamp.
