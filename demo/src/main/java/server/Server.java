package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.io.FileInputStream;
import java.util.Properties;

public class Server {
    private final int port;
    private final ServerGUI gui;

    public Server(int port, ServerGUI gui) {
        this.port = port;
        this.gui = gui;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ServerHandler(gui));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            gui.appendLog("Server khởi động trên cổng " + port + "\n");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/java/server/server.properties")) {
            prop.load(fis);
        }
        int port = Integer.parseInt(prop.getProperty("server.port", "8080"));

        ServerGUI gui = new ServerGUI();
        Server server = new Server(port, gui);
        gui.setServer(server);
        gui.setVisible(true);
        server.start();
    }
}
