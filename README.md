# 💳 Tap Pay

### Secure • Resilient • Offline-Capable Digital Payment System

**Tap Pay** is a full-stack fintech system designed to make digital payments more secure, reliable, and resilient when network connectivity is unavailable.

The project combines a **Spring Boot backend** with a **native Android application** to provide an end-to-end payment experience with:

* 🔐 Secure authentication
* 📱 Device management
* 💰 Digital wallets
* 💳 Transaction processing
* 📡 NFC-based payment
* 📷 QR-code payment fallback
* 📴 Offline transaction handling
* 🔄 Automatic synchronization
* 💸 Settlement processing
* 🧾 Audit logging
* 👨‍💼 Administrative operations

> **Pay securely. Keep moving. Sync when connected.**

---

# 📖 Overview

**Tap Pay** is a full-stack digital payment platform built around the idea that a temporary loss of connectivity should not automatically mean a broken payment experience.

The system consists of two major applications:

```text
                    TAP PAY
                       │
          ┌────────────┴────────────┐
          │                         │
          ▼                         ▼
   Android Frontend            Spring Boot Backend
   Kotlin + Compose             Java 21 + Spring Boot
          │                         │
          │                         ▼
          │                       MySQL
          │
          ├── NFC Payment
          ├── QR Payment
          ├── Offline Storage
          ├── Local Transactions
          └── Background Sync
```

The backend handles the core payment-domain operations while the Android application provides the user-facing payment experience.

---

# 🎯 The Problem

Digital payment systems usually assume that the client can continuously communicate with the payment server.

But real-world environments can introduce:

```text
Poor Network
     │
     ├── Weak Connectivity
     ├── Temporary Outage
     ├── Network Switching
     └── Offline Environment
              │
              ▼
         Payment Failure
```

This creates a reliability problem.

Tap Pay explores a different approach:

```text
                 PAYMENT REQUEST
                       │
              ┌────────┴────────┐
              │                 │
           ONLINE            OFFLINE
              │                 │
              ▼                 ▼
       Server Processing   Secure Local
              │             Transaction
              │                 │
              │                 ▼
              │               Queue
              │                 │
              └────────┬────────┘
                       ▼
                      SYNC
                       │
                       ▼
                   VALIDATION
                       │
                       ▼
                   SETTLEMENT
```

---

# ✨ Core Features

## 🔐 Secure Authentication

* JWT-based authentication
* Spring Security integration
* BCrypt password hashing
* Protected backend endpoints
* Role-oriented access control

---

## 📱 Device Management

* Device registration
* Active device handling
* Device deactivation
* Last-seen tracking
* Device-aware transaction architecture

The Android application maintains device identity and communicates the registered device information with the backend.

---

## 💰 Digital Wallet

* Wallet management
* Balance-oriented operations
* Wallet transaction integration
* Wallet reservation support

---

## 💳 Transaction Processing

* Transaction creation
* Transaction lifecycle management
* Transaction validation
* Transaction attempt tracking
* Transaction status handling
* Online transaction processing
* Offline transaction synchronization

---

## 🔏 Transaction Security

* Digital transaction signatures
* Nonce generation and validation
* Replay-protection concepts
* Authentication-based authorization

---

## 📡 Offline Synchronization

* Offline transaction handling
* Local transaction storage
* Pending transaction synchronization
* Server-side validation after reconnection
* Sync-oriented transaction lifecycle
* Background synchronization through Android WorkManager

---

## 📡 NFC Payment

Tap Pay supports device-to-device payment interaction using Android NFC on compatible devices.

The frontend provides an NFC-based payment flow for supported hardware.

```text
Sender Device
      │
      │ NFC
      ▼
Receiver Device
      │
      ▼
Payment Information
      │
      ▼
Create Transaction
      │
      ▼
Local / Server Processing
```

Because NFC availability and behavior can vary between Android devices, Tap Pay also provides a QR-code fallback.

---

## 📷 QR Payment Fallback

When NFC is unavailable or unsuitable for a particular device, Tap Pay provides QR-code payment as an alternative.

```text
Receiver
   │
   ▼
Generate QR
   │
   ▼
Sender Scans QR
   │
   ▼
Decode Payment Information
   │
   ▼
Confirm Payment
   │
   ▼
Create Transaction
```

The Android frontend uses the device camera and QR processing to support this fallback flow.

---

## 💸 Settlement

* Settlement lifecycle
* Transaction-to-settlement flow
* Payment completion processing
* Backend settlement records

---

## 🧾 Audit Logging

* Transaction-related activity tracking
* User activity records
* Administrative traceability
* Investigation-oriented audit data

---

## 👨‍💼 Admin Operations

* Administrative transaction operations
* System monitoring foundations
* Audit-oriented backend capabilities

---

# 📱 Android Frontend

The Tap Pay Android application provides the complete user-facing payment experience.

The frontend is built as a native Android application using **Kotlin and Jetpack Compose**.

### Android Features

* 🔐 User authentication
* 👤 User profile
* 📱 Device registration and status
* 💰 Wallet display
* 💳 Payment flow
* 📡 NFC payment
* 📷 QR-code payment
* 📷 QR-code scanning
* 📴 Offline transaction creation
* 💾 Local transaction storage
* 🔄 Background synchronization
* 📜 Transaction history
* 🌐 REST API communication

---

# 🏗️ Android Architecture

The Android application follows a layered architecture:

```text
                    Jetpack Compose UI
                           │
                           ▼
                     AppViewModel
                           │
                           ▼
                    Repository Layer
                     /            \
                    /              \
                   ▼                ▼
             Retrofit API       Room Database
                   │                │
                   ▼                ▼
            Spring Boot API   Local Transactions
                   │
                   ▼
                 MySQL
```

### Main Android Components

```text
android/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com.example.tappay/
│           │       ├── api/
│           │       ├── data/
│           │       ├── security/
│           │       ├── ui/
│           │       ├── worker/
│           │       └── ...
│           │
│           └── AndroidManifest.xml
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```

The exact package structure may evolve as the Android application develops.

---

# 📴 Offline-First Android Flow

The Android application is designed to preserve payment information locally when the backend cannot immediately be reached.

```text
                Android App
                    │
                    ▼
              Payment Request
                    │
          ┌─────────┴─────────┐
          │                   │
       ONLINE              OFFLINE
          │                   │
          ▼                   ▼
     Backend API        Room Database
                              │
                              ▼
                        PENDING_SYNC
                              │
                              ▼
                         WorkManager
                              │
                       Network Available
                              │
                              ▼
                         Backend API
                              │
                              ▼
                          Validation
                              │
                              ▼
                          Settlement
                              │
                              ▼
                           SYNCED
```

This allows the frontend to retain pending transaction information until synchronization becomes possible.

---

# 🔄 Background Synchronization

Tap Pay uses **Android WorkManager** to support background synchronization of pending transactions.

Example:

```text
Room Database

Transaction #1 → PENDING_SYNC
Transaction #2 → PENDING_SYNC
Transaction #3 → SYNCED
```

After successful synchronization:

```text
Transaction #1 → SYNCED
Transaction #2 → SYNCED
Transaction #3 → SYNCED
```

The backend maintains synchronization and settlement records for processing and auditing.

---

# 📷 QR Payment Flow

The QR fallback works as follows:

```text
             RECEIVER
                │
                ▼
          Generate QR Code
                │
                │
                ▼
             SENDER
                │
                ▼
          Scan QR Code
                │
                ▼
        Decode QR Payload
                │
                ▼
         Verify Receiver
                │
                ▼
         Enter / Confirm
             Amount
                │
                ▼
       Create Transaction
                │
                ▼
          Local Storage
                │
                ▼
              Sync
```

This provides an alternative payment mechanism when NFC cannot be used.

---

# 📊 Project Highlights

| Metric                  | Details                     |
| ----------------------- | --------------------------- |
| Backend Language        | Java 21                     |
| Backend Framework       | Spring Boot 4.x             |
| Android Language        | Kotlin                      |
| Android UI              | Jetpack Compose             |
| Security                | Spring Security + JWT       |
| Database                | MySQL                       |
| Backend Persistence     | Spring Data JPA / Hibernate |
| Android Local Storage   | Room                        |
| API                     | REST                        |
| API Documentation       | Swagger / OpenAPI           |
| Password Security       | BCrypt                      |
| Android Background Sync | WorkManager                 |
| QR Processing           | ZXing                       |
| Camera                  | CameraX                     |
| Payment Methods         | NFC + QR                    |
| Architecture            | Full-Stack Modular System   |
| Backend Architecture    | Modular Monolith            |
| Payment Model           | Online + Offline Sync       |
| Build Tools             | Maven + Gradle              |

---

# 💡 Why Tap Pay?

Most beginner payment projects stop at:

```text
User
 ↓
API
 ↓
Database
```

Tap Pay focuses on the problems that appear **after the basic CRUD layer**.

```text
                    ┌──────────────────┐
                    │     Tap Pay      │
                    └────────┬─────────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       ▼                     ▼                     ▼
   SECURITY              RESILIENCE          TRACEABILITY
       │                     │                     │
       ▼                     ▼                     ▼
 JWT / BCrypt          Offline Sync         Audit Logs
 Device Security      Nonce Handling        Transaction
 Signatures            Settlement             History
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
                             ▼
                    Android Frontend
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
                  NFC                QR
```

This makes Tap Pay a foundation for exploring **real-world fintech engineering across both backend and mobile development**.

---

# 🏛 High-Level Architecture

```text
                         TAP PAY
                            │
             ┌──────────────┴──────────────┐
             │                             │
             ▼                             ▼
       Android Client                Spring Boot API
       Kotlin + Compose              Java 21
             │                             │
       ┌─────┼─────┐              ┌───────┼────────┐
       │     │     │              │       │        │
       ▼     ▼     ▼              ▼       ▼        ▼
      NFC    QR   Room          Auth   Transaction Wallet
                    │              │       │        │
                    │              └───────┼────────┘
                    │                      │
                    ▼                      ▼
                WorkManager              MySQL
                    │
                    ▼
               Sync API
```

---

# 🧩 Modular Backend Architecture

Tap Pay currently follows a **Modular Monolith** architecture.

Instead of introducing microservices only for the sake of using microservices, the backend keeps related domains inside a single application while maintaining clear module boundaries.

```text
Tap Pay Backend
│
├── Auth
│
├── Device
│
├── Wallet
│
├── Transaction
│
├── Signature
│
├── Nonce
│
├── Settlement
│
├── Sync
│
├── Audit
│
└── Admin
```

### Why Modular Monolith?

A payment system benefits from clear domain boundaries, but distributed infrastructure also introduces:

* Network failures
* Service discovery
* Distributed transactions
* Deployment complexity
* Observability requirements
* Additional debugging overhead

Tap Pay therefore prioritizes:

> **Correct domain separation first. Distribution when scale actually requires it.**

This architecture also keeps the project ready for future service extraction.

---

# 🔄 Payment Flow

## Online Payment

```text
User
 │
 ▼
Authenticate
 │
 ▼
Open Android App
 │
 ▼
Create Payment
 │
 ▼
Validate Request
 │
 ▼
Verify Security
 │
 ▼
Process Transaction
 │
 ▼
Settlement
 │
 ▼
Audit
 │
 ▼
Transaction Completed
```

---

# 📡 Offline Payment Flow

The offline workflow is one of the key architectural concepts of Tap Pay.

```text
                    OFFLINE DEVICE
                          │
                          ▼
                   Open Android App
                          │
                          ▼
                   Create Transaction
                          │
                          ▼
                     Generate Nonce
                          │
                          ▼
                   Sign Transaction
                          │
                          ▼
                    Store / Queue
                          │
                   Network Restored
                          │
                          ▼
                         Sync
                          │
                          ▼
                   Server Validation
                          │
                  ┌───────┴───────┐
                  │               │
                VALID           INVALID
                  │               │
                  ▼               ▼
               Process          Reject
                  │
                  ▼
              Settlement
                  │
                  ▼
                Audit
```

This provides the foundation for building payment experiences that are more tolerant of temporary connectivity failures.

---

# 📱 End-to-End Payment Methods

Tap Pay supports two device-side payment mechanisms:

## 📡 NFC

```text
Sender
  │
  │ NFC
  ▼
Receiver
  │
  ▼
Payment Data
  │
  ▼
Transaction
```

## 📷 QR

```text
Receiver
  │
  ▼
QR Code
  │
  ▼
Sender Scans
  │
  ▼
Payment Data
  │
  ▼
Transaction
```

The QR mechanism acts as a fallback when NFC is unavailable or unsuitable for the participating devices.

---

# 🛡️ Security Architecture

Security is treated as a core payment requirement rather than an additional feature.

```text
                  SECURITY LAYER
                       │
       ┌───────────────┼────────────────┐
       ▼               ▼                ▼
 Authentication     Integrity       Replay Protection
       │               │                │
       ▼               ▼                ▼
      JWT           Signature          Nonce
       │               │                │
       └───────────────┼────────────────┘
                       ▼
                 Secure Transaction
```

### JWT Authentication

Provides stateless authentication for protected APIs.

### BCrypt

Passwords are stored using secure password hashing rather than plaintext credentials.

### Digital Signatures

Transaction signatures provide an integrity-oriented security layer.

### Nonce

Nonce handling provides a foundation for preventing replay of previously submitted transaction requests.

### Device Security

The Android application maintains device-related identity and communicates device information with the backend.

### Audit Logs

Important operations can be recorded for traceability and investigation.

---

# 🧱 Core Components

| Module          | Responsibility                       |
| --------------- | ------------------------------------ |
| **Auth**        | Authentication & authorization       |
| **Device**      | Device registration & lifecycle      |
| **Wallet**      | Wallet operations                    |
| **Transaction** | Payment transaction lifecycle        |
| **Signature**   | Transaction integrity                |
| **Nonce**       | Replay protection                    |
| **Settlement**  | Settlement processing                |
| **Sync**        | Offline transaction synchronization  |
| **Audit**       | Activity & transaction auditing      |
| **Admin**       | Administrative operations            |
| **Android App** | User-facing payment experience       |
| **NFC**         | Device-to-device payment interaction |
| **QR**          | Alternative payment interaction      |
| **Room**        | Local transaction persistence        |
| **WorkManager** | Background synchronization           |

---

# 🗄️ Database Layer

Tap Pay uses **MySQL** as the primary relational database.

The database stores the core payment-domain information required for:

```text
Users
  │
  ├── Devices
  │
  ├── Wallets
  │
  ├── Transactions
  │
  ├── Transaction Attempts
  │
  ├── Signatures
  │
  ├── Nonces
  │
  ├── Sync Batches
  │
  ├── Settlements
  │
  └── Audit Logs
```

The relational model provides consistency and structured relationships for financial transaction data.

---

# 📱 Android Local Database

The Android application maintains local transaction information using **Room**.

The local database is used for the offline-first portion of the application.

```text
Android Payment
      │
      ▼
LocalTransaction
      │
      ▼
PENDING_SYNC
      │
      ▼
WorkManager
      │
      ▼
Backend Synchronization
      │
      ▼
SYNCED
```

This separates temporary local transaction state from the backend's authoritative transaction processing.

---

# 📁 Full Project Structure

Tap Pay is organized as a full-stack project:

```text
Tap_Pay
│
├── backend
│   │
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com.example.tap_pay
│   │   │   │       ├── auth
│   │   │   │       ├── device
│   │   │   │       ├── wallet
│   │   │   │       ├── transaction
│   │   │   │       ├── signature
│   │   │   │       ├── nonce
│   │   │   │       ├── settlement
│   │   │   │       ├── sync
│   │   │   │       ├── audit
│   │   │   │       └── admin
│   │   │   │
│   │   │   └── resources
│   │   │       └── application.properties
│   │   │
│   │   └── test
│   │
│   ├── .mvn
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── android
│   │
│   ├── app
│   │   └── src
│   │       └── main
│   │           ├── java
│   │           │   └── com.example.tappay
│   │           │       ├── api
│   │           │       ├── data
│   │           │       ├── security
│   │           │       ├── ui
│   │           │       ├── worker
│   │           │       └── ...
│   │           │
│   │           └── AndroidManifest.xml
│   │
│   ├── gradle
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradlew
│
├── screenshots
│
└── README.md
```

> If the repository currently keeps the Spring Boot project at the repository root rather than inside `backend/`, the same architecture still applies; only the physical folder location differs.

---

# ⚙️ Tech Stack

## Backend

* Java 21
* Spring Boot 4.x
* Spring Web MVC
* Spring Data JPA
* Spring Security
* JWT
* BCrypt
* MySQL
* Hibernate
* Maven
* Swagger / OpenAPI

## Android Frontend

* Kotlin
* Jetpack Compose
* Android SDK
* Retrofit
* Room Database
* Kotlin Coroutines
* WorkManager
* CameraX
* ZXing
* Android NFC

---

# 🔌 Backend API

The backend exposes REST APIs for the Android client.

Major API areas include:

```text
Authentication
      │
      ├── Login
      └── Authorization

Devices
      │
      ├── Register
      ├── Get Device
      ├── Active Device
      └── Deactivate

Wallet
      │
      └── Reserve

Transactions
      │
      └── Create

Synchronization
      │
      └── Sync

Settlement
      │
      └── Settlement Processing

Audit
      │
      └── Audit Records
```

Swagger/OpenAPI provides the detailed API specification.

---

# 📖 API Documentation

After starting the backend, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

For testing with a physical Android device connected to the same local network, use the computer's LAN IP address:

```text
http://YOUR_PC_IP:8080/swagger-ui/index.html
```

Example:

```text
http://192.168.x.x:8080/swagger-ui/index.html
```

---

# ⚙️ Running the Backend

## Requirements

* JDK 21
* MySQL 8+
* Maven
* Git

## Clone the Repository

```bash
git clone https://github.com/k95578516-oss/Tap_Pay.git
cd Tap_Pay
```

## Configure MySQL

Create the database:

```sql
CREATE DATABASE Tap_Pay;
```

Configure the database connection in the backend configuration.

**Do not commit database passwords, JWT secrets, API keys, or other sensitive credentials to GitHub.**

## Start the Backend

From the backend project directory:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

# 📱 Running the Android Application

## Requirements

* Android Studio
* JDK 21
* Android SDK
* Android device or emulator
* Android 8.0+ device

## Open the Android Project

Open the Android project folder:

```text
android/
```

in Android Studio.

---

## Configure Backend URL

### Android Emulator

For an Android emulator accessing a backend running on the development computer:

```text
http://10.0.2.2:8080/
```

### Physical Android Device

For a physical Android device connected to the same Wi-Fi network as the development computer:

```text
http://YOUR_PC_IP:8080/
```

Example:

```text
http://192.168.1.114:8080/
```

The backend must be reachable from the Android device over the local network.

---

# 🧪 Running a Payment Test

A complete demonstration can use two Android devices.

## Device A — Sender

```text
Login
  ↓
Open Wallet
  ↓
Open Pay
  ↓
Choose NFC or QR
  ↓
Enter Payment Information
  ↓
Confirm Payment
```

## Device B — Receiver

For NFC:

```text
Open Receiver Flow
  ↓
Enable NFC
  ↓
Receive Payment Interaction
```

Or for QR:

```text
Generate QR
  ↓
Display QR
```

## Sender Using QR

```text
Open QR Scanner
  ↓
Scan Receiver QR
  ↓
Decode Payment Information
  ↓
Confirm Payment
  ↓
Create Transaction
```

---

# 🔄 Complete Offline-to-Online Flow

```text
                USER
                  │
                  ▼
             Android App
                  │
          ┌───────┴────────┐
          │                │
         NFC              QR
          │                │
          └───────┬────────┘
                  ▼
          Create Transaction
                  │
                  ▼
            Room Database
                  │
                  ▼
             PENDING_SYNC
                  │
                  ▼
            WorkManager
                  │
           Network Available
                  │
                  ▼
           Spring Boot API
                  │
                  ▼
          Server Validation
                  │
          ┌───────┴────────┐
          │                │
        VALID            INVALID
          │                │
          ▼                ▼
      Transaction        Rejected
       Processing
          │
          ▼
      Settlement
          │
          ▼
        Audit
          │
          ▼
        SYNCED
```

---

# 🎥 Demo

A complete Tap Pay demonstration should show:

```text
1. User Login
       ↓
2. Device Registration
       ↓
3. Wallet
       ↓
4. Payment Screen
       ↓
5. NFC Payment
       ↓
6. QR Payment Fallback
       ↓
7. Offline Transaction
       ↓
8. Local Storage
       ↓
9. Network Reconnection
       ↓
10. Automatic Synchronization
       ↓
11. Backend Validation
       ↓
12. Settlement
       ↓
13. Transaction History
```

---

# 📸 Screenshots

The repository can include screenshots demonstrating the Android application:

```text
screenshots/
│
├── login.png
├── profile.png
├── wallet.png
├── payment.png
├── nfc-payment.png
├── qr-payment.png
├── qr-scanner.png
├── transaction-history.png
└── sync-status.png
```

These screenshots provide a quick visual overview of the mobile application for reviewers who do not build the Android project locally.

---

# 🧠 Engineering Challenges

## Android ↔ Backend Communication

The Android application needs to communicate with the Spring Boot backend during physical-device testing.

This requires configuring the Android API URL with the development computer's LAN IP instead of `localhost`.

---

## NFC Compatibility

NFC behavior can vary between Android devices and hardware implementations.

Tap Pay therefore provides QR-based payment as a fallback mechanism.

---

## Offline Transactions

When the backend cannot immediately be reached, the Android application needs to preserve the transaction information locally.

Room is used to maintain pending local transaction state.

---

## Synchronization

The system needs to distinguish between transaction states such as:

```text
PENDING_SYNC
SYNCED
FAILED
```

and synchronize pending transactions when connectivity becomes available.

---

## Background Processing

WorkManager is used to support background synchronization instead of requiring the user to manually trigger every synchronization operation.

---

# 🔮 Future Improvements

Possible future improvements include:

* Stronger cryptographic protection for offline payment payloads
* Hardware-backed Android Keystore integration
* Improved NFC peer-to-peer protocol
* Better conflict resolution
* Idempotent transaction synchronization
* Stronger fraud detection
* Production-grade key management
* Secure server-side settlement architecture
* Improved offline balance management
* Push notifications
* Merchant mode
* Multi-device wallet support
* Production deployment
* Comprehensive automated Android UI testing
* Expanded observability and monitoring

---

# 🏆 Hackathon Value

Tap Pay demonstrates full-stack engineering across:

```text
             TAP PAY
                │
     ┌──────────┴──────────┐
     │                     │
     ▼                     ▼
 Android Engineering   Backend Engineering
     │                     │
 Kotlin                Java 21
 Compose               Spring Boot
 Room                  Spring Security
 WorkManager            JWT
 CameraX               JPA / Hibernate
 ZXing                 MySQL
 NFC                   REST APIs
     │                     │
     └──────────┬──────────┘
                ▼
        Offline Payment
                │
        ┌───────┴───────┐
        ▼               ▼
       NFC              QR
        │               │
        └───────┬───────┘
                ▼
        Offline Storage
                │
                ▼
        Automatic Sync
                │
                ▼
           Settlement
```

The project is intended to demonstrate not only CRUD functionality, but also practical considerations around **security, offline resilience, synchronization, device interaction, transaction lifecycle, and mobile-backend integration**.

---

# 👩‍💻 Project

## Tap Pay

**Secure • Resilient • Offline-Capable Digital Payment System**

### Repository

`https://github.com/k95578516-oss/Tap_Pay`

### Backend

```text
Java 21
Spring Boot 4.x
Spring Security
JWT
MySQL
Spring Data JPA
Swagger / OpenAPI
```

### Android Frontend

```text
Kotlin
Jetpack Compose
Retrofit
Room
WorkManager
CameraX
ZXing
Android NFC
```

### Payment Methods

```text
📡 NFC
📷 QR
```

### Offline Technology

```text
Room Database
+
WorkManager
+
Backend Synchronization
```

---

# 📄 License

This project is developed as a hackathon/academic project.

See the repository license for usage and distribution terms.
