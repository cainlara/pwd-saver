package io.github.cainlara.pwdsaver.credential;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Encrypts/decrypts vaulted credential passwords with AES-256-GCM (FR-012).
 * Unlike the user's own login password (hashed, one-way), a vaulted password
 * must be recoverable verbatim, so it is encrypted rather than hashed.
 */
@Service
public class CredentialEncryptionService {

  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final int GCM_IV_LENGTH_BYTES = 12;

  private final SecretKeySpec secretKey;
  private final SecureRandom secureRandom = new SecureRandom();

  public CredentialEncryptionService(@Value("${credential.encryption.key:}") String base64Key) {
    if (!StringUtils.hasText(base64Key)) {
      throw new IllegalStateException(
          "credential.encryption.key (CREDENTIAL_ENCRYPTION_KEY) must be set to a base64-encoded "
              + "256-bit key");
    }
    byte[] keyBytes = Base64.getDecoder().decode(base64Key);
    if (keyBytes.length != 32) {
      throw new IllegalStateException(
          "credential.encryption.key must decode to exactly 32 bytes (256 bits)");
    }
    this.secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      byte[] combined = new byte[iv.length + ciphertext.length];
      System.arraycopy(iv, 0, combined, 0, iv.length);
      System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
      return Base64.getEncoder().encodeToString(combined);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to encrypt credential password", e);
    }
  }

  public String decrypt(String encoded) {
    try {
      byte[] combined = Base64.getDecoder().decode(encoded);
      byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
      byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH_BYTES];
      System.arraycopy(combined, 0, iv, 0, iv.length);
      System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

      Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to decrypt credential password", e);
    }
  }
}
