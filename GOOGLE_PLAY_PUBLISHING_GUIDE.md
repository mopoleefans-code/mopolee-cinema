# 📱 MOPOLEE CINEMA - Complete Google Play Publishing Guide

This guide provides step-by-step instructions to generate your signed Android App Bundle (`.aab`), set up your Google Play Developer Account, complete all store policy disclosures, and submit **MOPOLEE CINEMA** for official review.

---

## 📋 App Specifications Summary

* **App Name:** MOPOLEE CINEMA
* **Application ID (Package Name):** `com.aistudio.cinefilm.kxmpzq`
* **Version Code:** `4`
* **Version Name:** `2.2`
* **Target SDK:** `35` (Android 15 — Fully compliant with Google Play's latest requirements)
* **Minimum SDK:** `24` (Android 7.0 Nougat and above)
* **Primary Payment Gateway:** Paystack (`https://paystack.shop/pay/mjzyo3ejz0`)
* **Compiled Release Bundle:** `MOPOLEE_CINEMA_RELEASE.aab` (Located in the root project directory)

---

## 🛠️ Step 1: How to Generate the Release AAB

Google Play requires the **Android App Bundle (`.aab`)** format for all new app submissions (APKs are only used for direct manual installation/testing).

### Method A: Using Gradle via Terminal
Run the following command from the project root:

```bash
gradle :app:bundleRelease
```

* **Build Output Location:**
  `app/build/outputs/bundle/release/app-release.aab`
* The file is automatically copied to the root folder as **`MOPOLEE_CINEMA_RELEASE.aab`**.

### Method B: Generating a Custom Production Release Keystore (Optional)
If you want to generate your own custom private signing key instead of using the pre-configured project keystore:

```bash
keytool -genkey -v -keystore release.keystore -alias mopoleekey -keyalg RSA -keysize 2048 -validity 10000
```
> **Important:** Keep a secure backup of your keystore password, alias, and file. Google Play App Signing also safely manages your release key once uploaded.

---

## 👤 Step 2: Set Up a Google Play Developer Account

Google requires all developers to register an official account before publishing apps.

1. **Visit Google Play Console:**
   Go to [https://play.google.com/console/signup](https://play.google.com/console/signup).
2. **Sign In:**
   Log in with the Google Account you wish to associate with your developer identity (e.g., `mopoleefans@gmail.com`).
3. **Choose Account Type:**
   * **Individual:** If you are an independent creator or developer.
   * **Organization:** If you have a registered business (requires a Dun & Bradstreet / D-U-N-S number).
4. **Pay the One-Time Registration Fee:**
   * Google charges a one-time fee of **$25 USD** (payable by credit/debit card).
   * There are no recurring annual subscription fees for standard Google Play Developer accounts.
5. **Verify Your Identity:**
   * Upload a valid government-issued ID (Passport, National ID, or Driver's License) as prompted by Google.
   * Account approval typically takes between **few hours and 2 business days**.

---

## 🚀 Step 3: Create Your App in Google Play Console

1. Open the [Google Play Console Dashboard](https://play.google.com/console).
2. Click the blue **Create app** button in the top right.
3. Fill in the initial app details:
   * **App name:** `MOPOLEE CINEMA`
   * **Default language:** English (United States or United Kingdom)
   * **App or Game:** `App`
   * **Free or Paid:** `Free` (Content purchases are handled in-app via Paystack)
4. Check the boxes agreeing to the **Developer Program Policies** and **US Export Laws**.
5. Click **Create app**.

---

## 🎨 Step 4: Prepare Store Presence & Graphic Assets

Navigate to **Grow** > **Store presence** > **Main store listing**:

### 1. Text Descriptions
* **App name:** `MOPOLEE CINEMA` (Max 30 characters)
* **Short description:**
  > *Stream 32 episodes, classic horror vaults, and exclusive cinema with Paystack VIP passes.*
* **Full description:**
  > *Welcome to MOPOLEE CINEMA, the premier streaming destination for episodic drama, public domain cinema archives, and independent cinema releases.*
  >
  > *Key Features:*
  > * *32 Distinct Episodes: Watch George A. Romero's classic 1968 remastered "Night of the Living Dead" and curated cinema features.*
  > * *Instant Local & Streaming Playback: Adaptive video player with Big Screen (9:16) and Standard (16:9) cinematic modes.*
  > * *Seamless Paystack Checkout: Securely upgrade to the VIP Cinema Pass using Nigerian Naira (₦ NGN) via Debit/Credit Cards, Bank Transfer, or USSD at https://paystack.shop/pay/mjzyo3ejz0.*
  > * *Cinema Exchange Hub: Integrated customer care and interactive cinema discussion desks.*

### 2. Graphic Assets Specifications
* **App Icon:** `512 x 512 px` (32-bit PNG with alpha, max 1MB). *(Located in `app/src/main/res/drawable/mopolee_app_icon_*.jpg` or `cinefilm_icon.jpg`)*
* **Feature Graphic:** `1024 x 500 px` (JPG or 24-bit PNG, max 15MB). *(Located in `app/src/main/res/drawable/img_cinema_hero_banner.jpg`)*
* **Phone Screenshots:** At least 2 screenshots (min 320px, max 3840px, 16:9 or 9:16 aspect ratio).
* **Tablet Screenshots:** Optional but recommended for 7-inch and 10-inch devices.

### 3. Store Listing Contact Details
Go to **Grow** > **Store presence** > **Store settings**:
* **Email address:** `mopoleefans@gmail.com`
* **Website:** `https://paystack.shop/pay/mjzyo3ejz0`
* **Phone number:** `+234 708 819 3492` (Optional)

---

## 🔒 Step 5: Complete App Content & Data Safety Questionnaires

In the left menu, scroll down to **Policy and programs** > **App content**. You must complete all mandatory policy sections before Google allows rollout:

### 1. Privacy Policy
* Provide a valid URL hosting your privacy terms (e.g. your Render web portal or Paystack page: `https://paystack.shop/pay/mjzyo3ejz0`).

### 2. Ads Declaration
* Select: **No, my app does not contain ads**.

### 3. App Access
* Select: **All or some functionality is restricted** (Explain that Episodes 1 & 2 are free previews, and Episodes 3–32 require a VIP pass available via Paystack).

### 4. Content Rating (IARC)
* Start questionnaire -> Enter `mopoleefans@gmail.com` -> Select **Entertainment / Streaming**.
* Answer questions regarding violence (reference 1968 zombie horror themes).
* Submit to receive your official IARC Rating badge (e.g. Teen / 12+).

### 5. Target Audience
* Select: **13–15, 16–17, and 18+**.
* Declare that the app is **not intended for children under 13**.

### 6. Financial Features
* Select: **Payment processing / In-app purchases via external gateway**.
* Gateway: **Paystack (PCI-DSS compliant)**.

### 7. Data Safety Questionnaire (CRITICAL)
When prompted by Google Play, disclose the following exact answers based on the app's SDK audit:
* **Does your app collect or share user data?** -> **Yes**.
* **Is all of the user data encrypted in transit?** -> **Yes** (HTTPS / TLS 1.3).
* **Do you provide a way for users to request data deletion?** -> **Yes**.
* **Data Types Collected:**
  1. **Personal info -> Email address:**
     * Collected? **Yes** (for Paystack purchase receipts and VIP authorization).
     * Shared? **Yes** (Shared with Paystack to complete payment).
     * Purpose: **App functionality** & **Account management**.
  2. **Financial info -> Purchase history:**
     * Collected? **Yes** (Package name, amount in ₦ NGN, transaction reference).
     * Shared? **Yes** (with Paystack for payment verification).
     * Purpose: **App functionality**.
  3. **Credit / Debit Card Data:**
     * Collected by app? **No**. Card details are processed directly by Paystack's PCI-DSS compliant checkout; the mobile app never reads, stores, or sees card numbers.
* **Other SDKs (AndroidX, Jetpack Compose, Coil Image Loader):**
  * Disclose as **No data collected or shared**.

---

## 📦 Step 6: Upload the AAB & Submit for Review

### Recommended: Start with Internal Testing Track (Instant Feedback)
1. In the Google Play Console menu, go to **Testing** > **Internal testing**.
2. Click **Create new release**.
3. Under **App bundles**, click **Upload** and select:
   `MOPOLEE_CINEMA_RELEASE.aab`
4. **Release name:** `2.2 (4)`
5. **Release notes:**
   ```
   Initial release of MOPOLEE CINEMA:
   - 32 complete film episodes with independent streaming routes
   - Paystack checkout integration for VIP Cinema Pass (₦2,500 NGN)
   - Dark purple obsidian UI with neon cyber styling
   ```
6. Click **Next** > **Save**.
7. Add your email (`mopoleefans@gmail.com`) under **Testers** to install and test the build immediately on your Android phone via a private Google Play link.

### Submitting to Production (Public Release)
1. When ready for the public, go to **Release** > **Production**.
2. Click **Create new release** (or promote your internal release).
3. Upload or select `MOPOLEE_CINEMA_RELEASE.aab`.
4. Review release details and click **Next**.
5. Resolve any warnings (ensure all App Content sections have green checkmarks).
6. Click **Save** > **Review release** > **Start rollout to Production**.

---

## ⏱️ What Happens After Submission?

* **Review Timeline:** Google's automated checks and human review team typically review new app submissions within **24 to 72 hours** (first-time developer accounts may take up to 7 days).
* **Notification:** Google will send an email to `mopoleefans@gmail.com` confirming when **MOPOLEE CINEMA** is approved and live on Google Play.
* **Live Store URL:** Once published, your app will be publicly available at:
  `https://play.google.com/store/apps/details?id=com.aistudio.cinefilm.kxmpzq`

---

## 🔗 Useful Project Links
* **GitHub Repository:** [https://github.com/mopoleefans-code/mopolee-cinema](https://github.com/mopoleefans-code/mopolee-cinema)
* **Secondary Repository:** [https://github.com/mopoleefans-code/user-documentation](https://github.com/mopoleefans-code/user-documentation)
* **Live Paystack Storefront:** [https://paystack.shop/pay/mjzyo3ejz0](https://paystack.shop/pay/mjzyo3ejz0)
* **Render Deployment Blueprint:** `render.yaml` (Deployable at [render.com](https://render.com))
