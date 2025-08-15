package client;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CryptoUtilitis {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    // AES key và IV (16 bytes)
    private final String aesKey = "1234567890123456";
    private final String aesIV = "1234567890123456";

    public CryptoUtilitis() throws Exception {
        loadKeys();
    }

    private void loadKeys() throws Exception {
        // Đọc và giải mã Private Key
        String privateKeyPEM = new String(Files.readAllBytes(Paths.get("src/main/resources/private_key_pkcs8.pem")));
        privateKeyPEM = privateKeyPEM
                .replaceAll("-----BEGIN .*-----", "")
                .replaceAll("-----END .*-----", "")
                .replaceAll("\\s", ""); // Xóa dòng trắng và xuống dòng
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(spec);

        // Đọc và giải mã Public Key
        String publicKeyPEM = new String(Files.readAllBytes(Paths.get("src/main/resources/public_key.pem")));
        publicKeyPEM = publicKeyPEM
                .replaceAll("-----BEGIN .*-----", "")
                .replaceAll("-----END .*-----", "")
                .replaceAll("\\s", ""); // Xóa dòng trắng và xuống dòng
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = kf.generatePublic(pubSpec);
    }

    public String signMessage(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        byte[] digitalSignature = signature.sign();
        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    public String encryptPublicKey() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(aesIV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] publicKeyBytes = publicKey.getEncoded();
        byte[] encrypted = cipher.doFinal(publicKeyBytes);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}