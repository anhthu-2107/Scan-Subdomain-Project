package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Client {
    private final String host;
    private final int port;
    private final GUI gui;
    private final CryptoUtilitis cryptoUtilitis;
    private Channel channel;

    public Client(String host, int port, GUI gui) throws Exception {
        this.host = host;
        this.port = port;
        this.gui = gui;
        this.cryptoUtilitis = new CryptoUtilitis();
        this.gui.setClient(this); // cho phép GUI gọi lại các hàm từ client
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    gui.appendResponse(msg); // show response in GUI
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    });

            channel = bootstrap.connect(host, port).sync().channel();
            gui.setChannel(channel); // giao tiếp ngược GUI -> Client
        } catch (Exception e) {
            group.shutdownGracefully();
            throw e;
        }
    }

    // Dùng để tạo chữ ký số
    public String signMessage(String message) throws Exception {
        return cryptoUtilitis.signMessage(message);
    }

    // Dùng để mã hóa public key bằng AES
    public String encryptPublicKey() throws Exception {
        return cryptoUtilitis.encryptPublicKey();
    }

    // Getter cho GUI gọi
    public CryptoUtilitis getCryptoUtilitis() {
        return cryptoUtilitis;
    }

    // Gửi thông điệp có chữ ký
    public void sendSignedMessage(String message) throws Exception {
        String signature = signMessage(message);
        String payload = "MESSAGE:" + message + "|SIGNATURE:" + signature;
        channel.writeAndFlush(payload + "\n");
    }

    // Gửi public key đã mã hóa
    public void sendEncryptedPublicKey() throws Exception {
        String encryptedKey = encryptPublicKey();
        channel.writeAndFlush("ENCRYPTED_KEY:" + encryptedKey + "\n");
    }

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/java/client/client.properties")) {
            prop.load(fis);
        }
        String host = prop.getProperty("server.host", "localhost");
        int port = Integer.parseInt(prop.getProperty("server.port", "8080"));

        GUI gui = new GUI();
        Client client = new Client(host, port, gui);
        client.start();
        gui.setVisible(true);
    }
}