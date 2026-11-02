package org.phresq.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Device-bound local vault. No export/backup: loss of device/Keystore key loses the data. */
final class LocalVault {
    private static final String ALIAS = "phresq_local_v1";
    private static final String PREFS = "phresq_vault_v1";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private final SharedPreferences preferences;

    LocalVault(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        KeyStore.Entry existing = store.getEntry(ALIAS, null);
        if (existing instanceof KeyStore.SecretKeyEntry) return ((KeyStore.SecretKeyEntry) existing).getSecretKey();
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build());
        return generator.generateKey();
    }

    /** Returns false when encryption or a durable preference write fails; never writes plaintext. */
    boolean put(String slot, String plain) {
        if (!validSlot(slot)) return false;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key());
            byte[] nonce = cipher.getIV();
            if (nonce == null || nonce.length != 12) return false;
            cipher.updateAAD(slot.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            String envelope = "v1:" + Base64.encodeToString(nonce, Base64.NO_WRAP) + ":"
                    + Base64.encodeToString(encrypted, Base64.NO_WRAP);
            return preferences.edit().putString(slot, envelope).commit();
        } catch (Exception e) {
            return false;
        }
    }

    /** Null indicates corruption/key loss, never confused with an empty stored value. */
    String get(String slot) {
        if (!validSlot(slot)) return null;
        String value = preferences.getString(slot, null);
        if (value == null) return "";
        try {
            String[] parts = value.split(":", -1);
            if (parts.length != 3 || !"v1".equals(parts[0])) return null;
            byte[] nonce = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] ciphertext = Base64.decode(parts[2], Base64.NO_WRAP);
            if (nonce.length != 12 || ciphertext.length < 16) return null;
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(slot.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    boolean clear() { return preferences.edit().clear().commit(); }
    private static boolean validSlot(String slot) {
        return "contact".equals(slot) || "incident_note".equals(slot);
    }
}
