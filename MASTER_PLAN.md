# PH-ResQ Vanguard 2026: Master Architecture & Implementation Plan

This repository contains the master architectural blueprint, features, legal compliance framework, and comprehensive nationwide emergency directory for **PH-ResQ Vanguard 2026**, a 100% functional, secure, and nationwide emergency application built for Filipinos.

---

## 🏛️ 1. Executive Summary & Core Design Principles
- **Name:** PH-ResQ Vanguard 2026
- **Target Users:** All Filipino citizens, commuters, families, students, motorists, senior citizens, workers, and residents in high-risk disaster zones across Luzon, Visayas, and Mindanao.
- **Design Philosophy:** Clean, professional UI where unnecessary elements are hidden, and vital emergency tools are immediately tappable. Low-end device optimized with a neon sci-fi glassmorphic aesthetic and a 100% looping wind-swaying Philippine flag canvas physics simulation.

---

## 🔒 2. Legal Framework & Data Privacy Compliance (RA 10173) & Security
- **Data Privacy Act of 2012 (RA 10173) Compliance:** All personal emergency contacts and device GPS coordinates remain **strictly local and private** (`localStorage`). No personal data or location is ever uploaded, sold, or transmitted to any external third-party cloud servers.
- **Client-Side Encryption (Zero-Knowledge Local Storage):** The 10 personal emergency contacts and sensitive inputs are secured via **AES-GCM client-side encryption** with a secure device-bound key to protect against data extraction, malware, or physical snooping.
- **Strict Sanitization & Anti-Injection Guards:** Input handlers utilize rigorous Regex validation filters to block XSS, script injection, and tampering on Name fields (letters/spaces only) and Phone fields (numbers only).
- **Secure Sandbox Isolation:** Strict scheme whitelisting for `tel:`, `sms:`, and `mailto:` intents to prevent tap-jacking or malicious hijacking.

---

## 📱 3. 10-Slot Personal Emergency Contact System & Auto-Boot Agent
- **Capacity:** Expanded to **10 dedicated emergency contact slots**.
- **Strict Input Validation & Error Handling:**
  - **Name Field:** Accepts *letters and spaces only*. Rejects numbers or prohibited symbols with an immediate error prompt; non-compliant entries cannot be saved.
  - **Number Field:** Accepts *numbers only* (e.g., `09XXXXXXXXX` or `+639XXXXXXXXX`). Rejects text or invalid formats with an error prompt; incomplete or invalid entries **will not be saved to local storage**.
- **Auto-Boot Background Agent:** Configured via Service Workers and Web Broadcast Channels to initialize monitoring on device boot / shutdown recovery so the safety agent remains active without manual full-UI app launches.

---

## 📞 4. Device Integrations, Phonebook Export & Email Dispatch
- **Direct Phone Dialer (`tel:`):** Tapping any hotline or saved contact immediately launches the native device dialer with the number pre-filled for instant cellular network calls.
- **Direct SMS Broadcast (`sms:`):** Triggers multi-recipient emergency SMS broadcasts pre-populated with user distress status and real-time GPS coordinates.
- **Direct Email Dispatch (`mailto:`):** Full support for dispatching official inquiry, medical profile, or assistance emails to public/private institutional contacts pre-populated with user coordinates.
- **Save to Phonebook (vCard Export):** Every directory listing, emergency hotline, municipal post, and contact feature includes a dedicated phonebook icon (`ACTION_INSERT` / vCard intent) allowing users to instantly save numbers directly to their device contact list with a single tap.

---

## 🗺️ 5. Interactive Proximity Map, Geolocation Pinning & Dynamic Radar
- **Universal Map-Based Proximity Radar:** Offline-capable GPS geofencing mapping **all** institutional listings (government offices, public/private hospitals, SSS/PhilHealth/Pag-IBIG branches, utilities, transport hubs, and evacuation centers) dynamically sorted from closest to farthest based on user's live hardware GPS coordinates.
- **Coastal & Island Emergency Points:** Specialized positioning for coastal/islands prioritizing PCG detachments, naval outposts, and high-ground evacuation points.
- **Metro Manila Transit & LRT/MRT Navigation Guides:** Step-by-step platform instructions (Northbound/Southbound, left/right door open indicators for interchanges like EDSA, Recto, Cubao, Doroteo Jose) and transit hotlines.

---

## 📡 6. Smart Hybrid Sync & "Free Data / Social Media Only" Architecture
To support millions of Filipinos who rely purely on social media or limited promotional data packages ("Free FB/TikTok data"), the system integrates multi-tier fallback mechanisms:
1. **RA 10639 Free Mobile Disaster Alerts Integration:** Built-in broadcast receiver to ingest zero-rated government/telco emergency warnings and auto-update status feeds offline.
2. **Messenger / Lightweight Bot Bridge:** Optional companion lightweight chat channels accessible via standard social data promos for querying directory updates or checking region safety statuses without full web browsing.
3. **100% Offline Pre-Loaded Core Database:** Complete directory, transport guides, and security tools baked directly into the installation binary, requiring zero data for daily use.
4. **P2P Local Mesh Sharing:** Ability to sync emergency updates and directory patches locally between nearby devices via Bluetooth Low Energy (BLE) or Wi-Fi Direct without cellular data consumption.

---

## 🎒 7. Student, Senior Citizens & Workers Protection Framework
- **Safe Arrival / Safe Home Auto-Check-In:** Automated parent/guardian alert trigger notifying departure or arrival status with GPS beacons.
- **Universal Medical & Maintenance ID Pocket (Senior/Student Vault):** Encrypted offline emergency medical profile (maintenance medicines, blood type, allergies) accessible by first responders even when device is locked.
- **Real-Time Class Suspension & Weather Ticker:** Official DepEd, CHED, and LGU advisory parser caching suspension announcements offline.
- **Official University & Educational Institution Hotline Hub (Public Directories & Websites):**
  - **University of the Philippines Diliman:** Police `(02) 8928-3615` / `(02) 8981-8500 loc 113`, Health Services `(02) 8981-8500 loc 111`, Security Helpline `0917-597-1984` | Email: `ovcsa.upd@up.edu.ph` | Website: `upd.edu.ph`
  - **Ateneo de Manila University:** Loyola Heights Police/Brgy `(02) 8442-7782` / `(02) 8425-1434`, CORD SOSS `(02) 8426-6001 loc 5263` | Email: `taktak.ls@ateneo.edu` | Website: `ateneo.edu`
  - **De La Salle University (Manila):** Taft Trunkline `(02) 8524-4611`, Clinic (Emergency) `loc 111`, Security & Safety `loc 444/555` | Email: `my.dlsu@dlsu.edu.ph` | Website: `dlsu.edu.ph`
  - **University of Santo Tomas (UST / UST Hospital):** UST Hospital Emergency & Info `(02) 8731-3001` | Email: `contactus@ust.edu.ph` | Website: `ust.edu.ph` / `usthospital.com.ph`
  - **De La Salle University - Dasmariñas (Cavite):** Trunkline `(046) 481-1900`, Security Office `loc 3044`, Health Clinic `loc 3039` | Email: `dlsud@dlsud.edu.ph` | Website: `dlsud.edu.ph`

---

## 🏛️ 8. Comprehensive Nationwide Emergency Directory Hub (Multi-Agency & Private/Public Grid)
The application integrates a hyper-accurate, offline-first national database spanning all regions of the Philippines, dynamically sorted using hardware GPS geofencing to present the closest responders, social services, and utility offices first:

### **A. National Unified & Emergency Hotlines**
- **National Emergency 911:** `911`
- **PNP Command Center (National HQ):** `(02) 8537-4600` / Mobile: `0917-311-9570` / `0998-535-7250`
- **NDRRMC Operations Center (National):** `(02) 8911-1406` / `(02) 8912-2665` / `(02) 8911-5061`
- **Philippine Red Cross National HQ:** `143` / `(02) 8790-2300`
- **Bureau of Fire Protection (BFP Direct):** `(02) 8426-0219` / `(02) 8426-0246`
- **Philippine Coast Guard (PCG):** `(02) 8527-8481` / Mobile: `0917-724-3682`

### **B. Social Security, Labor & Public Welfare Agencies (Geo-Mapped & Contactable)**
* *Mapped dynamically to user location with full telephony, email, and website integration.*
- **SSS (Social Security System Main & Regional Branches):**
  - Hotline: `(02) 8920-6446` | Email: `member_relations@sss.gov.ph` | Website: `sss.gov.ph`
- **PhilHealth (Philippine Health Insurance Corporation):**
  - Hotline: `(02) 8662-2588` | Email: `actioncenter@philhealth.gov.ph` | Website: `philhealth.gov.ph`
- **Pag-IBIG Fund (HDMF):**
  - Hotline: `(02) 8724-4244` | Email: `contactus@pagibigfund.gov.ph` | Website: `pagibigfund.gov.ph`
- **DOTr & DOLE (Labor & Transport Welfare):**
  - DOLE Hotline: `1349` | Email: `hotline1349@dole.gov.ph` | Website: `dole.gov.ph`

### **C. Public & Private Hospitals / Medical Centers (Geo-Mapped Nationwide)**
* *Includes both public health institutions and major private hospital groups with full public contact details and map pin integration.*
- **Public:**
  - **Philippine General Hospital (PGH Manila):** `(02) 8554-8400` | Email: `pgh.gov.ph@gmail.com` | Website: `pgh.gov.ph`
  - **East Avenue Medical Center (Quezon City):** `(02) 8928-0611` | Email: `eamc@doh.gov.ph` | Website: `eamc.doh.gov.ph`
  - **Vicente Sotto Memorial Medical Center (Cebu):** `(032) 253-9891` | Email: `vsmmc@doh.gov.ph` | Website: `vsmmc.doh.gov.ph`
- **Private (Publicly Accessible Emergency Desks):**
  - **Makati Medical Center (Makati):** `(02) 8888-8999` | Email: `mmc@makatimed.net.ph` | Website: `makatimed.net.ph`
  - **St. Luke’s Medical Center (Global City / QC):** `(02) 8789-7700` | Email: `customercare@stlukes.com.ph` | Website: `stlukes.com.ph`
  - **Cardinal Santos Medical Center (San Juan):** `(02) 8727-0001` | Email: `csmc@cardinalsantos.com.ph` | Website: `cardinalsantos.com.ph`
  - **The Medical City (Pasig / Ortigas):** `(02) 8988-1000` | Email: `mail@themedicalcity.com` | Website: `themedicalcity.com`
  - **Asian Hospital and Medical Center (Muntinlupa):** `(02) 8771-9000` | Email: `info@asianhospital.com` | Website: `asianhospital.com`

### **D. Utility & Life Support Providers (Power & Water)**
- **MERALCO:** `16211` / Text: `0920-971-6211` | Email: `customercare@meralco.com.ph` | Website: `meralco.com.ph`
- **Maynilad Water Services:** `1626` | Email: `customer.help@mayniladwater.com.ph` | Website: `mayniladwater.com.ph`
- **Manila Water:** `1627` | Email: `customercare@manilawater.com` | Website: `manilawater.com`

### **E. Uniformed Services & Defense (AFP & PCG)**
- **AFP Command Center:** `(02) 8911-6001` loc 3222 / Mobile: `0917-853-3951` | Email: `afp.pao@gmail.com` | Website: `afp.mil.ph`
- **Philippine Army (PA Operations):** `(02) 8845-9555` | Email: `army.pao@army.mil.ph` | Website: `army.mil.ph`
- **Philippine Navy (PN Watch Center):** `(02) 8525-8320` | Email: `navy.pao@navy.mil.ph` | Website: `navy.mil.ph`
- **Philippine Air Force (PAF Operations):** `(02) 8854-6701` | Email: `paf.pao@mil.ph` | Website: `paf.mil.ph`
- **PCG 24/7 Action Center:** `(02) 8527-3877` | Email: `pcg_actioncenter@coastguard.gov.ph` | Website: `coastguard.gov.ph`

### **F. Motorist, Transport & Road Assistance (LTFRB, LTO, DPWH, DOTr, MMDA, Expressways)**
- **LTFRB 24/7 Hotline:** `1342` / Text/Viber: `0921-448-7777` | Email: `complaints@ltfrb.gov.ph` | Website: `ltfrb.gov.ph`
- **LTO Hotline & Assistance:** Text `LTOHELP` to `2600` / Trunkline: `(02) 8922-9061` | Email: `lto@lto.gov.ph` | Website: `lto.gov.ph`
- **DPWH 24/7 Road Rescue Hotline:** `165-02` | Email: `co@dpwh.gov.ph` | Website: `dpwh.gov.ph`
- **DOTr Action Center:** `7890` | Email: `dotr.actioncenter@dotr.gov.ph` | Website: `dotr.gov.ph`
- **MMDA Metrobase & Rescue:** `136` / `(02) 8882-4151` | Email: `metrobase@mmda.gov.ph` | Website: `mmda.gov.ph`
- **Expressways:**
  - **NLEx:** `(02) 8350-0000` | Email: `customercare@nlex.com.ph` | Website: `nlex.com.ph`
  - **SLEX / Skyway:** `(02) 8824-2282` | Email: `customercare@slex.ph` | Website: `slex.ph`
  - **CAVITEX:** `(02) 8825-4004` | Email: `customercare@cavitex.ph` | Website: `cavitex.ph`

### **G. Regional, Provincial & Municipal Hubs (Luzon, Visayas, Mindanao)**
- **CAR & Baguio:** BCPO `(074) 442-1215`, BFP `(074) 442-2222`, Benguet General Hospital `(074) 422-2936`, CDRRMO `(074) 442-1900`
- **Ilocos Region:** Laoag Police `(077) 772-0402`, ITRMC La Union `(072) 607-6421`, Dagupan Police `(075) 522-1160`
- **Cagayan Valley:** Tuguegarao Police `(078) 844-1234`, Isabela PPO `(078) 624-0555`, CVMC `(078) 396-1550`
- **Central Luzon:** Angeles Police `(045) 892-2302`, Bulacan PPO `(044) 791-1256`, Olongapo Police `(047) 222-2929`
- **CALABARZON:** Cavite PPO `(046) 471-2920`, Laguna PPO `(049) 501-1377`, Batangas PPO `(043) 723-2335`, Rizal PPO `(02) 8696-1520`, Quezon PPO `(042) 710-5321`
- **MIMAROPA:** Puerto Princesa Police `(048) 434-2735`, Oriental Mindoro Police `(043) 288-7010`
- **Bicol Region:** Legazpi Police `(052) 481-1234`, Naga Police `(054) 473-1234`, BRTTH `(052) 483-0057`
- **Western Visayas:** Iloilo City Police `(033) 337-1337`, Bacolod Police `(034) 433-6171`, WVMC `(033) 321-2841`
- **Central Visayas:** Cebu City Police `(032) 255-6845`, Mandaue Police `(032) 344-0300`, VSMMC `(032) 253-9891`, Bohol DRRMO `(038) 501-8317`
- **Eastern Visayas:** Tacloban Police `(053) 832-1234`, EVRMC `(053) 832-0177`
- **Zamboanga Peninsula:** Zamboanga City Center `(062) 991-1002`, ZC MC `(062) 991-2933`
- **Northern Mindanao:** CDO Police `(088) 857-1234`, NMMC `(088) 856-4147`, Iligan Police `(063) 221-1234`
- **Davao Region:** Davao City Police `(082) 224-1311`, SPMC `(082) 227-2731`, CDRRMO `(082) 911-0115`
- **SOCCSKSARGEN:** GenSan Police `(083) 552-3030`, Koronadal Police `(083) 228-1234`
- **Caraga Region:** Butuan Police `(085) 341-1234`, Surigao Police `(086) 826-1234`
- **BARMM:** Cotabato City Police `(064) 421-1234`, Marawi Police `0998-598-6345`
