package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.json.JSONObject;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private final DatabaseManager dbManager = new DatabaseManager();
    private final ServerGUI gui;

    public ServerHandler(ServerGUI gui) {
        this.gui = gui;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        try {
            gui.appendLog("Nhận yêu cầu: " + msg + "\n");

            JSONObject json = new JSONObject(msg);
            String encodedMessage = json.getString("message");
            String signature = json.getString("signature");
            String encryptedPublicKey = json.getString("publicKey");

            String[] keyInfo = dbManager.getKeyInfo();
            String aesKey = keyInfo[0];
            String aesIV = keyInfo[1];

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesIV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] publicKeyBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPublicKey));

            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = kf.generatePublic(pubSpec);

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(Base64.getDecoder().decode(encodedMessage));
            boolean verified = sig.verify(Base64.getDecoder().decode(signature));

            if (verified) {
                String message = new String(Base64.getDecoder().decode(encodedMessage));
                gui.appendLog("Xác thực thành công cho tin nhắn: " + message + "\n");
                SubdomainScanner scanner = new SubdomainScanner();
                String result = scanner.scanSubdomains(message);
                gui.appendLog("Kết quả quét: \n" + result + "\n");
                ctx.writeAndFlush(result + "\n");
            } else {
                gui.appendLog("Xác thực thất bại\n");
                ctx.writeAndFlush("VERIFICATION_FAILED\n");
            }
        } catch (Exception e) {
            gui.appendLog("Lỗi: " + e.getMessage() + "\n");
            ctx.writeAndFlush("VERIFICATION_FAILED\n");
            e.printStackTrace();
        }
    }
}
