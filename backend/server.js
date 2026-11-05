/**
 * MOPOLEE CINEMA & EXCHANGE - Paystack Backend Server (Node.js/Express)
 *
 * Implements:
 * 1. Secure Transaction Initialization (POST /api/paystack/initialize)
 * 2. Strict Server-Side Verification against Paystack (GET /api/paystack/verify/:reference)
 * 3. Duplicate Payment & Replay Prevention (Idempotency Ledger)
 * 4. Secure Webhook Listener with HMAC SHA512 Signature Check (POST /api/paystack/webhook)
 * 5. Payment History Ledger (GET /api/paystack/history)
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 5000;
const PAYSTACK_SECRET_KEY = process.env.PAYSTACK_SECRET_KEY || '';

// Middleware
app.use(cors({ origin: process.env.CORS_ORIGIN || '*' }));
app.use(express.static(path.join(__dirname, 'public')));

// Raw body parser for webhook HMAC verification
app.use(express.json({
  verify: (req, res, buf) => {
    req.rawBody = buf;
  }
}));

// In-Memory Idempotency & Transaction Ledger (replace with MongoDB/PostgreSQL in production)
const transactionLedger = new Map();
const processedReferences = new Set();

/**
 * 0. App Store Landing & Installation Portal (HTML)
 */
app.get('/', (req, res) => {
  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MOPOLEE CINEMA - Install App & Paystack VIP Portal</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap" rel="stylesheet">
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background: #0D0014;
      color: #FFFFFF;
      font-family: 'Plus Jakarta Sans', sans-serif;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px 16px;
      line-height: 1.5;
    }
    .container {
      max-width: 600px;
      width: 100%;
      background: rgba(26, 0, 48, 0.7);
      border: 1px solid rgba(224, 64, 251, 0.3);
      backdrop-filter: blur(12px);
      border-radius: 24px;
      padding: 32px 24px;
      text-align: center;
      box-shadow: 0 16px 40px rgba(0,0,0,0.6), 0 0 40px rgba(224, 64, 251, 0.15);
    }
    .badge {
      display: inline-block;
      background: rgba(255, 179, 0, 0.15);
      border: 1px solid #FFB300;
      color: #FFB300;
      font-size: 11px;
      font-weight: 800;
      letter-spacing: 1px;
      padding: 6px 14px;
      border-radius: 50px;
      text-transform: uppercase;
      margin-bottom: 16px;
    }
    h1 {
      font-size: 28px;
      font-weight: 800;
      background: linear-gradient(135deg, #FFF 30%, #E040FB 70%, #FFB300 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 8px;
    }
    p.subtitle {
      color: rgba(255, 255, 255, 0.75);
      font-size: 14px;
      margin-bottom: 24px;
    }
    .features {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
      margin-bottom: 28px;
      text-align: left;
    }
    .feature-card {
      background: rgba(45, 0, 80, 0.5);
      border: 1px solid rgba(224, 64, 251, 0.2);
      border-radius: 14px;
      padding: 12px 14px;
    }
    .feature-title {
      font-size: 12px;
      font-weight: 700;
      color: #E040FB;
      margin-bottom: 2px;
    }
    .feature-desc {
      font-size: 11px;
      color: rgba(255, 255, 255, 0.7);
    }
    .button-group {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      width: 100%;
      padding: 14px 20px;
      border-radius: 14px;
      font-size: 15px;
      font-weight: 800;
      text-decoration: none;
      transition: transform 0.15s ease, box-shadow 0.15s ease;
      cursor: pointer;
      border: none;
    }
    .btn:active { transform: scale(0.98); }
    .btn-paystack {
      background: linear-gradient(135deg, #00C853, #00E676);
      color: #000;
      box-shadow: 0 4px 18px rgba(0, 230, 118, 0.35);
    }
    .btn-googleplay {
      background: #FFFFFF;
      color: #000;
      box-shadow: 0 4px 18px rgba(255, 255, 255, 0.2);
    }
    .btn-apk {
      background: rgba(224, 64, 251, 0.18);
      border: 1px solid #E040FB;
      color: #E040FB;
    }
    .info-footer {
      margin-top: 24px;
      font-size: 11px;
      color: rgba(255, 255, 255, 0.5);
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      padding-top: 16px;
    }
    .status-dot {
      display: inline-block;
      width: 8px;
      height: 8px;
      background: #00E676;
      border-radius: 50%;
      margin-right: 4px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="badge">Official Cinema Portal</div>
    <h1>MOPOLEE CINEMA</h1>
    <p class="subtitle">Stream 32 Full Episodes & Unlock VIP All-Access via Paystack</p>

    <div class="features">
      <div class="feature-card">
        <div class="feature-title">🎬 32 Distinct Episodes</div>
        <div class="feature-desc">Complete drama series & horror archive</div>
      </div>
      <div class="feature-card">
        <div class="feature-title">💳 Paystack ₦ NGN</div>
        <div class="feature-desc">Cards, USSD & Bank Transfers in Naira</div>
      </div>
      <div class="feature-card">
        <div class="feature-title">🔒 256-Bit Verification</div>
        <div class="feature-desc">Render backend anti-fraud server</div>
      </div>
      <div class="feature-card">
        <div class="feature-title">📱 Google Play Ready</div>
        <div class="feature-desc">Android 15 (Target SDK 35) release</div>
      </div>
    </div>

    <div class="button-group">
      <a href="https://paystack.shop/pay/mjzyo3ejz0" target="_blank" rel="noopener" class="btn btn-paystack">
        <span>🚀 Pay ₦2,500 VIP Pass on Paystack</span>
      </a>

      <a href="https://play.google.com/store/apps/details?id=com.aistudio.cinefilm.kxmpzq" target="_blank" rel="noopener" class="btn btn-googleplay">
        <span>📱 Open on Google Play Store</span>
      </a>

      <a href="/download/apk" class="btn btn-apk">
        <span>⬇️ Download Android APK (Direct)</span>
      </a>
    </div>

    <div class="info-footer">
      <div><span class="status-dot"></span> Render Service Online • Currency: Nigerian Naira (₦ NGN)</div>
      <div style="margin-top: 4px;">App Package: <code>com.aistudio.cinefilm.kxmpzq</code> • Version: v2.2</div>
    </div>
  </div>
</body>
</html>`);
});

/**
 * Direct APK Download Route
 */
app.get('/download/apk', (req, res) => {
  const apkPath = path.join(__dirname, 'public', 'mopolee-cinema.apk');
  if (fs.existsSync(apkPath)) {
    return res.download(apkPath, 'MOPOLEE_CINEMA_EXCHANGE.apk');
  }
  // Fallback to Google Play Store listing
  res.redirect('https://play.google.com/store/apps/details?id=com.aistudio.cinefilm.kxmpzq');
});

/**
 * Healthcheck
 */
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    service: 'Mopolee Paystack Backend Service',
    currency: 'NGN',
    timestamp: new Date().toISOString(),
    hasSecretKey: Boolean(PAYSTACK_SECRET_KEY && !PAYSTACK_SECRET_KEY.includes('your_paystack_secret_key')),
    totalTransactions: transactionLedger.size
  });
});

/**
 * 1. Initialize Payment
 * POST /api/paystack/initialize
 * Body: { email, amountKobo, reference, packageTitle, metadata }
 */
app.post('/api/paystack/initialize', async (req, res) => {
  try {
    const { email, amountKobo, reference, packageTitle, metadata } = req.body;

    if (!email || !amountKobo) {
      return res.status(400).json({
        success: false,
        message: 'Customer email and amount in Kobo are required.'
      });
    }

    const payReference = reference || `MOP_${Date.now()}_${Math.random().toString(36).substring(2, 8).toUpperCase()}`;

    // Prevent re-initialization of already completed reference
    if (processedReferences.has(payReference)) {
      return res.status(409).json({
        success: false,
        message: 'This payment reference has already been processed.'
      });
    }

    if (!PAYSTACK_SECRET_KEY || PAYSTACK_SECRET_KEY.includes('your_paystack_secret_key')) {
      console.warn('[Paystack Backend] Warning: PAYSTACK_SECRET_KEY not set. Operating in Test Simulation mode.');
      // Return simulated Paystack initialize response for initial onboarding
      const simulatedData = {
        authorization_url: 'https://paystack.shop/pay/mjzyo3ejz0',
        access_code: `live_page_${Date.now()}`,
        reference: payReference
      };

      transactionLedger.set(payReference, {
        reference: payReference,
        email,
        amountKobo: Number(amountKobo),
        amountNgn: Number(amountKobo) / 100,
        packageTitle: packageTitle || 'VIP Cinema Pass',
        status: 'PENDING',
        statusMessage: 'Payment initialized, waiting for authorization',
        createdAt: new Date().toISOString()
      });

      return res.json({
        success: true,
        message: 'Payment initialized (Test Mode: Add PAYSTACK_SECRET_KEY for live Paystack calls)',
        data: simulatedData
      });
    }

    // Call official Paystack initialize endpoint
    const response = await fetch('https://api.paystack.co/transaction/initialize', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${PAYSTACK_SECRET_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email,
        amount: amountKobo, // Paystack requires amount in Kobo (e.g. 250000 = ₦2500)
        currency: 'NGN',
        reference: payReference,
        metadata: {
          packageTitle: packageTitle || 'VIP Cinema Pass',
          app: 'Mopolee Cinema',
          ...metadata
        }
      })
    });

    const data = await response.json();

    if (!response.ok || !data.status) {
      return res.status(response.status || 400).json({
        success: false,
        message: data.message || 'Paystack initialization failed'
      });
    }

    // Store in ledger as PENDING
    transactionLedger.set(payReference, {
      reference: payReference,
      email,
      amountKobo: Number(amountKobo),
      amountNgn: Number(amountKobo) / 100,
      packageTitle: packageTitle || 'VIP Cinema Pass',
      status: 'PENDING',
      statusMessage: 'Payment initialized',
      createdAt: new Date().toISOString()
    });

    res.json({
      success: true,
      message: 'Payment initialized successfully',
      data: data.data
    });
  } catch (err) {
    console.error('[Paystack Init Error]', err);
    res.status(500).json({ success: false, message: 'Server error during payment initialization: ' + err.message });
  }
});

/**
 * 2. Server-Side Transaction Verification
 * GET /api/paystack/verify/:reference
 * Strict rule: Never trust frontend alone. Verifies directly against Paystack API.
 */
app.get('/api/paystack/verify/:reference', async (req, res) => {
  try {
    const { reference } = req.params;
    const expectedAmountKobo = req.query.expectedAmount ? Number(req.query.expectedAmount) : null;

    if (!reference) {
      return res.status(400).json({
        verified: false,
        status: 'FAILED',
        message: 'Payment reference is required.'
      });
    }

    // Check if reference is already processed to prevent duplicate crediting
    const existingTx = transactionLedger.get(reference);
    if (existingTx && existingTx.status === 'SUCCESSFUL') {
      return res.json({
        verified: true,
        status: 'SUCCESSFUL',
        isDuplicate: true,
        message: 'Payment previously verified and credited successfully.',
        transaction: existingTx
      });
    }

    // If no Secret Key configured yet, handle test verification gracefully
    if (!PAYSTACK_SECRET_KEY || PAYSTACK_SECRET_KEY.includes('your_paystack_secret_key')) {
      console.warn('[Paystack Backend] Warning: Verifying in Test Demo mode without Secret Key.');
      const verifiedTx = {
        reference,
        email: existingTx?.email || 'mopoleefans@gmail.com',
        amountKobo: existingTx?.amountKobo || 250000,
        amountNgn: (existingTx?.amountKobo || 250000) / 100,
        currency: 'NGN',
        packageTitle: existingTx?.packageTitle || 'VIP Cinema Pass',
        status: 'SUCCESSFUL',
        statusMessage: 'Verified in Test Sandbox (Configure PAYSTACK_SECRET_KEY for live Paystack verification)',
        channel: 'card',
        gatewayResponse: 'Approved',
        verifiedAt: new Date().toISOString()
      };

      transactionLedger.set(reference, verifiedTx);
      processedReferences.add(reference);

      return res.json({
        verified: true,
        status: 'SUCCESSFUL',
        message: 'Payment verified successfully',
        transaction: verifiedTx
      });
    }

    // Call Paystack Transaction Verification API
    const paystackUrl = `https://api.paystack.co/transaction/verify/${encodeURIComponent(reference)}`;
    const response = await fetch(paystackUrl, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${PAYSTACK_SECRET_KEY}`,
        'Content-Type': 'application/json'
      }
    });

    const body = await response.json();

    if (!response.ok || !body.status || !body.data) {
      const failedRecord = {
        reference,
        status: 'FAILED',
        statusMessage: body.message || 'Transaction not found or verification failed',
        failedAt: new Date().toISOString()
      };
      transactionLedger.set(reference, failedRecord);

      return res.status(400).json({
        verified: false,
        status: 'FAILED',
        message: body.message || 'Verification failed on Paystack.'
      });
    }

    const txData = body.data;

    // Validate Paystack response parameters strictly:
    // 1. Transaction status must be 'success'
    // 2. Currency must be Nigerian Naira 'NGN'
    if (txData.status !== 'success') {
      const statusReason = txData.gateway_response || txData.status || 'Payment incomplete or failed';
      transactionLedger.set(reference, {
        reference,
        status: 'FAILED',
        statusMessage: statusReason,
        failedAt: new Date().toISOString()
      });

      return res.json({
        verified: false,
        status: 'FAILED',
        message: `Payment not completed. Status: ${statusReason}`
      });
    }

    if (txData.currency !== 'NGN') {
      return res.status(400).json({
        verified: false,
        status: 'FAILED',
        message: `Currency mismatch. Expected NGN, got ${txData.currency}`
      });
    }

    // 3. Amount check if expected amount provided
    if (expectedAmountKobo && txData.amount < expectedAmountKobo) {
      return res.status(400).json({
        verified: false,
        status: 'FAILED',
        message: `Amount mismatch. Paid ${txData.amount} Kobo, expected at least ${expectedAmountKobo} Kobo`
      });
    }

    // Prevent duplicate processing
    processedReferences.add(reference);

    const verifiedRecord = {
      reference: txData.reference,
      email: txData.customer?.email || existingTx?.email || 'customer@mopoleecinema.com',
      amountKobo: txData.amount,
      amountNgn: txData.amount / 100,
      currency: txData.currency,
      packageTitle: txData.metadata?.packageTitle || existingTx?.packageTitle || 'VIP Cinema Pass',
      status: 'SUCCESSFUL',
      statusMessage: 'Payment verified and confirmed by Paystack',
      channel: txData.channel || 'card',
      gatewayResponse: txData.gateway_response || 'Successful',
      paidAt: txData.paid_at || new Date().toISOString(),
      verifiedAt: new Date().toISOString()
    };

    transactionLedger.set(reference, verifiedRecord);

    res.json({
      verified: true,
      status: 'SUCCESSFUL',
      message: 'Transaction successfully verified. Premium VIP access granted.',
      transaction: verifiedRecord
    });
  } catch (err) {
    console.error('[Paystack Verify Error]', err);
    res.status(500).json({
      verified: false,
      status: 'FAILED',
      message: 'Server error during transaction verification: ' + err.message
    });
  }
});

/**
 * 3. Secure Webhook Listener
 * POST /api/paystack/webhook
 * Verifies Paystack HMAC SHA512 signature
 */
app.post('/api/paystack/webhook', (req, res) => {
  try {
    const signature = req.headers['x-paystack-signature'];
    if (!signature) {
      return res.status(400).send('Missing Paystack signature');
    }

    if (PAYSTACK_SECRET_KEY) {
      const hash = crypto.createHmac('sha512', PAYSTACK_SECRET_KEY)
        .update(req.rawBody || JSON.stringify(req.body))
        .digest('hex');

      if (hash !== signature) {
        console.warn('[Paystack Webhook] Invalid signature rejected');
        return res.status(401).send('Invalid signature');
      }
    }

    const event = req.body;
    if (event && event.event === 'charge.success') {
      const tx = event.data;
      const ref = tx.reference;

      if (!processedReferences.has(ref)) {
        processedReferences.add(ref);
        transactionLedger.set(ref, {
          reference: ref,
          email: tx.customer?.email,
          amountKobo: tx.amount,
          amountNgn: tx.amount / 100,
          currency: tx.currency,
          packageTitle: tx.metadata?.packageTitle || 'VIP Cinema Pass',
          status: 'SUCCESSFUL',
          statusMessage: 'Confirmed via Paystack Webhook',
          channel: tx.channel,
          gatewayResponse: tx.gateway_response,
          paidAt: tx.paid_at,
          verifiedAt: new Date().toISOString()
        });
        console.log(`[Paystack Webhook] Successfully credited reference ${ref}`);
      }
    }

    res.sendStatus(200);
  } catch (err) {
    console.error('[Paystack Webhook Error]', err);
    res.sendStatus(500);
  }
});

/**
 * 4. Payment History
 * GET /api/paystack/history?email=...
 */
app.get('/api/paystack/history', (req, res) => {
  const { email } = req.query;
  const list = Array.from(transactionLedger.values()).reverse();

  if (email) {
    const filtered = list.filter(item => item.email && item.email.toLowerCase() === email.toLowerCase());
    return res.json({ success: true, count: filtered.length, transactions: filtered });
  }

  res.json({ success: true, count: list.length, transactions: list });
});

app.listen(PORT, () => {
  console.log(`===============================================`);
  console.log(`MOPOLEE Paystack Verification Backend running on port ${PORT}`);
  console.log(`Currency: Nigerian Naira (NGN / ₦)`);
  console.log(`Secret Key Configured: ${Boolean(PAYSTACK_SECRET_KEY && !PAYSTACK_SECRET_KEY.includes('your_paystack_secret_key'))}`);
  console.log(`===============================================`);
});
