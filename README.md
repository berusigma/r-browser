<div align="center">

<img src="docs/assets/rbrowser_hero_banner.jpg" alt="R Browser Banner" width="100%" style="border-radius: 12px; margin-bottom: 20px;" />

# ⚡ R Browser
### *Next-Gen Privacy & Developer Web Engine for Android*

[![Android CI](https://github.com/berusigma/r-browser/actions/workflows/build-apk.yml/badge.svg)](https://github.com/berusigma/r-browser/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min_SDK-24-00E676?logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target_SDK-36-00E676?logo=android&logoColor=white)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

<p align="center">
  <b>R Browser</b> is a mobile web browser crafted for developers, security researchers, and privacy enthusiasts. Featuring an integrated DevTools suite, real DNS-over-HTTPS (DoH) engine, proxy rotator, dynamic header injection, and ad & tracker blocker wrapped in a Cyberpunk Glassmorphic interface.
</p>

[✨ Key Features](#-key-features) •
[🛠 Architecture & Tech Stack](#-architecture--tech-stack) •
[🚀 Getting Started](#-getting-started) •
[🤖 Automated CI/CD](#-automated-cicd) •
[📱 How to Download APK](#-how-to-download-apk)

---

</div>

## 🌟 Key Features

### 🛡️ 1. Real DNS-over-HTTPS (DoH) Engine
* **RFC 8484 / JSON DoH Client**: Real DNS queries directly resolving through top-tier encrypted resolvers:
  * 🌐 **Cloudflare** (`1.1.1.1` / `cloudflare-dns.com`)
  * 🛡️ **Quad9** (`9.9.9.9` / `dns.quad9.net`)
  * 🚫 **AdGuard DNS** (`dns.adguard.com`)
  * 🔍 **Google DNS** (`8.8.8.8` / `dns.google`)
* **Live Query Verification**: Displays live resolved IP addresses, latency, and resolver status directly in the user interface.

### 🔬 2. Integrated DevTools Inspector
* **Live Network Traffic Monitor**: Real-time HTTP/HTTPS request interception with HTTP status codes, payload sizes, MIME types, request/response headers, and timing.
* **cURL Exporter & Replay Engine**: Export any captured network request directly into cURL syntax or replay it on the fly.
* **Storage Inspector**: Full inspection and clearing of Cookies, LocalStorage, SessionStorage, and IndexedDB.
* **Interactive Console**: JavaScript console output streamer capturing logs, warnings, and errors directly from web viewports.
* **Tools Suite**: Web asset extractor, Server DNS lookup, HTML source viewer, and responsive viewport tester.

### 🛡️ 3. Security & Privacy Shield
* **Built-in Ad & Tracker Blocker**: Real-time filtering blocking third-party ads, analytic trackers, and invasive fingerprinting scripts.
* **API Key Leak Detector**: Automatic heuristics engine scanning outbound network queries for accidentally exposed API keys and sensitive tokens.
* **Custom Headers Injector**: Pre-configured and customizable HTTP header injection (`DNT`, `Sec-GPC`, `X-Forwarded-For`, `X-Requested-With`).
* **Proxy Configuration**: Supports HTTP and SOCKS5 proxy routing with instant connection toggle.
* **User-Agent Profile Switcher**: One-tap spoofing between Mobile Chrome, Safari iOS, Desktop Firefox, Desktop Chrome, and custom strings.

### ⚡ 4. Modern Browsing Experience
* **Search History Cache & Autocomplete**: Instant dropdown overlay displaying search history suggestions and instant URL navigation.
* **Reader Mode & Dark Reader**: Distraction-free article extraction and dynamic dark mode engine for all websites.
* **Offline Page Archives**: Save full web pages locally for offline reading and research.
* **Cyberpunk Glassmorphism UI**: Smooth animations, dark neon color palette, fluid tab management, and responsive edge-to-edge layout.

---

## 🛠 Architecture & Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Language** | Kotlin 2.2.10 |
| **UI Framework** | Jetpack Compose with Material 3 & Compose Animation |
| **Architecture** | MVVM (Model-View-ViewModel) + Android Architecture Components |
| **Asynchronous Engine** | Kotlin Coroutines & Flow (StateFlow / SharedFlow) |
| **Networking & HTTP** | OkHttp 4.10, Retrofit 2.12, Moshi |
| **Database & Cache** | Room Database 2.7.0 (KSP), SharedPreferences |
| **Web Engine** | AndroidX WebKit 1.12.1 with Chrome DevTools Protocol hooks |
| **Build System** | Gradle 9.3.1 with Android Gradle Plugin (AGP) 9.1.1 |

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio Ladybug / Meerkat** (or newer)
* **JDK 17** or **JDK 21**
* **Android SDK** with Platform API 36 (Android 16) & Build Tools

### Clone & Open
```bash
# Clone the repository
git clone https://github.com/berusigma/r-browser.git

# Navigate to project directory
cd r-browser

# Build debug APK locally
./gradlew assembleDebug
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 Automated CI/CD

R Browser includes a preconfigured **GitHub Actions CI/CD pipeline** located in [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml).

Every push to `main` or manual trigger via **Workflow Dispatch** will:
1. Initialize JDK 17 & Android SDK 36.
2. Verify Gradle wrapper integrity.
3. Compile and build the signed Debug APK (`./gradlew assembleDebug`).
4. Upload the built APK as a downloadable GitHub Actions artifact.

---

## 📱 How to Download APK

1. Go to the **[Actions](https://github.com/berusigma/r-browser/actions)** tab in this repository.
2. Click on the latest workflow run named **Build Android APK**.
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Download **`R-Browser-Debug-APK`**, extract the ZIP, and install the APK on your Android device.

---

## 🔒 Permissions Used

* `android.permission.INTERNET`: Required for web browsing and DoH resolution.
* `android.permission.ACCESS_NETWORK_STATE`: Required for monitoring network connectivity.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
