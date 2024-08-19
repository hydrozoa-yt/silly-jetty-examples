package yt.hydrozoa;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;

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
        Server server = new Server();

        HttpConfiguration http = new HttpConfiguration();
        HttpConnectionFactory http11 = new HttpConnectionFactory(http);

        ServerConnector connector = new ServerConnector(server, http11);
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