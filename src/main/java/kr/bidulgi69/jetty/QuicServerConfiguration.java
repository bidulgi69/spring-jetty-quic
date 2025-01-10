package kr.bidulgi69.jetty;

import org.eclipse.jetty.http3.server.RawHTTP3ServerConnectionFactory;
import org.eclipse.jetty.quic.server.QuicServerConnectionFactory;
import org.eclipse.jetty.quic.server.QuicServerConnector;
import org.eclipse.jetty.quic.server.ServerQuicConfiguration;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

@Configuration
public class QuicServerConfiguration implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

    private final DefaultSslBundleRegistry defaultSslBundleRegistry;

    @Value("${server.port}")
    private int port;

    @Autowired
    public QuicServerConfiguration(DefaultSslBundleRegistry defaultSslBundleRegistry) {
        this.defaultSslBundleRegistry = defaultSslBundleRegistry;
    }

    @Override
    public void customize(JettyServletWebServerFactory factory) {
        JettyServerCustomizer customizer = server -> {
            KeyStore keyStore = defaultSslBundleRegistry.getBundle("server").getStores().getKeyStore();
            SslContextFactory.Server sslContextFactory = getSslContextFactory(keyStore);

            Path pemWorkDir = Paths.get(System.getProperty("java.io.tmpdir"));
            QuicServerConnector connector = getServerConnector(server, sslContextFactory, pemWorkDir);
            connector.getQuicConfiguration().setPemWorkDirectory(pemWorkDir);
            server.addConnector(connector);
        };

        factory.addServerCustomizers(customizer);
    }

    private SslContextFactory.Server getSslContextFactory(KeyStore keyStore) {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePassword("");
        // ALPN settings
        // Ensure TLS 1.3 is enabled
        sslContextFactory.setIncludeProtocols("TLSv1.3");
        // Include required cipher suites for TLS 1.3
        sslContextFactory.setIncludeCipherSuites(
            "TLS_AES_128_GCM_SHA256",
            "TLS_AES_256_GCM_SHA384"
        );
        return sslContextFactory;
    }

    private QuicServerConnector getServerConnector(Server server,
                                                   SslContextFactory.Server sslContextFactory,
                                                   Path pemWorkDir
    ) {
        // fallback configuration for http/1.x
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer());
        httpConfig.setIdleTimeout(9_000_000);

        // configuration for http/3
        ServerQuicConfiguration serverQuicConfiguration = new ServerQuicConfiguration(
            sslContextFactory,
            pemWorkDir
        );

        QuicServerConnector connector = new QuicServerConnector(
            server,
            serverQuicConfiguration,
            new QuicServerConnectionFactory(serverQuicConfiguration),
            new RawHTTP3ServerConnectionFactory(serverQuicConfiguration, httpConfig, new RouteAllocator())
        );

        connector.setPort(port);
        return connector;
    }
}
