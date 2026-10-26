# PH-ResQ Vanguard 2026: Master Architecture & Implementation Plan

This repository contains the master architectural blueprint, features, legal compliance framework, and comprehensive nationwide emergency directory for **PH-ResQ Vanguard 2026**, a 100% functional, secure, and nationwide emergency application built for Filipinos.

---

## 🏛️ 1. Executive Summary & Core Design Principles
- **Name:** PH-ResQ Vanguard 2026
- **Target Users:** All Filipino citizens, commuters, families, students, motorists, senior citizens, workers, OFWs, first-time jobseekers arriving in Luzon from Visayas and Mindanao, and residents across **all 17 Regions, 82 Provinces, 149 Cities, 1,493 Municipalities, 42,046 Barangays, every street, and coastal/marine boundaries** of Luzon, Visayas, and Mindanao.
- **Design Philosophy:** Clean, professional UI where unnecessary elements are hidden, and vital emergency tools are immediately tappable. Low-end device optimized with a lightweight APK footprint, neon sci-fi glassmorphic aesthetic, and a 100% looping wind-swaying Philippine flag canvas physics simulation.

---

## 🔘 2. Dynamic Role-Based Mode Switcher & Strict UI Filtering Architecture
- **Multi-Mode Selector (Instant UI Adaptation):** The main interface dynamically morphs and filters displayed content based on the user's selected mode, hiding irrelevant clutter and showing only what is vital to that specific role:
  - **Student Mode:** Filters UI to display university/school hotlines, campus emergency contacts, class suspension weather tickers, school supplies retail hubs, budget-friendly fast food / student meals, and student safety check-ins.
  - **Motorist Mode:** Filters UI to display expressway SOS hotlines, LTO/LTFRB/DPWH road rescue, digital glovebox, traffic radar, and an exhaustive nationwide directory of auto repair shops, motor repair shops, vulcanizing shops (24/7 emergency roadside repair), and towing services across every region, city, and barangay.
  - **Senior Citizen / PWD Mode:** High-contrast, large-font UI displaying simplified 1-tap emergency calling and the offline medical maintenance vault.
  - **Worker & OFW / First-Timer Mode:** Filters UI to display DOLE, DMW, OWWA, accredited recruitment agencies, passport/visa processing hubs, temporary shelters, public transport terminals, utility provider desks, and quick dining options.
  - **Offline Mode:** Dedicated toggle forcing 100% local database rendering without network dependency.
- **Stealth Background Lazy Caching & Wi-Fi Maximizer:** 
  - **Initial Ultra-Light APK:** The app is shipped with a minimal binary size containing core emergency directories.
  - **Stealth Background Downloading:** Upon installation and app launch, the background worker *silently and invisibly* downloads and caches remaining regional data, maps, recruitment agency contacts, public pages, and updates piece by piece without showing annoying loading screens.
  - **Wi-Fi Maximizer:** When connected to Wi-Fi, background bandwidth is fully leveraged to fetch the latest nationwide updates without consuming mobile data credits.
- **Unlimited Local Caching (For Local):** All offline-capable directories, contacts, barangay/street map coordinates, operating schedules, fast food delivery hotlines, recruitment agencies, and auto/motor repair shops are stored locally with zero artificial storage limits (`localStorage` / IndexedDB).
- **Smart Garbage Collector & Version Rotation Agent:** 
  - If a new update arrives, the agent **instantly replaces** outdated entries (`v_old` -> `v_latest`), purging deprecated data from active UI display memory.
  - If no new updates arrive, stable cached local data remains securely in place indefinitely without deletion, ensuring continuous offline availability.

---

## 🔒 3. Legal Framework & Data Privacy Compliance (RA 10173) & Security
- **Data Privacy Act of 2012 (RA 10173) Compliance:** All personal emergency contacts and device GPS coordinates remain **strictly local and private** (`localStorage`) (For Local). No personal data or location is ever uploaded, sold, or transmitted to any external third-party cloud servers without user authorization (For Online sync).
- **Client-Side Encryption (Zero-Knowledge Local Storage):** The 10 personal emergency contacts and sensitive inputs are secured via **AES-GCM client-side encryption** with a secure device-bound key (For Local) to protect against data extraction, malware, or physical snooping.
- **Strict Sanitization & Anti-Injection Guards:** Input handlers utilize rigorous Regex validation filters (For Local) to block XSS, script injection, and tampering on Name fields (letters/spaces only) and Phone fields (numbers only).
- **Secure Sandbox Isolation:** Strict scheme whitelisting for `tel:`, `sms:`, and `mailto:` intents (For Local/Device). When tapping emails or websites, the app presents seamless intent choices to launch the user's preferred installed email client (Gmail, Mail apps) or native web browser.

---

## 📱 4. 10-Slot Personal Emergency Contact System & Auto-Boot Agent
- **Capacity:** Expanded to **10 dedicated emergency contact slots** (For Local).
- **Strict Input Validation & Error Handling (For Local):**
  - **Name Field:** Accepts *letters and spaces only* (For Local). Rejects numbers or prohibited symbols with an immediate error prompt.
  - **Number Field:** Accepts *numbers only* e.g., `09XXXXXXXXX` (For Local). Incomplete or invalid entries will not be saved.
- **Auto-Boot Background Agent:** Configured via Service Workers and Web Broadcast Channels to initialize monitoring on device boot (For Local).

---

## 📞 5. Device Integrations, Phonebook Export & App Intent Dispatch
- **Direct Phone Dialer (`tel:`):** Tapping any hotline launches native dialer with pre-filled numbers (For Local).
- **Direct SMS Broadcast (`sms:`):** Triggers multi-recipient emergency SMS broadcasts with GPS coordinates (For Local).
- **Direct Email Dispatch (`mailto:` with App Choices):** Full support for dispatching official inquiry or medical profile emails, prompting the user to select from installed email apps (Gmail, Outlook, native mail clients) (For Online/Connected).
- **Web & Calling App Handlers:** Seamless intent dispatchers allowing users to open official public websites and public social media pages (Facebook, TikTok, official portals) in their preferred mobile browser or app, or place calls using default device telecommunication apps.
- **Save to Phonebook (vCard Export):** Dedicated phonebook icon to instantly save numbers to device contact list (For Local).

---

## 🗺️ 6. Universal Granular Map-Based Proximity Radar (All 42,046 Barangays & Coastal Waters)
- **Exhaustive Nationwide Barangay-Level Geofencing:** Offline-capable GPS geofencing mapping **all 42,046 barangays** across Luzon, Visayas, and Mindanao. Every single barangay hall, local tanod outpost, municipal disaster risk reduction management office (MDRRMO / CDRRMO), public/private hospital, clinic, police station, fire substation, SSS/PhilHealth/Pag-IBIG branch, DMW/OWWA office, recruitment agency hub, utility desk, retail store, fast food chain, and auto/motor repair shop is indexed and geo-mapped down to the exact street address (For Local / Pre-loaded).
- **Coastal & Marine Proximity Detection:** Specialized positioning for coastal areas, municipal waters, and open seas across all island groups detecting the nearest Philippine Coast Guard (PCG) detachments, Naval outposts, and high-ground tsunami/storm surge evacuation zones.
- **Metro Manila & Regional Transit Navigation Guides:** Step-by-step platform instructions (Northbound/Southbound, left/right door open indicators for interchanges like EDSA, Recto, Cubao, Doroteo Jose) and provincial transport terminal hotlines (For Local).

---

## ✈️ 7. OFW, First-Time Jobseeker & Migrant Worker Protection Framework (Luzon Arrival Hubs)
- **Dedicated Arrival & Transition Guide for VisMin Travelers:** Specialized offline mapping and support for first-time jobseekers and OFWs arriving in Luzon (ports, NAIA terminals, PITX, provincial bus terminals in Pasay/Cubao/Manila) seeking licensed recruitment agencies, training centers, and government documentation hubs.
- **Government Agencies for Overseas Employment (Hotlines, Emails, Official Web & Public Social Media Pages):**
  - **Department of Migrant Workers (DMW / formerly POEA):** Hotline `(02) 8722-1144` | Email: `aksyon@dmw.gov.ph` | Website: `dmw.gov.ph` (For Local / Online)
  - **Overseas Workers Welfare Administration (OWWA):** 24/7 Hotline `1348` | Email: `support@owwa.gov.ph` | Website: `owwa.gov.ph` (For Local / Online)
  - **Department of Foreign Affairs (DFA - Passport & Assistance to Nationals):** Hotline `(02) 8834-4000` | Email: `ocrg.concerns@dfa.gov.ph` | Website: `dfa.gov.ph` (For Local / Online)
  - **Technical Education and Skills Development Authority (TESDA - NC II Training):** Hotline `(02) 8888-5656` | Email: `contactcenter@tesda.gov.ph` | Website: `tesda.gov.ph` (For Local / Online)
- **Licensed Recruitment & Manpower Agency Hubs (Metro Manila / Luzon):** Pre-loaded directory of DMW-accredited placement agencies in Ermita, Malate, Mandaluyong, and Makati with public contact numbers, emails, and official web/social media links to protect first-time applicants from illegal recruiters.

---

## 🍔 8. Nationwide Fast Food, Dining & Coffee Shop Directory (Inclusive - Budget to Premium)
- **Comprehensive Geo-Mapped Food Hubs:** Geo-mapped across all regions, cities, and barangays to serve both students, workers, OFWs, and families (affordability-inclusive). Includes operating hours, delivery hotline numbers, email addresses, website links, and public social media handles:
  - **Jollibee Delivery:** `#87000` | Email: `feedback@jollibee.com.ph` | Website: `jollibee.com.ph` (For Local / Online)
  - **McDonald's Delivery:** `(02) 8888-6236` / `#8-6236` | Email: `customercare@ph.mcd.com` | Website: `mcdonalds.com.ph` (For Local / Online)
  - **Greenwich Delivery:** `#5-55-55` | Email: `feedback@greenwich.com.ph` | Website: `greenwich.com.ph` (For Local / Online)
  - **Chowking Delivery:** `#9-88-88` | Email: `feedback@chowking.com` | Website: `chowking.com` (For Local / Online)
  - **Starbucks Philippines:** Customer Care: `(02) 8462-6262` | Email: `info@starbucks.ph` | Website: `starbucks.ph` (For Local / Online)

---

## 🚗 9. Motorist, Auto Repair, Motor Mechanic & Vulcanizing Road Safety Framework
- **Exhaustive Nationwide Auto & Motor Repair / Vulcanizing Directory (Every Municipality & Barangay):**
  - Pre-loaded database covering every town and barangay across Luzon, Visayas, and Mindanao for stranded motorists:
    - **Auto Repair Shops & Service Centers:** Mechanical diagnostics, battery jumpstart, and towing hotlines.
    - **Motorcycle Repair & Maintenance Shops:** Local "talyer ng motor", spare parts hubs, and emergency roadside tune-ups for riders.
    - **Vulcanizing Shops (24/7 Emergency Flat Tire Assistance):** Geotagged local vulcanizing shops noting operating hours with direct dial links.
- **Real-Time Expressway & Highway SOS Dispatch:** Dedicated offline-first dialing and SMS channels for all major Philippine expressways (NLEx, SLEX, Skyway, TPLEX, CAVITEX, SCTEX) with automated GPS mile-marker tagging (For Local).
- **LTO, LTFRB & DPWH Road Rescue Integration:** Immediate access to vehicular accident reporting, towing services, roadside assistance, and traffic violation assistance hotlines (For Local / Online).
- **Motorist Digital Glovebox (Offline Document Vault):** Encrypted local storage for driver's license details, vehicle registration (OR/CR), and insurance emergency policy numbers (For Local).

---

## 📡 10. Smart Hybrid Sync & "Free Data / Wi-Fi Maximizer" Architecture
To support millions of Filipinos who rely purely on social media or limited promotional data packages ("Free FB/TikTok data") or **Wi-Fi networks**:
1. **Wi-Fi Maximizer Agent:** When connected to Wi-Fi, the background sync agent automatically maximizes search and download threads to pull down the latest nationwide news, directory patches, recruitment updates, and emergency updates without using mobile data.
2. **RA 10639 Free Mobile Disaster Alerts Integration:** Built-in broadcast receiver to ingest zero-rated government/telco emergency warnings (For Local / Broadcast).
3. **Messenger / Lightweight Bot Bridge:** Optional companion lightweight chat channels for querying directory updates (For Online).
4. **100% Offline Pre-Loaded Core Database:** Complete nationwide directory down to all 42,046 barangays, motorist expressways, auto/motor repair shops, vulcanizing stations, OFW recruitment hubs, student retail stores, fast food delivery hotlines, and transport guides baked directly into the installation binary (For Local).
5. **P2P Local Mesh Sharing:** Ability to sync emergency updates and directory patches locally between nearby devices via Bluetooth Low Energy (BLE) or Wi-Fi Direct (For Local).

---

## 🎒 11. Student, Senior Citizens & Workers Protection Framework
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

## 🏛️ 12. Comprehensive Nationwide Emergency Directory Hub (All 17 Regions, Provinces, Cities & 42,046 Barangays)
The application integrates a massive, exhaustive offline-first national database spanning **all 17 Regions, 82 Provinces, 149 Cities, 1,493 Municipalities, and 42,046 Barangays** across Luzon, Visayas, and Mindanao, dynamically sorted using hardware GPS geofencing. Every listing with a physical location, contact number, email, and public social media page (Facebook, TikTok, official portal) is automatically detected by the map (For Local):

### **A. National Unified & Emergency Hotlines**
- **National Emergency 911:** `911` (For Local)
- **PNP Command Center (National HQ):** `(02) 8537-4600` | Email: `pnp.cio@pnp.gov.ph` | Website: `pnp.gov.ph` (For Local / Online)
- **NDRRMC Operations Center:** `(02) 8911-1406` | Email: `ndrrmoc@ocd.gov.ph` | Website: `ndrrmc.gov.ph` (For Local / Online)
- **Philippine Red Cross:** `143` | Email: `secgen@redcross.org.ph` | Website: `redcross.org.ph` (For Local / Online)
- **Bureau of Fire Protection (BFP):** `(02) 8426-0219` | Email: `bfp_nhq@bfp.gov.ph` | Website: `bfp.gov.ph` (For Local / Online)
- **Philippine Coast Guard (PCG):** `(02) 8527-8481` | Email: `pcg_actioncenter@coastguard.gov.ph` | Website: `coastguard.gov.ph` (For Local / Online)

### **B. Public & Private Hospitals / Medical Centers (Geo-Mapped Nationwide - Hotlines, Emails, Websites & Public Social Media Handles)**
- **Public (Operating Hours & ER 24/7):**
  - **PGH Manila:** `(02) 8554-8400` (ER: 24/7) | Email: `pgh.gov.ph@gmail.com` | Website: `pgh.gov.ph` (For Local / Online)
  - **East Avenue Medical Center (QC):** `(02) 8928-0611` (ER: 24/7) | Email: `eamc@doh.gov.ph` | Website: `eamc.doh.gov.ph` (For Local / Online)
  - **Vicente Sotto Memorial Medical Center (Cebu):** `(032) 253-9891` (ER: 24/7) | Email: `vsmmc@doh.gov.ph` | Website: `vsmmc.doh.gov.ph` (For Local / Online)
  - **Southern Philippines Medical Center (Davao):** `(082) 227-2731` (ER: 24/7) | Email: `spmc@doh.gov.ph` | Website: `spmc.doh.gov.ph` (For Local / Online)
- **Private (Operating Hours & ER 24/7):**
  - **Makati Medical Center:** `(02) 8888-8999` (ER: 24/7) | Email: `mmc@makatimed.net.ph` | Website: `makatimed.net.ph` (For Local / Online)
  - **St. Luke’s Medical Center (Global City / QC):** `(02) 8789-7700` (ER: 24/7) | Email: `customercare@stlukes.com.ph` | Website: `stlukes.com.ph` (For Local / Online)
  - **Cardinal Santos Medical Center (San Juan):** `(02) 8727-0001` (ER: 24/7) | Email: `csmc@cardinalsantos.com.ph` | Website: `cardinalsantos.com.ph` (For Local / Online)
  - **The Medical City (Ortigas):** `(02) 8988-1000` (ER: 24/7) | Email: `mail@themedicalcity.com` | Website: `themedicalcity.com` (For Local / Online)
  - **Asian Hospital (Muntinlupa):** `(02) 8771-9000` (ER: 24/7) | Email: `info@asianhospital.com` | Website: `asianhospital.com` (For Local / Online)
