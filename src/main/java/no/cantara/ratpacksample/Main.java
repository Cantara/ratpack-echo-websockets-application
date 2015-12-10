package no.cantara.ratpacksample;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import no.cantara.ratpacksample.config.ConfigModule;
import no.cantara.ratpacksample.echo.EchoModule;
import no.cantara.ratpacksample.echo.EchoWebsocketRequestUpgradeHandler;
import no.cantara.ratpacksample.freemarkersupport.FreemarkerModel;
import no.cantara.ratpacksample.freemarkersupport.FreemarkerModule;
import no.cantara.ratpacksample.hello.HelloModule;
import no.cantara.ratpacksample.hello.IndexHandler;
import no.cantara.ratpacksample.hello.PathSpecificHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.dropwizard.metrics.DropwizardMetricsConfig;
import ratpack.dropwizard.metrics.DropwizardMetricsModule;
import ratpack.dropwizard.metrics.MetricsWebsocketBroadcastHandler;
import ratpack.error.ClientErrorHandler;
import ratpack.error.internal.DefaultDevelopmentErrorHandler;
import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.handling.Handler;
import ratpack.health.HealthCheckHandler;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfigBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(serverConfig("echo.properties"))
                .registry(Guice.registry(b -> b
                        .module(new ConfigModule(b.getServerConfig()))
                        .module(new EchoModule())
                        .moduleConfig(DropwizardMetricsModule.class, new DropwizardMetricsConfig()
                                .jmx(jmxConfig -> jmxConfig.enable(true))
                                .jvmMetrics(true)
                                .webSocket(websocketConfig -> {
                                })
                        )
                        .moduleConfig(FreemarkerModule.class, new FreemarkerModule.Config().templateLoadingPath("/freemarker"))
                        .module(HelloModule.class)
                        .bind(ClientErrorHandler.class, DefaultDevelopmentErrorHandler.class)
                ))
                .handlers(rootChain -> rootChain
                        .all(requestCountMetricsHandler())
                        .prefix("admin", chain -> {
                            chain.get("metrics", new MetricsWebsocketBroadcastHandler());
                            chain.get("health/:name?", new HealthCheckHandler());
                        })
                        .prefix("hello", chain -> chain
                                .get(freemarkerHandler("hello/index.ftl"))
                                .get(":name", PathSpecificHandler.class)
                                .get(IndexHandler.class)
                        )
                        .prefix("echo", chain -> chain
                                .get(freemarkerHandler("echo/echo.ftl"))
                                .get("echows", new EchoWebsocketRequestUpgradeHandler())
                        )
                        // root path serves index file
                        .get(freemarkerHandler("index.ftl"))

                        .files(f -> f.path("js").dir("assets/js"))
                        .files(f -> f.path("css").dir("assets/css"))

                        .get("favicon.ico", sendFileHandler("assets/ico/3dlb-3d-Lock.ico"))

                        // redirect index* to root path
                        .prefix("index", chain -> chain.redirect(301, "/"))
                        .path(":name?", ctx -> {
                            String name = ctx.getPathTokens().get("name");
                            if (name.startsWith("index")) {
                                ctx.redirect("/");
                            } else {
                                ctx.next();
                            }
                        })

                        .all(chain -> chain.notFound())
                )
        );
    }

    private static Action<ServerConfigBuilder> serverConfig(String propertiesFilename) {
        return serverConfigBuilder -> {
            String defaultPropertiesResource = "appconfig/" + propertiesFilename;
            log.info("loading default configuration from resource on classpath: " + defaultPropertiesResource);
            serverConfigBuilder
                    .props(ImmutableMap.of("app.name", "Ratpack Echo-WebSockets"))
                    .port(12345)
                    .props(Resources.getResource(defaultPropertiesResource)); // default config from classpath
            Path overridePath = Paths.get(propertiesFilename);
            File overrideFile = overridePath.toFile();
            if (overrideFile.isFile() && overrideFile.canRead()) {
                log.info("loading override configuration from file: " + overrideFile.getCanonicalPath().toString());
                serverConfigBuilder.props(readProperties(overrideFile));
            }
            serverConfigBuilder
                    .env()
                    .sysProps()
                    .baseDir(BaseDir.find());
        };
    }

    private static Properties readProperties(File overrideFile) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(overrideFile), Charset.forName("UTF-8")))) {
            properties.load(br);
        }
        return properties;
    }

    private static Handler sendFileHandler(String path) {
        return ctx -> ctx.getResponse().sendFile(Paths.get(Main.class.getClassLoader().getResource(path).toURI()));
    }

    private static Handler freemarkerHandler(String template) {
        return freemarkerHandler(new FreemarkerModel(template));
    }

    private static Handler freemarkerHandler(FreemarkerModel object) {
        return ctx -> ctx.render(object);
    }

    private static Handler requestCountMetricsHandler() {
        return ctx -> {
            MetricRegistry metricRegistry = ctx.get(MetricRegistry.class);
            metricRegistry.counter("request-count").inc();
            ctx.next();
        };
    }
}
