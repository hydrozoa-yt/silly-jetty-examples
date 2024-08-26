package yt.hydrozoa;

import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.security.Security;

public class Main implements Runnable {

    private String page = """
            <html>
                <body>
                    <h1>Hello world</h1>
                </body>
            </html>
            """;

    @Override
    public void run() {
        Security.insertProviderAt(new OpenSSLProvider(), 1);
        Server server = new Server();

        HttpConfiguration httpConfig = new HttpConfiguration();
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfig);

        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(http11.getProtocol());

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("keystore.bin");
        sslContextFactory.setKeyStorePassword("pass123");

        SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        ServerConnector connector = new ServerConnector(server, tls, alpn, h2, http11);
        connector.setPort(8080);
        server.addConnector(connector);

        server.setHandler(new HelloWorldHandler());

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class HelloWorldHandler extends Handler.Abstract {

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            sendMessage(page, response, callback);
            return true;
        }

        private void sendMessage(String message, Response resp, Callback callback) {
            Content.Sink.write(resp, true, message, callback);
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}