package no.cantara.ratpacksample;

import com.codahale.metrics.MetricRegistry;
import no.cantara.ratpack.config.RatpackConfigs;
import no.cantara.ratpack.config.RatpackGuiceConfigModule;
import no.cantara.ratpack.freemarker.FreemarkerModel;
import no.cantara.ratpack.freemarker.FreemarkerModule;
import no.cantara.ratpacksample.echo.EchoModule;
import no.cantara.ratpacksample.echo.EchoWebsocketRequestUpgradeHandler;
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
import ratpack.guice.Guice;
import ratpack.handling.Handler;
import ratpack.health.HealthCheckHandler;
import ratpack.server.RatpackServer;

import java.nio.file.Paths;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(RatpackConfigs.configuration("Ratpack Echo-WebSockets", 12121, "appconfig/echo.properties", "echo.properties"))
                .registry(Guice.registry(b -> b
                        .module(new RatpackGuiceConfigModule(b.getServerConfig()))
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
