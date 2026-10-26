# PH-ResQ Vanguard 2026: Master Architecture & Implementation Plan

This repository contains the master architectural blueprint, features, legal compliance framework, device permissions policy, cybersecurity standards, user-defined PIN vault mechanism, user-consent emergency data sharing framework, and comprehensive nationwide emergency directory for **PH-ResQ Vanguard 2026**, a 100% functional, secure, and nationwide emergency application built for Filipinos.

---

## 🏛️ 1. Executive Summary & Core Design Principles
- **Name:** PH-ResQ Vanguard 2026
- **Target Users:** All Filipino citizens, commuters, families, students, motorists, senior citizens, PWDs (Persons with Disabilities), workers, OFWs, pet lovers/owners, and residents across **all 17 Regions, 82 Provinces, 149 Cities, 1,493 Municipalities, 42,046 Barangays, every street, and coastal/marine boundaries** of Luzon, Visayas, and Mindanao.
- **Design Philosophy:** Clean, professional UI where unnecessary elements are hidden, and vital emergency tools are immediately tappable. Low-end device optimized with a lightweight APK footprint, neon sci-fi glassmorphic aesthetic, and immersive visual identity.
- **Header & Visual FX Identity:**
  - **Infinite Philippine Flag Color-Looping Main Header:** The main header title (**"PH-ResQ"**) features an infinite, smooth animated color gradient flowing continuously from left to right using the official colors of the Philippine flag (Yellow, Blue, Red, White). The color wave loops endlessly across the letters without disrupting readability, paired with a 100% looping wind-swaying Philippine flag canvas physics simulation as the application's immersive live background.

---

## 📱 2. Advanced Responsive UI & Device-Adaptive Layout Architecture
- **Automatic Notch & Status Bar Compatibility (Safe Area Insets):** 
  - The UI utilizes dynamic CSS/Android safe-area padding (`env(safe-area-inset-top)`, `windowInsets`) ensuring that no critical headers, titles, or status indicators ever get hidden behind phone hardware notches, punch-hole cameras, curved screen edges, or the device notification/battery status bar across any Android model or screen ratio.
- **Universal Single-Column Scrollable Main Hub with Sticky Tappable Mode Switcher & Global Search Bar:**
  - A unified, scrollable main interface where **everything is accessible in one place**.
  - **Sticky Global Smart Search Bar (`[🔍 Search nationwide directory...]`):** Positioned prominently right below the header/mode switcher. Powered by a lightning-fast local IndexedDB regex/fuzzy search engine, it instantly queries the entire offline database (all 42,046 barangays, agencies, hospitals, hotlines, schools, talyer, and vet clinics).
  - **Interactive Instant Search Dropdown & Detail Navigation:** As the user types (e.g., `"ched"`), matching results appear dynamically in a clean scrollable list directly underneath the search bar with instant summary details (Name, Category, Distance/Location). Tapping any search result seamlessly navigates the user directly to that specific asset's **Dedicated Detail Page**, displaying comprehensive information (hotlines, emails, official web portals, operating hours, GPS coordinates, and direct action triggers).
  - Features an intuitive **Sticky Tappable Switcher bar** allowing instant switching between **General / All Mode, Student Mode, Motorist Mode, Senior Citizen & PWD Mode, Worker & OFW Mode, and Pet Owner Mode**.
- **Tappable Icon-Driven Buttons (`[Icon] + [Label]`):**
  - Every button, card, and action trigger is explicitly designed with a clear icon coupled with bold descriptive text underneath, optimized for senior citizens, PWDs, and users with impaired vision.
  - Zero horizontal overflow (no horizontal scrolling required). All elements perfectly wrap to fit any screen width.
- **Interactive Proximity Map Page (`[🗺️ Map View]` Button):**
  - Tapping the map button launches the full interactive map view.
  - **Hybrid Geo-Engine & Local Spatial Indexing:** Operates fully offline using pre-loaded local MBTiles / vector tiles and IndexedDB spatial indexes. When connected to the internet, it seamlessly adapts and integrates with Google Maps / device geolocation services for pin-point live accuracy.
  - **Dynamic List Integration:** The map displays geo-pins on top, while the bottom section features a cleanly sorted, scrollable list of the closest emergency assets, hospitals, police stations, convenience stores, repair shops, and vet clinics based on live proximity.
  - **Context-Aware Dynamic Filtering Rule:** While the interactive map operates globally across all facilities, the bottom proximity list and displayed emergency resources automatically filter and adapt based on the active mode tab. Universal core hotlines (911, PNP, NDRRMC, BFP, Philippine Red Cross) remain permanently pinned at the top of every mode list, followed by sector-specific resources (e.g., student support lines for Student Mode, expressway SOS and talyer for Motorist Mode, NCDA/OSCA for Senior & PWD Mode, DOLE/DMW for Worker Mode, and veterinary clinics for Pet Owner Mode).

---

## 📄 3. Defined UI Pages & Application Screen Structure (Core Layout)
The application is structured into a clean, highly optimized multi-page/view architecture designed for rapid navigation on low-end mobile devices:
1. **Main Hub Page (Dashboard, Global Search & Mode Switcher):** The central home view featuring the infinite color-looping header, the wind-swaying Philippine flag canvas background, the global instant search bar with live dropdown results, the sticky mode switcher tabs, emergency 1-tap dial buttons, and the real-time GPS distance-sorted asset list.
2. **Dedicated Resource Detail Page:** The specific landing page triggered when tapping a search result or list item, displaying complete institutional information (Hotlines, Emails, Official Websites, Social Media links, Physical Address, Operating Hours, and GPS Navigation triggers).
3. **Interactive Map View Page (`[🗺️ Map View]`):** Dedicated full-screen or expanded map interface displaying nationwide vector tiles, regional bounds, coastal/marine points, and interactive proximity pins.
4. **Personal Emergency Contacts & Medical ID Vault Page:** Secure local storage interface managing the 10-slot emergency contact vault, user-defined Master PIN/passcode protection, validation rules, and AES-GCM encrypted medical profile.
5. **Directory & Resource Lookup Page:** Comprehensive searchable directory housing all 17 regions, 82 provinces, 149 cities, 1,493 municipalities, and 42,046 barangays, including institutional hotlines, hospitals, convenience stores, and transport guides.
6. **Settings & Offline Sync Page:** Configuration hub managing local database versioning, Wi-Fi maximizer toggles, manual P2P mesh sync triggers, and data privacy disclosures (RA 10173).

---

## 📡 4. Automatic GPS Proximity Calculation & Local Distance Reader
- **Real-Time Distance Computation (Haversine & Local R-Tree Engine):**
  - **Automatic GPS Reading on Main Dashboard & Active Tabs:** Once the device GPS is enabled and the user opens the main page or selects an active mode tab (such as Student, Motorist, or Senior/PWD mode), the application's built-in local script automatically reads the device's precise latitude and longitude.
  - **Live Meter / Kilometer Distance Display:** The app instantly calculates and displays exact distances (e.g., `150m away`, `1.2km away`) right next to every listed emergency asset, hospital, police station, convenience store, talyer, or vet clinic in real time.
  - **Dynamic Proximity Sorting:** Assets are automatically re-sorted from closest to farthest dynamically as the user moves, ensuring immediate visibility of the nearest help without requiring manual refreshes or external server queries.

---

## 🗺️ 5. Optimized Local Data, Storage & Offline Mapping Architecture (~100MB - ~150MB Total Footprint)
- **App Storage Breakdown & Lightweight Optimization:**
  - **Core Application Binary (APK / Frontend Code & UI Assets):** ~20 MB to ~30 MB base download size for rapid installation even on promotional data or low cellular signal.
  - **Exhaustive Nationwide Directory Database (IndexedDB / JSON):** ~10 MB to ~15 MB storing all 42,046 barangays, government desks, hotlines, emails, websites, and public social media handles.
  - **Offline Vector Map Data & MBTiles Caching:** ~50 MB to ~100 MB containing compressed nationwide vector tiles, regional highways, road networks, and barangay boundaries without heavy 3D satellite bloat.
  - **Total Installed Footprint:** ~100 MB to ~150 MB maximum, ensuring complete offline availability across low-end Android devices with zero risk of storage exhaustion.
- **Spatial Indexing & Haversine Proximity Engine:**
  - Local R-Tree spatial indexing in IndexedDB calculates nearest emergency assets in milliseconds using hardware GPS coordinates without requiring server queries.

---

## 🔘 6. Dynamic Role-Based Mode Switcher & Strict UI Filtering Architecture
- **Multi-Mode Selector (Instant UI Adaptation):**
  - **General / All Mode:** Default comprehensive view displaying all essential public services, emergency hotlines, hospitals, convenience stores, and community assets.
  - **Student Mode:** University hotlines, National Youth Commission (NYC), DepEd / CHED action centers, student mental health lines (NCMH 1553, In Touch), class suspension weather tickers, school supplies retail hubs, budget-friendly fast food / student meals, and student safety check-ins.
  - **Motorist Mode:** Expressway SOS hotlines, LTO/LTFRB/DPWH road rescue, digital glovebox, traffic radar, 24/7 convenience stores (7-Eleven, Uncle John's, Alfamart, FamilyMart), highway sari-sari stores, and an exhaustive nationwide directory of auto/motor repair shops, vulcanizing shops, and roadside pit stops across every barangay.
  - **Senior Citizen & PWD Mode:** High-contrast, large-font UI displaying simplified 1-tap emergency calling, offline medical maintenance vault, National Council on Disability Affairs (NCDA), municipal OSCA desks, COSE, PFRD, and MMDA emergency dispatch (`136`).
  - **Worker & OFW Mode:** DOLE (1349), DMW, OWWA (1348), DFA, TESDA, accredited recruitment agencies, SSS/PhilHealth/Pag-IBIG calamity benefit desks, passport/visa processing hubs, temporary shelters, public transport terminals, utility provider desks, and quick dining options.
  - **Pet Owner / Animal Welfare Mode:** Nationwide veterinary clinics, animal hospitals, 24/7 emergency pet care, Biyaya Animal Care, PAWS, Vets in Practice, and animal rescue hotlines.
  - **Offline Mode:** Dedicated toggle forcing 100% local database rendering without network dependency.
- **Stealth Background Lazy Caching & Wi-Fi Maximizer:** 
  - **Initial Ultra-Light APK:** Shipped with a minimal binary size containing core emergency directories.
  - **Stealth Background Downloading:** Upon installation and app launch, the local background script *silently and invisibly* downloads and caches remaining regional data, maps, convenience stores, vet clinics, PWD offices, recruitment agency contacts, and public social media pages piece by piece without showing annoying loading screens.
  - **Wi-Fi Maximizer:** When connected to Wi-Fi, background bandwidth is fully leveraged to fetch the latest nationwide updates without consuming mobile data credits.
- **Unlimited Local Caching (For Local):** All offline-capable directories, contacts, barangay/street map coordinates, operating schedules, convenience stores, vet clinics, PWD assistance desks, fast food delivery hotlines, and auto/motor repair shops are stored locally with zero artificial storage limits (`localStorage` / IndexedDB).
- **Smart Garbage Collector & Version Rotation Agent:** 
  - If a new update arrives, the local routine **instantly replaces** outdated entries (`v_old` -> `v_latest`), purging deprecated data from active UI display memory.
  - If no new updates arrive, stable cached local data remains securely in place indefinitely without deletion, ensuring continuous offline availability.

---

## 🔒 7. Legal Framework, Data Privacy Compliance (RA 10173), Cybersecurity & Permissions Policy
- **Data Privacy Act of 2012 (RA 10173) & NPC Compliance:** 
  - Strict adherence to the principle of proportionality and transparency. All personal emergency contacts and device GPS coordinates remain **strictly local and private** (`localStorage` / IndexedDB) with zero unauthorized cloud transmission.
- **Client-Side Encryption & User-Defined Master PIN Vault:** 
  - The 10 personal emergency contacts and sensitive medical inputs are secured via **AES-GCM client-side encryption**.
  - **User-Defined PIN/Passcode:** The app requires the user to set a personal Master PIN or passcode upon initial vault setup. This PIN derives a device-bound hardware encryption key that locks the 10-slot contact list, preventing unauthorized viewing or physical snooping by others.
  - **Emergency SOS Bypass:** During actual disaster or emergency scenarios, triggering the *"Send Emergency SOS"* action utilizes an internal secure decryption token to dispatch GPS coordinates instantly via SMS without requiring manual PIN entry during crisis moments.
- **User-Consent & Intent-Driven Emergency Data Sharing Policy:**
  - **Zero Silent Tracking (Passive Privacy):** The user's location is never silently transmitted or tracked in the background without explicit user awareness.
  - **Explicit User-Initiated SOS Sharing (Emergency Exception):** During critical disaster or emergency scenarios, when the user actively triggers the *"Send Emergency SOS"* action, the app is explicitly authorized to capture current GPS coordinates (lat/long link) and embed them into an SMS text message (`sms:`) dispatched to the pre-configured 10 personal emergency contacts or emergency hotlines.
  - **Vital Interest Legal Exception:** Location data sharing is strictly confined to user-initiated emergency requests for personal life preservation, fully aligned with the *Vital Interests* exemption under RA 10173 and Google Play emergency app guidelines.
- **Google Play Store Policy & Device Permissions Framework (Zero-Violation Standard):**
  - **Core Functionality Exception & Transparency:** In full compliance with Google Play Developer policies regarding emergency and public safety apps, permissions are requested strictly as core features with clear in-app disclosures:
    1. `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: Foreground GPS proximity calculation and map centering. *No background location tracking.*
    2. `CALL_PHONE` / Intent Dispatch (`tel:`): Safe 1-tap emergency calling via native device dialer (`ACTION_DIAL`).
    3. `SEND_SMS`: User-triggered emergency SOS broadcast with GPS coordinates to the 10-slot emergency contact vault.
    4. `RECEIVE_BOOT_COMPLETED`: Local startup initialization routine for offline safety monitoring.
    5. `INTERNET` / `ACCESS_NETWORK_STATE`: Wi-Fi maximizer bandwidth optimization and zero-rated disaster alert reception.
- **Strict Sanitization & Anti-Injection Guards:** Input handlers utilize rigorous Regex validation filters to block XSS, script injection, and tampering on Name fields (letters/spaces only) and Phone fields (numbers only).
- **Database Integrity & Checksum Verification:** Nationwide JSON directory files are guarded by SHA-256 integrity checks at startup to prevent tampering.
- **Secure Sandbox Isolation:** Strict scheme whitelisting for `tel:`, `sms:`, and `mailto:` intents, prompting users to select from installed secure email clients or web browsers.

---

## 📱 8. 10-Slot Personal Emergency Contact System & Auto-Boot Monitoring
- **Capacity:** Expanded to **10 dedicated emergency contact slots** (For Local).
- **Strict Input Validation & Error Handling (For Local):**
  - **Name Field:** Accepts *letters and spaces only* (For Local). Rejects numbers or prohibited symbols with an immediate error prompt.
  - **Number Field:** Accepts *numbers only* e.g., `09XXXXXXXXX` (For Local). Incomplete or invalid entries will not be saved.
- **Auto-Boot Local Routine:** Configured via internal service scripts and storage listeners to initialize monitoring on device boot (For Local).

---

## 📞 9. Device Integrations, Phonebook Export & App Intent Dispatch
- **Direct Phone Dialer (`tel:`):** Tapping any hotline launches native dialer with pre-filled numbers (For Local).
- **Direct SMS Broadcast (`sms:`):** Triggers multi-recipient emergency SMS broadcasts with GPS coordinates (For Local).
- **Direct Email Dispatch (`mailto:` with App Choices):** Full support for dispatching official inquiry or medical profile emails, prompting the user to select from installed email apps (Gmail, Outlook, native mail clients) (For Online/Connected).
- **Web & Calling App Handlers:** Seamless intent dispatchers allowing users to open official public websites and public social media pages in their preferred mobile browser or app, or place calls using default device telecommunication apps.
- **Save to Phonebook (vCard Export):** Dedicated phonebook icon to instantly save numbers to device contact list (For Local).

---

## 🗺️ 10. Universal Granular Map-Based Proximity Radar (All 42,046 Barangays & Coastal Waters)
- **Exhaustive Nationwide Barangay-Level Geofencing:** Offline-capable GPS geofencing mapping **all 42,046 barangays** across Luzon, Visayas, and Mindanao. Every single barangay hall, local tanod outpost, municipal disaster risk reduction management office (MDRRMO / CDRRMO), public/private hospital, clinic, police station, fire substation, SSS/PhilHealth/Pag-IBIG branch, DMW/OWWA office, PWD municipal desk, veterinary clinic, convenience store, fast food chain, and auto/motor repair shop is indexed and geo-mapped down to the exact street address (For Local / Pre-loaded).
- **Coastal & Marine Proximity Detection:** Specialized positioning for coastal areas, municipal waters, and open seas across all island groups detecting the nearest Philippine Coast Guard (PCG) detachments, Naval outposts, and high-ground tsunami/storm surge evacuation zones.
- **Metro Manila & Regional Transit Navigation Guides:** Step-by-step platform instructions (Northbound/Southbound, left/right door open indicators for interchanges like EDSA, Recto, Cubao, Doroteo Jose) and provincial transport terminal hotlines (For Local).

---

## 🏪 11. Nationwide 24/7 Convenience Stores, Roadside Pit Stops & Sari-Sari Store Networks
- **Geo-Mapped 24/7 Convenience Stores & Rest Stops (Luzon, Visayas, Mindanao):**
  - **7-Eleven Philippines:** Nationwide branch network (24/7). Customer Hotline: `(02) 8714-9581` | Email: `customercare@7-eleven.com.ph` | Website: `7-eleven.com.ph` (For Local / Online)
  - **Uncle John’s (formerly Ministop):** Nationwide branch network (24/7 in select areas). Hotline: `(02) 8397-8500` | Email: `feedback@ministop.com.ph` | Website: `ministop.com.ph` (For Local / Online)
  - **Alfamart Philippines:** Neighborhood convenience stores across Luzon. Hotline: `(02) 8532-7570` | Email: `customercare@alfamart.com.ph` | Website: `alfamart.com.ph` (For Local / Online)
  - **FamilyMart Philippines:** Urban convenience stores. Hotline: `(02) 8249-8310` | Email: `customercare@familymart.com.ph` | Website: `familymart.com.ph` (For Local / Online)
- **Local Barangay Sari-Sari Stores & Roadside Pit Stops:**
  - Integrated local micro-retail hubs, highway sari-sari stores, and rest stops across barangay roads where motorists, truck drivers, and travelers can pull over for drinking water, emergency provisions, and local directions.

---

## 🐾 12. Nationwide Veterinary Clinics, Animal Hospitals & Pet Care Directory
- **Comprehensive Geo-Mapped Animal Care Network (Private & Public - Luzon, Visayas, Mindanao):**
  - **Biyaya Animal Care (Metro Manila / Rizal):** Emergency vet services, rescue, and rehabilitation. Hotline: `0968-738-4888` | Email: `biyayaanimalcare@gmail.com` | Website: `biyayaanimalcare.org` (For Local / Online)
  - **Philippine Animal Welfare Society (PAWS - QC):** Animal shelter, rescue, and veterinary clinic. Hotline: `(02) 8475-1688` | Email: `paws@paws.org.ph` | Website: `paws.org.ph` (For Local / Online)
  - **Vets in Practice Animal Hospital (Manila / Makati / QC):** 24/7 Emergency Veterinary Care. Hotline: `(02) 8846-9555` | Email: `inquiry@vetsinpractice.com.ph` | Website: `vetsinpractice.com.ph` (For Local / Online)
  - **Provincial & City Veterinary Offices (Luzon, Visayas, Mindanao):** Local government veterinary desks across all provinces for livestock health, rabies vaccination, and pet emergency response.

---

## ♿ 13. PWD & Senior Citizen Support, Reporting & Advocacy Framework
- **National Council on Disability Affairs (NCDA):** Government agency protecting PWD rights and handling accessibility complaints and reports. Hotline: `(02) 8932-6422` | Email: `council@ncda.gov.ph` | Website: `ncda.gov.ph` (For Local / Online)
- **Office for Senior Citizens Affairs (OSCA - Municipal / City Level):** Local municipal desks across all 1,493 municipalities and 149 cities for senior ID, benefits, and emergency assistance reporting.
- **Accessible Reporting & Feedback Channel:** Direct offline-to-online reporting form for PWD and senior citizen welfare concerns, violation of accessibility laws (BP 344), and emergency rescue priorities.

---

## ✈️ 14. OFW, First-Time Jobseeker & Migrant Worker Protection Framework (Luzon Arrival Hubs)
- **Dedicated Arrival & Transition Guide for VisMin Travelers:** Specialized offline mapping and support for first-time jobseekers and OFWs arriving in Luzon (ports, NAIA terminals, PITX, provincial bus terminals in Pasay/Cubao/Manila) seeking licensed recruitment agencies, training centers, and government documentation hubs.
- **Government Agencies for Overseas Employment (Hotlines, Emails, Official Web & Public Social Media Pages):**
  - **Department of Migrant Workers (DMW):** Hotline `(02) 8722-1144` | Email: `aksyon@dmw.gov.ph` | Website: `dmw.gov.ph` (For Local / Online)
  - **Overseas Workers Welfare Administration (OWWA):** 24/7 Hotline `1348` | Email: `support@owwa.gov.ph` | Website: `owwa.gov.ph` (For Local / Online)
  - **Department of Foreign Affairs (DFA):** Hotline `(02) 8834-4000` | Email: `ocrg.concerns@dfa.gov.ph` | Website: `dfa.gov.ph` (For Local / Online)
  - **TESDA:** Hotline `(02) 8888-5656` | Email: `contactcenter@tesda.gov.ph` | Website: `tesda.gov.ph` (For Local / Online)

---

## 🍔 15. Nationwide Fast Food, Dining & Coffee Shop Directory (Inclusive - Budget to Premium)
- **Comprehensive Geo-Mapped Food Hubs:** Geo-mapped across all regions, cities, and barangays to serve students, workers, OFWs, and families. Includes operating hours, delivery hotline numbers, email addresses, website links, and public social media handles:
  - **Jollibee Delivery:** `#87000` | Email: `feedback@jollibee.com.ph` | Website: `jollibee.com.ph` (For Local / Online)
  - **McDonald's Delivery:** `(02) 8888-6236` / `#8-6236` | Email: `customercare@ph.mcd.com` | Website: `mcdonalds.com.ph` (For Local / Online)
  - **Greenwich Delivery:** `#5-55-55` | Email: `feedback@greenwich.com.ph` | Website: `greenwich.com.ph` (For Local / Online)
  - **Chowking Delivery:** `#9-88-88` | Email: `feedback@chowking.com` | Website: `chowking.com` (For Local / Online)
  - **Starbucks Philippines:** Customer Care: `(02) 8462-6262` | Email: `info@starbucks.ph` | Website: `starbucks.ph` (For Local / Online)

---

## 🚗 16. Motorist, Auto Repair, Motor Mechanic & Vulcanizing Road Safety Framework
- **Exhaustive Nationwide Auto & Motor Repair / Vulcanizing Directory (Every Municipality & Barangay):**
  - Pre-loaded database covering every town and barangay across Luzon, Visayas, and Mindanao for stranded motorists:
    - **Auto Repair Shops & Service Centers:** Mechanical diagnostics, battery jumpstart, and towing hotlines.
    - **Motorcycle Repair & Maintenance Shops:** Local "talyer ng motor", spare parts hubs, and emergency roadside tune-ups for riders.
    - **Vulcanizing Shops (24/7 Emergency Flat Tire Assistance):** Geotagged local vulcanizing shops noting operating hours with direct dial links.
- **Real-Time Expressway & Highway SOS Dispatch:** Dedicated offline-first dialing and SMS channels for all major Philippine expressways (NLEx, SLEX, Skyway, TPLEX, CAVITEX, SCTEX) with automated GPS mile-marker tagging (For Local).

---

## 📡 17. Smart Hybrid Sync & "Free Data / Wi-Fi Maximizer" Architecture
1. **Wi-Fi Maximizer Integration:** When connected to Wi-Fi, the built-in sync routine automatically maximizes search and download threads to pull down the latest nationwide news, directory patches, and emergency updates without using mobile data.
2. **RA 10639 Free Mobile Disaster Alerts Integration:** Built-in broadcast receiver to ingest zero-rated government/telco emergency warnings (For Local / Broadcast).
3. **100% Offline Pre-Loaded Core Database:** Complete nationwide directory down to all 42,046 barangays, convenience stores, vet clinics, PWD offices, motorist expressways, auto/motor repair shops, fast food delivery hotlines, and transport guides baked directly into the installation binary (For Local).
4. **P2P Local Mesh Sharing:** Ability to sync emergency updates and directory patches locally between nearby devices via Bluetooth Low Energy (BLE) or Wi-Fi Direct (For Local).

---

## 🏛️ 18. Comprehensive Nationwide Emergency Directory Hub (All 17 Regions, Provinces, Cities & 42,046 Barangays)
The application integrates a massive, exhaustive offline-first national database spanning **all 17 Regions, 82 Provinces, 149 Cities, 1,493 Municipalities, and 42,046 Barangays** across Luzon, Visayas, and Mindanao, dynamically sorted using hardware GPS proximity calculations. Every listing with a physical location, contact number, email, and public social media page (Facebook, TikTok, official portal) is automatically detected by the map (For Local):

### **A. National Unified & Emergency Hotlines**
- **National Emergency 911:** `911` (For Local)
- **PNP Command Center (National HQ):** `(02) 8537-4600` | Email: `pnp.cio@pnp.gov.ph` | Website: `pnp.gov.ph` (For Local / Online)
- **NDRRMC Operations Center:** `(02) 8911-1406` | Email: `ndrrmoc@ocd.gov.ph` | Website: `ndrrmc.gov.ph` (For Local / Online)
- **Philippine Red Cross:** `143` | Email: `secgen@redcross.org.ph` | Website: `redcross.org.ph` (For Local / Online)
- **Bureau of Fire Protection (BFP):** `(02) 8426-0219` | Email: `bfp_nhq@bfp.gov.ph` | Website: `bfp.gov.ph` (For Local / Online)
- **Philippine Coast Guard (PCG):** `(02) 8527-8481` | Email: `pcg_actioncenter@coastguard.gov.ph` | Website: `coastguard.gov.ph` (For Local / Online)

### **B. Public & Private Hospitals / Medical Centers (Geo-Mapped Nationwide - Hotlines, Emails, Websites & Public Social Media Pages)**
- **Public (Operating Hours & ER 24/7):**
  - **PGH Manila:** `(02) 8554-8400` (ER: 24/7) | Email: `pgh.gov.ph@gmail.com` | Website: `pgh.gov.ph` (For Local / Online)
  - **East Avenue Medical Center (QC):** `(02) 8928-0611` (ER: 24/7) | Email: `eamc@doh.gov.ph` | Website: `eamc.doh.gov.ph` (For Local / Online)
  - **Vicente Sotto Memorial Medical Center (Cebu):** `(032) 253-9891` (ER: 24/7) | Email: `vsmmc@doh.gov.ph` | Website: `vsmmc@doh.gov.ph` (For Local / Online)
  - **Southern Philippines Medical Center (Davao):** `(082) 227-2731` (ER: 24/7) | Email: `spmc@doh.gov.ph` | Website: `spmc@doh.gov.ph` (For Local / Online)
- **Private (Operating Hours & ER 24/7):**
  - **Makati Medical Center:** `(02) 8888-8999` (ER: 24/7) | Email: `mmc@makatimed.net.ph` | Website: `makatimed.net.ph` (For Local / Online)
  - **St. Luke’s Medical Center (Global City / QC):** `(02) 8789-7700` (ER: 24/7) | Email: `customercare@stlukes.com.ph` | Website: `stlukes.com.ph` (For Local / Online)
  - **Cardinal Santos Medical Center (San Juan):** `(02) 8727-0001` (ER: 24/7) | Email: `csmc@cardinalsantos.com.ph` | Website: `cardinalsantos.com.ph` (For Local / Online)
  - **The Medical City (Ortigas):** `(02) 8988-1000` (ER: 24/7) | Email: `mail@themedicalcity.com` | Website: `themedicalcity.com` (For Local / Online)
  - **Asian Hospital (Muntinlupa):** `(02) 8771-9000` (ER: 24/7) | Email: `info@asianhospital.com` | Website: `asianhospital.com` (For Local / Online)
