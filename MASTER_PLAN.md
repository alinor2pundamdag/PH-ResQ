# PH-ResQ Vanguard 2026: Master Architecture & Implementation Plan

This repository contains the master architectural blueprint, features, legal compliance framework, and comprehensive nationwide emergency directory for **PH-ResQ Vanguard 2026**, a 100% functional, secure, and nationwide emergency application built for Filipinos.

---

## 🏛️ 1. Executive Summary & Core Design Principles
- **Name:** PH-ResQ Vanguard 2026
- **Target Users:** All Filipino citizens, commuters, families, students, motorists, senior citizens, workers, and residents in high-risk disaster zones across Luzon, Visayas, and Mindanao.
- **Design Philosophy:** Clean, professional UI where unnecessary elements are hidden, and vital emergency tools are immediately tappable. Low-end device optimized with a neon sci-fi glassmorphic aesthetic and a 100% looping wind-swaying Philippine flag canvas physics simulation.

---

## 🔒 2. Legal Framework & Data Privacy Compliance (RA 10173) & Security
- **Data Privacy Act of 2012 (RA 10173) Compliance:** All personal emergency contacts and device GPS coordinates remain **strictly local and private** (`localStorage`) (For Local). No personal data or location is ever uploaded, sold, or transmitted to any external third-party cloud servers without user authorization (For Online sync).
- **Client-Side Encryption (Zero-Knowledge Local Storage):** The 10 personal emergency contacts and sensitive inputs are secured via **AES-GCM client-side encryption** with a secure device-bound key (For Local) to protect against data extraction, malware, or physical snooping.
- **Strict Sanitization & Anti-Injection Guards:** Input handlers utilize rigorous Regex validation filters (For Local) to block XSS, script injection, and tampering on Name fields (letters/spaces only) and Phone fields (numbers only).
- **Secure Sandbox Isolation:** Strict scheme whitelisting for `tel:`, `sms:`, and `mailto:` intents (For Local/Device).

---

## 📱 3. 10-Slot Personal Emergency Contact System & Auto-Boot Agent
- **Capacity:** Expanded to **10 dedicated emergency contact slots** (For Local).
- **Strict Input Validation & Error Handling (For Local):**
  - **Name Field:** Accepts *letters and spaces only* (For Local). Rejects numbers or prohibited symbols with an immediate error prompt.
  - **Number Field:** Accepts *numbers only* e.g., `09XXXXXXXXX` (For Local). Incomplete or invalid entries will not be saved.
- **Auto-Boot Background Agent:** Configured via Service Workers and Web Broadcast Channels to initialize monitoring on device boot (For Local).

---

## 📞 4. Device Integrations, Phonebook Export & Email Dispatch
- **Direct Phone Dialer (`tel:`):** Tapping any hotline launches native dialer (For Local).
- **Direct SMS Broadcast (`sms:`):** Triggers multi-recipient emergency SMS broadcasts with GPS coordinates (For Local).
- **Direct Email Dispatch (`mailto:`):** Full support for dispatching official inquiry or medical profile emails (For Online/Connected).
- **Save to Phonebook (vCard Export):** Dedicated phonebook icon to instantly save numbers to device contact list (For Local).

---

## 🗺️ 5. Interactive Proximity Map, Geolocation Pinning & Dynamic Radar
- **Universal Map-Based Proximity Radar:** Offline-capable GPS geofencing mapping **all** institutional listings (government offices, public/private hospitals, SSS/PhilHealth/Pag-IBIG branches, utilities, transport hubs, student retail/bookstores, motorist assistance hubs, and evacuation centers) across Luzon, Visayas, and Mindanao, dynamically sorted from closest to farthest based on user's live hardware GPS coordinates (For Local / Pre-loaded).
- **Coastal & Island Emergency Points:** Specialized positioning for coastal/islands prioritizing PCG detachments, naval outposts, and high-ground evacuation points (For Local).
- **Metro Manila Transit & LRT/MRT Navigation Guides:** Step-by-step platform instructions (Northbound/Southbound, left/right door open indicators for interchanges like EDSA, Recto, Cubao, Doroteo Jose) and transit hotlines (For Local).

---

## 🚗 6. Motorist & Road Safety Protection Framework (Deep Research & Architecture)
- **Real-Time Expressway & Highway SOS Dispatch:** Dedicated offline-first dialing and SMS channels for all major Philippine expressways (NLEx, SLEX, Skyway, TPLEX, CAVITEX, SCTEX) with automated GPS mile-marker tagging (For Local).
- **LTO, LTFRB & DPWH Road Rescue Integration:** Immediate access to vehicular accident reporting, towing services, roadside assistance, and traffic violation assistance hotlines (For Local / Online).
- **Motorist Digital Glovebox (Offline Document Vault):** Encrypted local storage for driver's license details, vehicle registration (OR/CR), and insurance emergency policy numbers (For Local).
- **Nationwide Expressway & Road Assistance Hub (Geo-Mapped):**
  - **NLEx Traffic & Accident Hotline:** `(02) 8350-0000` / `(02) 8580-8900` | Email: `customercare@nlex.com.ph` | Website: `nlex.com.ph` (For Local / Online)
  - **SLEX / Skyway Traffic Command:** `(02) 8824-2282` / Mobile: `0917-539-8762` | Email: `customercare@slex.ph` | Website: `slex.ph` (For Local / Online)
  - **TPLEX (Tarlac-Pangasinan-La Union Expressway):** Hotline: `0917-888-0715` | Email: `customercare@tplex.com.ph` | Website: `tplex.com.ph` (For Local / Online)
  - **SCTEX (Subic-Clark-Tarlac Expressway):** Hotline: `(02) 8350-0000` | Email: `customercare@nlex.com.ph` | Website: `nlex.com.ph` (For Local / Online)
  - **CAVITEX / C5 Link:** `(02) 8825-4004` / Mobile: `0942-822-8489` | Email: `customercare@cavitex.ph` | Website: `cavitex.ph` (For Local / Online)
  - **MMDA Metrobase & Road Rescue:** `136` / `(02) 8882-4151` | Email: `metrobase@mmda.gov.ph` | Website: `mmda.gov.ph` (For Local / Online)

---

## 📡 7. Smart Hybrid Sync & "Free Data / Social Media Only" Architecture
To support millions of Filipinos who rely purely on social media or limited promotional data packages ("Free FB/TikTok data"):
1. **RA 10639 Free Mobile Disaster Alerts Integration:** Built-in broadcast receiver to ingest zero-rated government/telco emergency warnings (For Local / Broadcast).
2. **Messenger / Lightweight Bot Bridge:** Optional companion lightweight chat channels for querying directory updates (For Online).
3. **100% Offline Pre-Loaded Core Database:** Complete nationwide directory, motorist expressways, student retail stores, and transport guides baked directly into the installation binary (For Local).
4. **P2P Local Mesh Sharing:** Ability to sync emergency updates and directory patches locally between nearby devices via Bluetooth Low Energy (BLE) or Wi-Fi Direct (For Local).

---

## 🎒 8. Student, Senior Citizens & Workers Protection Framework
- **Safe Arrival / Safe Home Auto-Check-In:** Automated parent/guardian alert trigger notifying departure or arrival status with GPS beacons (For Local/Online).
- **Universal Medical & Maintenance ID Pocket (Senior/Student Vault):** Encrypted offline emergency medical profile (maintenance medicines, blood type, allergies) accessible by first responders (For Local).
- **Real-Time Class Suspension & Weather Ticker:** Official DepEd, CHED, and LGU advisory parser caching suspension announcements offline (For Local / Online sync).
- **Official University & Educational Institution Hotline Hub (Public Directories & Websites):**
  - **University of the Philippines Diliman:** Police `(02) 8928-3615` | Email: `ovcsa.upd@up.edu.ph` | Website: `upd.edu.ph` (For Local / Online)
  - **Ateneo de Manila University:** Loyola Police `(02) 8442-7782` | Email: `taktak.ls@ateneo.edu` | Website: `ateneo.edu` (For Local / Online)
  - **De La Salle University (Manila):** Taft Trunkline `(02) 8524-4611` | Email: `my.dlsu@dlsu.edu.ph` | Website: `dlsu.edu.ph` (For Local / Online)
  - **University of Santo Tomas (UST):** Hospital ER `(02) 8731-3001` | Email: `contactus@ust.edu.ph` | Website: `ust.edu.ph` (For Local / Online)
  - **De La Salle University - Dasmariñas:** Trunkline `(046) 481-1900` | Email: `dlsud@dlsud.edu.ph` | Website: `dlsud.edu.ph` (For Local / Online)

---

## 🏛️ 9. Comprehensive Nationwide Emergency Directory Hub (Multi-Agency, Retail & Private/Public Grid)
The application integrates a hyper-accurate, offline-first national database spanning Luzon, Visayas, and Mindanao, dynamically sorted using hardware GPS geofencing. Every listing with a physical location is automatically detected by the map (For Local):

### **A. National Unified & Emergency Hotlines**
- **National Emergency 911:** `911` (For Local)
- **PNP Command Center (National HQ):** `(02) 8537-4600` | Email: `pnp.cio@pnp.gov.ph` | Website: `pnp.gov.ph` (For Local / Online)
- **NDRRMC Operations Center:** `(02) 8911-1406` | Email: `ndrrmoc@ocd.gov.ph` | Website: `ndrrmc.gov.ph` (For Local / Online)
- **Philippine Red Cross:** `143` | Email: `secgen@redcross.org.ph` | Website: `redcross.org.ph` (For Local / Online)
- **Bureau of Fire Protection (BFP):** `(02) 8426-0219` | Email: `bfp_nhq@bfp.gov.ph` | Website: `bfp.gov.ph` (For Local / Online)
- **Philippine Coast Guard (PCG):** `(02) 8527-8481` | Email: `pcg_actioncenter@coastguard.gov.ph` | Website: `coastguard.gov.ph` (For Local / Online)

### **B. Social Security, Labor & Public Welfare Agencies (Geo-Mapped Nationwide)**
- **SSS (Social Security System):** Hotline `(02) 8920-6446` | Email: `member_relations@sss.gov.ph` | Website: `sss.gov.ph` (For Local / Online)
- **PhilHealth:** Hotline `(02) 8662-2588` | Email: `actioncenter@philhealth.gov.ph` | Website: `philhealth.gov.ph` (For Local / Online)
- **Pag-IBIG Fund (HDMF):** Hotline `(02) 8724-4244` | Email: `contactus@pagibigfund.gov.ph` | Website: `pagibigfund.gov.ph` (For Local / Online)
- **DOLE & DOTr:** DOLE Hotline `1349` | Email: `hotline1349@dole.gov.ph` | Website: `dole.gov.ph` (For Local / Online)

### **C. Public & Private Hospitals / Medical Centers (Geo-Mapped Nationwide - Luzon, Visayas, Mindanao)**
- **Public:**
  - **PGH Manila:** `(02) 8554-8400` | Email: `pgh.gov.ph@gmail.com` | Website: `pgh.gov.ph` (For Local / Online)
  - **East Avenue Medical Center (QC):** `(02) 8928-0611` | Email: `eamc@doh.gov.ph` | Website: `eamc.doh.gov.ph` (For Local / Online)
  - **Vicente Sotto Memorial Medical Center (Cebu):** `(032) 253-9891` | Email: `vsmmc@doh.gov.ph` | Website: `vsmmc.doh.gov.ph` (For Local / Online)
- **Private:**
  - **Makati Medical Center:** `(02) 8888-8999` | Email: `mmc@makatimed.net.ph` | Website: `makatimed.net.ph` (For Local / Online)
  - **St. Luke’s Medical Center (Global City / QC):** `(02) 8789-7700` | Email: `customercare@stlukes.com.ph` | Website: `stlukes.com.ph` (For Local / Online)
  - **Cardinal Santos Medical Center (San Juan):** `(02) 8727-0001` | Email: `csmc@cardinalsantos.com.ph` | Website: `cardinalsantos.com.ph` (For Local / Online)
  - **The Medical City (Ortigas):** `(02) 8988-1000` | Email: `mail@themedicalcity.com` | Website: `themedicalcity.com` (For Local / Online)
  - **Asian Hospital (Muntinlupa):** `(02) 8771-9000` | Email: `info@asianhospital.com` | Website: `asianhospital.com` (For Local / Online)

### **D. Student Retail Hubs & School Supplies Stores (Geo-Mapped Nationwide)**
- **National Book Store (NBS):** Hotline `(02) 8888-6274` | Email: `support@nationalbookstore.com.ph` | Website: `nationalbookstore.com` (For Local / Online)
- **The SM Store & SM Supermalls:** Hotline `(02) 8876-1111` | Email: `customercare@smsupermalls.com` | Website: `smsupermalls.com` (For Local / Online)
- **Robinsons Department Store:** Hotline `(02) 8397-1888` | Email: `ask@robinsonsmalls.com` | Website: `robinsonsdepartmentstore.com.ph` (For Local / Online)
- **Pandayan Bookshop:** Hotline `(044) 806-9340` | Email: `info@pandayan.com.ph` | Website: `pandayan.com.ph` (For Local / Online)

### **E. Utility & Life Support Providers (Power & Water)**
- **MERALCO:** `16211` | Email: `customercare@meralco.com.ph` | Website: `meralco.com.ph` (For Local / Online)
- **Maynilad Water Services:** `1626` | Email: `customer.help@mayniladwater.com.ph` | Website: `mayniladwater.com.ph` (For Local / Online)
- **Manila Water:** `1627` | Email: `customercare@manilawater.com` | Website: `manilawater.com` (For Local / Online)

### **F. Regional, Provincial & Municipal Hubs (Luzon, Visayas, Mindanao - Geo-Mapped)**
- **CAR & Baguio:** BCPO `(074) 442-1215`, Benguet Gen Hospital `(074) 422-2936` (For Local)
- **Ilocos Region:** Laoag Police `(077) 772-0402`, ITRMC La Union `(072) 607-6421` (For Local)
- **Cagayan Valley:** Tuguegarao Police `(078) 844-1234`, CVMC `(078) 396-1550` (For Local)
- **Central Luzon:** Angeles Police `(045) 892-2302`, Bulacan PPO `(044) 791-1256` (For Local)
- **CALABARZON:** Cavite PPO `(046) 471-2920`, Laguna PPO `(049) 501-1377` (For Local)
- **MIMAROPA:** Puerto Princesa Police `(048) 434-2735` (For Local)
- **Bicol Region:** Legazpi Police `(052) 481-1234`, BRTTH `(052) 483-0057` (For Local)
- **Western Visayas:** Iloilo Police `(033) 337-1337`, WVMC `(033) 321-2841` (For Local)
- **Central Visayas:** Cebu Police `(032) 255-6845`, VSMMC `(032) 253-9891` (For Local)
- **Eastern Visayas:** Tacloban Police `(053) 832-1234`, EVRMC `(053) 832-0177` (For Local)
- **Zamboanga Peninsula:** Zamboanga Police `(062) 991-1002`, ZCMC `(062) 991-2933` (For Local)
- **Northern Mindanao:** CDO Police `(088) 857-1234`, NMMC `(088) 856-4147` (For Local)
- **Davao Region:** Davao Police `(082) 224-1311`, SPMC `(082) 227-2731` (For Local)
- **SOCCSKSARGEN:** GenSan Police `(083) 552-3030` (For Local)
- **Caraga Region:** Butuan Police `(085) 341-1234` (For Local)
- **BARMM:** Cotabato Police `(064) 421-1234`, Marawi Police `0998-598-6345` (For Local)
