package org.phresq.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

/** Minimal offline emergency helper. Does NOT dispatch messages automatically. */
public final class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 7;
    private static final String PREFS = "phresq_local";
    private LocalVault vault;
    private TextView contactStatus;
    private TextView locationStatus;
    private Location lastLocation;
    private boolean locationFresh;
    private long holdStarted;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        vault = new LocalVault(this);
        if (getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean("privacy_v1", false)) { FeedJob.schedule(this); showDashboard(); }
        else showPrivacy();
    }

    private static final int NAVY = Color.rgb(5, 39, 92);
    private static final int BLUE = Color.rgb(12, 91, 190);

    private LinearLayout page(String heading, String subtitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 26, 24, 32);
        root.setBackgroundColor(Color.rgb(235, 244, 255));
        scroll.addView(root);
        TextView brand = info(root, heading);
        brand.setTextColor(NAVY);
        brand.setTextSize(28);
        brand.setTypeface(null, android.graphics.Typeface.BOLD);
        info(root, subtitle);
        setContentView(scroll);
        return root;
    }

    private void showPrivacy() {
        LinearLayout root = page("🇵🇭 PH-ResQ", "PRIVACY AT KALIGTASAN · Basahin bago magpatuloy");
        title(root, "Paano gumagana ang app", 22);
        info(root, "Lokal sa iyong device ang naka-encrypt na emergency contact at ang iyong privacy choice. Walang account, cloud sync o background tracking. Hindi iniimbak ng app ang lokasyon: kukunin lamang ito kapag hiniling mo. Maaaring ilipat ang SMS draft sa SMS app; ikaw ang magpapadala nito. Hindi tiyak ang pagdating ng tawag o SMS.");
        info(root, "Maaari mong tanggihan ang opsyonal na pag-save ng contact. Mananatiling mababasa ang gabay sa kaligtasan at maaari mong buksan ang dialer. Walang pagsang-ayon sa Apache license na kailangan upang basahin ang gabay.");
        info(root, "Kapag tinanggap, mase-save sa device ang privacy choice at maaari mong burahin ito sa Settings. Hihingin nang hiwalay ang location permission kapag ginamit mo ang location button.");
        button(root, "Tanggapin at magpatuloy", () -> {
            boolean saved = getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean("privacy_v1", true).commit();
            if (saved) { FeedJob.schedule(this); showDashboard(); }
            else Toast.makeText(this, "Hindi na-save ang pagpili. Subukan muli.", Toast.LENGTH_LONG).show();
        });
        button(root, "Tumanggi — basahin ang gabay", this::showGuidance);
    }

    private void showDashboard() {
        LinearLayout root = page("🇵🇭 PH-ResQ", "Be ready. Be safe. Be resilient. · ANDROID");
        card(root, "⚠  WALANG LIVE DISASTER ALERT", "Hindi nakakonekta sa opisyal na weather o hazard feed. Huwag umasa rito para sa babala.", Color.rgb(170, 36, 36), null);
        card(root, "EARTHQUAKE OBSERVATIONS", EarthquakeFeed.cached(this), NAVY, null);
        button(root, "I-refresh ang USGS observations", () -> EarthquakeFeed.refresh(this, result -> { Toast.makeText(this, result, Toast.LENGTH_LONG).show(); showDashboard(); }));
        title(root, "Mga kasangkapan", 21);
        LinearLayout row1 = row(root);
        tile(row1, "SOS\nEmergency", Color.rgb(213, 35, 45), this::showHome);
        tile(row1, "Report\nIncident", Color.rgb(18, 106, 207), this::showIncident);
        tile(row1, "View\nMap", Color.rgb(43, 148, 66), this::showMap);
        LinearLayout row2 = row(root);
        tile(row2, "Hazard\nGuide", Color.rgb(224, 128, 22), this::showGuidance);
        tile(row2, "Evacuation\nCenters", Color.rgb(101, 58, 181), () -> unavailable("Walang verified offline evacuation-center dataset o real-time capacity."));
        tile(row2, "Community\nChat", Color.rgb(13, 148, 169), () -> unavailable("Walang chat o mesh transport sa bersyong ito."));
        LinearLayout row3 = row(root);
        tile(row3, "Emergency\nContacts", Color.rgb(47, 154, 53), this::showHome);
        tile(row3, "Weather\nUpdate", Color.rgb(24, 110, 205), () -> unavailable("Walang live PAGASA feed. Tingnan ang opisyal na source kapag may internet."));
        tile(row3, "Settings", Color.rgb(99, 55, 177), this::showSettings);
        info(root, "Prototype only · Walang automated dispatch, live data, offline maps o rescue guarantee.");
    }

    private LinearLayout row(LinearLayout root) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }
    private void tile(LinearLayout row, String label, int color, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setMinHeight(104);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 112, 1);
        params.setMargins(2, 4, 2, 4);
        row.addView(b, params);
        b.setOnClickListener(v -> action.run());
    }
    private void card(LinearLayout root, String heading, String detail, int color, Runnable action) {
        TextView panel = info(root, heading + "\n" + detail);
        panel.setTextColor(Color.WHITE);
        panel.setTextSize(16);
        panel.setPadding(18, 18, 18, 18);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(22);
        panel.setBackground(bg);
        if (action != null) panel.setOnClickListener(v -> action.run());
    }
    private void unavailable(String reason) {
        new AlertDialog.Builder(this).setTitle("Hindi pa available").setMessage(reason)
                .setPositiveButton("OK", null).show();
    }
    private void showGuidance() {
        LinearLayout root = page("Gabay sa kaligtasan", "Offline na pangkalahatang gabay · hindi live na babala");
        button(root, "Buksan ang 911 dialer", () -> openDialer("911"));
        card(root, "BAGYO / BAHA", "Makinig sa opisyal na abiso. Lumipat sa mataas at ligtas na lugar kung inutusang lumikas. Iwasan ang tubig-baha at linya ng kuryente.", BLUE, null);
        card(root, "LINDOL", "Drop, Cover, Hold On. Lumayo sa salamin at bumabagsak na bagay. Pagkatapos ng pagyanig, lumabas nang maingat at bantayan ang aftershocks.", NAVY, null);
        card(root, "MEDICAL EMERGENCY", "Tumawag sa lokal na emergency service. Ibigay ang lokasyon at sintomas. Sundin ang instruksyon ng trained responder; huwag umasa sa app bilang medikal na payo.", BLUE, null);
        button(root, "Balik sa privacy screen / dashboard", () -> {
            if (getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean("privacy_v1", false)) { FeedJob.schedule(this); showDashboard(); }
            else showPrivacy();
        });
    }
    private void showMap() {
        unavailable("Wala pang bundled offline map. Maaaring buksan ang ibang map app kung mayroon; maaaring kailangan nito ng internet.");
    }
    private void showIncident() {
        LinearLayout root = page("Report incident", "Personal offline note lamang · HINDI ipinapadala sa responder");
        EditText note = new EditText(this);
        note.setHint("Ano ang nangyari? (max 500 characters)");
        note.setMinLines(4);
        note.setMaxLines(8);
        String storedNote = vault.get("incident_note");
        if (storedNote == null) { unavailable("Hindi mabuksan ang encrypted note. Hindi ito io-overwrite."); showDashboard(); return; }
        note.setText(storedNote);
        root.addView(note);
        button(root, "I-save sa device", () -> {
            String text = note.getText().toString().trim();
            if (text.length() > 500) { Toast.makeText(this, "Hanggang 500 characters lamang.", Toast.LENGTH_LONG).show(); return; }
            boolean ok = vault.put("incident_note", text);
            Toast.makeText(this, ok ? "Na-save sa device; HINDI naipadala." : "Hindi na-save.", Toast.LENGTH_LONG).show();
        });
        button(root, "Burahin ang note", () -> {
            boolean ok = vault.put("incident_note", "");
            if (ok) note.setText("");
            Toast.makeText(this, ok ? "Nabura." : "Hindi nabura.", Toast.LENGTH_LONG).show();
        });
        button(root, "Bumalik sa dashboard", this::showDashboard);
    }
    private void showSettings() {
        LinearLayout root = page("Settings at privacy", "Local settings");
        info(root, "Burahin ang contact, incident note at privacy choice upang bumalik sa unang screen. Hindi ito nagbubura ng draft sa panlabas na SMS app.");
        button(root, "Burahin ang lokal na data", () -> new AlertDialog.Builder(this)
                .setTitle("Burahin ang data?")
                .setMessage("Mawawala ang contact, note at privacy choice sa app na ito.")
                .setNegativeButton("Kanselahin", null)
                .setPositiveButton("Burahin", (d, which) -> {
                    boolean ok = vault.clear();
                    if (ok) ok = getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().commit();
                    if (ok) ok = EarthquakeFeed.clear(this);
                    if (ok) { FeedJob.cancel(this); lastLocation = null; showPrivacy(); }
                    else Toast.makeText(this, "Hindi nabura ang data; subukan muli.", Toast.LENGTH_LONG).show();
                }).show());
        button(root, "Bumalik sa dashboard", this::showDashboard);
    }

    private void showHome() {
        LinearLayout root = page("SOS Emergency", "Manual call at SMS composer · hindi automated dispatch");
        button(root, "← Bumalik sa dashboard", this::showDashboard);
        info(root, "Prototype lamang. Hindi ito automated rescue service. Ang SMS at tawag ay nangangailangan ng cellular signal; ikaw ang magpapadala ng mensahe.");
        title(root, "Kung may agarang panganib", 20);
        button(root, "Buksan ang dialer: 911", () -> openDialer("911"));
        info(root, "Tingnan muna kung ang 911 ang tamang emergency number sa iyong lugar. Kung hindi makatawag, humingi ng tulong sa malapit na tao o opisyal na lokal na serbisyo.");
        title(root, "Emergency contact", 20);
        contactStatus = info(root, "");
        button(root, "I-set ang contact number", this::editContact);
        title(root, "Lokasyon (opsyonal)", 20);
        locationStatus = info(root, "Hindi pa nakuha ang lokasyon. Maaari ka pa ring magpadala ng mensahe nang wala nito.");
        button(root, "Kunin ang last known GPS", this::requestLocation);
        title(root, "SOS message", 20);
        info(root, "Pindutin nang 2 segundo. Bubuksan lamang ang SMS app para sa iyong review at manual send. Walang mensaheng awtomatikong ipinapadala.");
        Button sos = button(root, "PINDUTIN NANG 2 SEGUNDO: SOS", () -> {});
        sos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(175, 25, 25)));
        sos.setTextColor(Color.WHITE);
        sos.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                holdStarted = SystemClock.elapsedRealtime();
                return true;
            }
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (SystemClock.elapsedRealtime() - holdStarted >= 2000) confirmSOS();
                else Toast.makeText(this, "Pindutin nang buong 2 segundo, o gamitin ang accessible button sa ibaba.", Toast.LENGTH_LONG).show();
                return true;
            }
            if (event.getAction() == android.view.MotionEvent.ACTION_CANCEL) { holdStarted = 0; return true; }
            return false;
        });
        button(root, "Accessible alternative: ihanda ang SMS", this::confirmSOS);
        info(root, "Privacy: naka-encrypt sa device lamang ang contact. Walang server, account o background tracking. Maaaring burahin ang contact sa edit dialog. Huwag mag-store ng sensitibong medical data rito.");
        refreshContact();
    }

    private void title(LinearLayout root, String text, int sp) {
        TextView label = info(root, text);
        label.setTextSize(sp);
        label.setTextColor(Color.BLACK);
        label.setPadding(0, 16, 0, 12);
    }

    private TextView info(LinearLayout root, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(17);
        label.setTextColor(Color.rgb(30, 30, 30));
        label.setPadding(0, 8, 0, 12);
        root.addView(label);
        return label;
    }

    private Button button(LinearLayout root, String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setMinHeight(64);
        b.setGravity(Gravity.CENTER);
        root.addView(b, new LinearLayout.LayoutParams(-1, -2));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private String contact() { return vault.get("contact"); }
    private void refreshContact() {
        String number = contact();
        contactStatus.setText(number == null ? "Hindi mabuksan ang naka-encrypt na contact. Huwag itong palitan nang walang backup." : number.isEmpty() ? "Wala pang nakatalagang numero." : "Naka-save: " + number);
    }
    private void editContact() {
        EditText field = new EditText(this);
        field.setInputType(InputType.TYPE_CLASS_PHONE);
        field.setSingleLine(true);
        String savedContact = contact();
        if (savedContact == null) { unavailable("Hindi mabuksan ang vault. Huwag i-overwrite ang data."); return; }
        field.setText(savedContact);
        new AlertDialog.Builder(this).setTitle("Emergency contact (burahin ang laman para alisin)")
                .setView(field).setPositiveButton("I-save", (d, which) -> {
                    String value = field.getText().toString().trim();
                    if (!value.isEmpty() && !value.matches("\\+?[0-9]{7,15}")) {
                        Toast.makeText(this, "Gumamit ng 7–15 digit, maaaring may + sa unahan.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    boolean saved = vault.put("contact", value);
                    Toast.makeText(this, saved ? "Na-save." : "Hindi na-save; subukan muli.", Toast.LENGTH_LONG).show();
                    refreshContact();
                }).setNegativeButton("Kanselahin", null).show();
    }

    private void requestLocation() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        } else readLocation();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_REQUEST) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) readLocation();
            else if (locationStatus != null) locationStatus.setText("Walang pahintulot sa lokasyon. Maaari pa ring mag-SMS nang walang coordinates.");
        }
    }

    private void readLocation() {
        lastLocation = null;
        locationFresh = false;
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (lm != null) {
                for (String provider : lm.getProviders(true)) {
                    if (provider.equals(LocationManager.GPS_PROVIDER) && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) continue;
                    Location candidate = lm.getLastKnownLocation(provider);
                    if (candidate != null && (lastLocation == null || candidate.getTime() > lastLocation.getTime())) lastLocation = candidate;
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            lastLocation = null;
        }
        if (locationStatus == null) return;
        if (lastLocation == null) {
            locationStatus.setText("Walang available na last known location. Hindi ito real-time GPS tracking.");
        } else {
            long age = System.currentTimeMillis() - lastLocation.getTime();
            locationFresh = age >= 0 && age <= 5 * 60 * 1000L;
            String date = DateFormat.getDateTimeInstance().format(new Date(lastLocation.getTime()));
            locationStatus.setText("Last known: " + date + ", accuracy ±" + Math.round(lastLocation.getAccuracy()) + "m. "
                    + (locationFresh ? "Nakuha sa nakaraang 5 minuto." : "LUMA: hindi isasama sa SOS."));
        }
    }

    private void confirmSOS() {
        String recipient = contact();
        if (recipient == null) { unavailable("Hindi mabuksan ang encrypted contact. Walang SMS draft na bubuksan."); return; }
        if (recipient.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Walang contact")
                    .setMessage("Mag-set muna ng emergency contact, o gamitin ang dialer.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        // Recheck age at the point of use; a once-fresh fix can become stale.
        boolean includeLocation = lastLocation != null && locationFresh && System.currentTimeMillis() >= lastLocation.getTime()
                && System.currentTimeMillis() - lastLocation.getTime() <= 5 * 60 * 1000L;
        String message = "EMERGENCY: Kailangan ko ng tulong. Pakitawagan ako at alamin ang aking sitwasyon.";
        if (includeLocation) message += " Huling lokasyon (hindi live): https://maps.google.com/?q="
                + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
        String finalMessage = message;
        new AlertDialog.Builder(this).setTitle("Suriin bago mag-SMS")
                .setMessage("Para sa: " + recipient + "\n\n" + finalMessage + "\n\nBubuksan ang SMS app. IKAW ang pipindot ng Send. Hindi garantisado ang delivery.")
                .setNegativeButton("Kanselahin", null)
                .setPositiveButton("Buksan ang SMS app", (d, which) -> openSms(finalMessage)).show();
    }

    private void openSms(String text) {
        String recipient = contact();
        if (recipient == null || recipient.isEmpty()) { unavailable("Hindi mabuksan ang contact."); return; }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(recipient)));
        intent.putExtra("sms_body", text);
        try { startActivity(intent); }
        catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, "Walang available na SMS app. Tawagan ang contact o humingi ng tulong.", Toast.LENGTH_LONG).show();
        }
    }
    private void openDialer(String number) {
        try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number))); }
        catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, "Walang available na dialer.", Toast.LENGTH_LONG).show();
        }
    }
}
