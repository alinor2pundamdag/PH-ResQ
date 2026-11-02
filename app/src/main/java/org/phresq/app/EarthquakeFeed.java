package org.phresq.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/** Public USGS observations, filtered for the Philippines. Not PHIVOLCS warnings. */
final class EarthquakeFeed {
    private static final String URL_STRING = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";
    private static final String PREFS = "phresq_public_cache";
    private static final int MAX_BYTES = 2_000_000;
    interface Callback { void done(String status); }

    static String cached(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String text = prefs.getString("summary", "");
        long time = prefs.getLong("fetched_at", 0);
        if (time == 0 || text.isEmpty()) return "Wala pang na-download na earthquake observations. Walang live alert.";
        return "USGS observation · nakuha " + DateFormat.getDateTimeInstance().format(new Date(time))
                + " (maaaring luma; hindi PHIVOLCS warning)\n" + text;
    }

    static void refresh(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        new Thread(() -> {
            String outcome = fetchAndStore(app);
            if (callback != null) new Handler(Looper.getMainLooper()).post(() -> callback.done(outcome));
        }, "PHResQ-feed").start();
    }

    static String fetchAndStore(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = cm == null ? null : cm.getActiveNetworkInfo();
        if (active == null || !active.isConnected()) return "Offline: pinanatili ang huling naka-cache na data.";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(URL_STRING).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/geo+json, application/json");
            if (connection.getResponseCode() != 200) return "Hindi ma-download ang USGS data; hindi binago ang cache.";
            int len = connection.getContentLength();
            if (len > MAX_BYTES) return "Sobrang laki ng feed; hindi binago ang cache.";
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (bytes.size() + read > MAX_BYTES) return "Sobrang laki ng feed; hindi binago ang cache.";
                    bytes.write(buffer, 0, read);
                }
            }
            JSONObject root = new JSONObject(bytes.toString("UTF-8"));
            if (!"FeatureCollection".equals(root.optString("type"))) return "Hindi kilalang feed format; hindi binago ang cache.";
            JSONArray events = root.getJSONArray("features");
            StringBuilder text = new StringBuilder();
            long now = System.currentTimeMillis();
            int count = 0;
            for (int i = 0; i < events.length() && count < 10; i++) {
                JSONObject event = events.optJSONObject(i);
                if (event == null) continue;
                JSONObject properties = event.optJSONObject("properties");
                JSONObject geometry = event.optJSONObject("geometry");
                if (properties == null || geometry == null) continue;
                JSONArray coordinates = geometry.optJSONArray("coordinates");
                if (coordinates == null || coordinates.length() < 2) continue;
                double longitude = coordinates.optDouble(0, Double.NaN);
                double latitude = coordinates.optDouble(1, Double.NaN);
                long occurred = properties.optLong("time", 0);
                double magnitude = properties.optDouble("mag", Double.NaN);
                if (!(latitude >= 4 && latitude <= 22 && longitude >= 116 && longitude <= 128)
                        || Double.isNaN(magnitude) || occurred <= 0 || occurred > now + 300000
                        || now - occurred > 48L * 60 * 60 * 1000) continue;
                String place = properties.optString("place", "Hindi tukoy").replace('\n', ' ').trim();
                if (place.length() > 100) place = place.substring(0, 100);
                text.append("M").append(String.format(java.util.Locale.US, "%.1f", magnitude))
                        .append(" · ").append(place).append(" · ")
                        .append(DateFormat.getDateTimeInstance().format(new Date(occurred))).append('\n');
                count++;
            }
            if (count == 0) text.append("Walang PH-area events sa USGS all-day feed na pumasa sa filter.");
            boolean saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString("summary", text.toString()).putLong("fetched_at", now).commit();
            return saved ? "Na-update ang USGS observations; hindi ito emergency warning." : "Hindi na-save ang feed; nanatili ang dating cache.";
        } catch (Exception e) {
            return "Hindi ma-update ang feed; nanatili ang dating cache.";
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    static boolean clear(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit();
    }
}
