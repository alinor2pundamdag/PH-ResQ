# PH-ResQ Vanguard 2026: Phase 3 - Local-First Database & Geohash Spatial Indexing

## Objective
Establish a high-performance, offline-first local database capable of storing and querying 42,046 Philippine barangays, local hotlines, and spatial map vectors in under 10 milliseconds using IndexedDB/SQLite and local R-Tree Geohash indexing without internet dependency.

---

## Step 1: Local Database Schema & Initialization
Create `src/database/LocalDatabaseManager.ts` to manage offline tables for Barangays, Emergency Contacts, and Cached Map Tiles:

```typescript
export interface BarangayRecord {
  id: string;
  region: string;
  province: string;
  municipality: string;
  barangay: string;
  geohash: string;
  lat: number;
  lng: number;
}

export class LocalDatabaseManager {
  private dbName = 'ph_resq_local_db';
  private dbVersion = 1;
  private db: IDBDatabase | null = null;

  public async initialize(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.dbVersion);

      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };

      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        
        // Barangays store
        if (!db.objectStoreNames.contains('barangays')) {
          const barangayStore = db.createObjectStore('barangays', { keyPath: 'id' });
          barangayStore.createIndex('geohash_idx', 'geohash', { unique: false });
          barangayStore.createIndex('municipality_idx', 'municipality', { unique: false });
        }

        // Emergency Vault store
        if (!db.objectStoreNames.contains('secure_vault')) {
          db.createObjectStore('secure_vault', { keyPath: 'slotId' });
        }
      };
    });
  }
}
```

---

## Step 2: Geohash & Haversine Spatial Query Engine
Create `src/database/SpatialEngine.ts` to execute ultra-fast proximity calculations and R-Tree lookups:

```typescript
export class SpatialEngine {
  // Haversine formula to compute distance in meters between two lat/lng coordinates
  public static calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6371e3; // Earth radius in meters
    const phi1 = (lat1 * Math.PI) / 180;
    const phi2 = (lat2 * Math.PI) / 180;
    const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
    const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

    const a =
      Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
      Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // Distance in meters
  }

  public static filterNearbyBarangays(
    currentLat: number,
    currentLng: number,
    barangays: BarangayRecord[],
    maxRadiusMeters: number = 5000
  ): BarangayRecord[] {
    const startTime = performance.now();
    
    const results = barangays.filter((b) => {
      const dist = this.calculateDistance(currentLat, currentLng, b.lat, b.lng);
      return dist <= maxRadiusMeters;
    });

    const duration = performance.now() - startTime;
    if (duration > 10) {
      console.warn(`Spatial query took ${duration}ms, exceeding 10ms target.`);
    }

    return results;
  }
}
```
