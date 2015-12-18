package no.cantara.ratpacksample;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Injector;
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
import ratpack.func.Action;
import ratpack.func.Function;
import ratpack.guice.Guice;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.health.HealthCheckHandler;
import ratpack.registry.Registry;
import ratpack.server.RatpackServer;

import java.nio.file.Paths;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final String APPLICATION_NAME = "Ratpack Echo-WebSockets";
    public static final int HTTP_PORT = 12121;
    public static final String CONTEXT_ROOT = "/ratpacksample";
    public static final String DEFAULT_CONFIGURATION_RESOURCE_PATH = "appconfig/echo.properties";
    public static final String OVERRIDE_CONFIGURATION_FILE_PATH = "echo.properties";

    public static void main(String... args) throws Exception {
        new Main().start();
    }

    public void start() throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(RatpackConfigs.configuration(APPLICATION_NAME, HTTP_PORT, DEFAULT_CONFIGURATION_RESOURCE_PATH, OVERRIDE_CONFIGURATION_FILE_PATH))
                .registry(registry())
                .handlers(rootChain(CONTEXT_ROOT))
        );
    }

    private Function<Registry, Registry> registry() {
        return Guice.registry(bindings -> bindings
                .module(new RatpackGuiceConfigModule(bindings.getServerConfig()))
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
        );
    }

    private Action<Chain> rootChain(String contextRoot) {
        if (contextRoot.startsWith("/")) {
            contextRoot = contextRoot.substring(1);
        }
        final String noSlashContextRoot = contextRoot;
        return rootChain -> rootChain
                .all(requestCountMetricsHandler())
                .prefix(noSlashContextRoot, applicationChain())
                .get(chain -> chain.redirect(301, "/" + noSlashContextRoot))
                .get("favicon.ico", sendFileHandler("assets/ico/3dlb-3d-Lock.ico"))
                .all(chain -> chain.notFound());
    }

    private Action<Chain> applicationChain() {
        return appChain -> appChain
                .all(requestCountMetricsHandler())
                .prefix("admin", chain -> {
                    chain.get("metrics", new MetricsWebsocketBroadcastHandler());
                    chain.get("health/:name?", new HealthCheckHandler());
                })
                .prefix("hello", chain -> chain
                        .get(freemarkerHandler("hello/index.ftl"))
                        .get(":name", chain.getRegistry().get(Injector.class).getInstance(PathSpecificHandler.class))
                        .get(IndexHandler.class)
                )
                .prefix("echo", chain -> chain
                        .get(freemarkerHandler("echo/echo.ftl"))
                        .get("echows", chain.getRegistry().get(Injector.class).getInstance(EchoWebsocketRequestUpgradeHandler.class))
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
                });
    }

    private static Handler sendFileHandler(String path) {
        return ctx -> ctx.getResponse().sendFile(Paths.get(Main.class.getClassLoader().getResource(path).toURI()));
    }

    private Handler freemarkerHandler(String template) {
        FreemarkerModel model = new FreemarkerModel(template);
        model.put("contextRoot", CONTEXT_ROOT);
        return ctx -> ctx.render(model);
    }

    private static Handler requestCountMetricsHandler() {
        return ctx -> {
            MetricRegistry metricRegistry = ctx.get(MetricRegistry.class);
            metricRegistry.counter("request-count").inc();
            ctx.next();
        };
    }
}
