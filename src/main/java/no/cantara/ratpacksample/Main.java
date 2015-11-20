package no.cantara.ratpacksample;

import com.google.common.collect.ImmutableMap;
import no.cantara.ratpacksample.hello.HelloModule;
import no.cantara.ratpacksample.hello.IndexHandler;
import no.cantara.ratpacksample.hello.PathSpecificHandler;
import ratpack.dropwizard.metrics.DropwizardMetricsConfig;
import ratpack.dropwizard.metrics.DropwizardMetricsModule;
import ratpack.dropwizard.metrics.MetricsWebsocketBroadcastHandler;
import ratpack.error.ClientErrorHandler;
import ratpack.error.internal.DefaultDevelopmentErrorHandler;
import ratpack.guice.Guice;
import ratpack.health.HealthCheckHandler;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;

public class Main {
    public static void main(String... args) throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(serverConfigBuilder -> serverConfigBuilder
                        .props(ImmutableMap.of("app.name", "Ratpack Hello-World"))
                        .sysProps()
                        .baseDir(BaseDir.find())
                )
                .registry(Guice.registry(b -> b
                        .moduleConfig(DropwizardMetricsModule.class, new DropwizardMetricsConfig()
                                .jmx(jmxConfig -> jmxConfig.enable(true))
                                .jvmMetrics(true)
                                .webSocket(websocketConfig -> {
                                })
                        )
                        .module(HelloModule.class)
                        .bind(ClientErrorHandler.class, DefaultDevelopmentErrorHandler.class)
                ))
                .handlers(rootChain -> rootChain
                        .prefix("admin", chain -> {
                            chain.get("metrics", new MetricsWebsocketBroadcastHandler());
                            chain.get("health/:name?", new HealthCheckHandler());
                        })
                        .prefix("hello", chain -> chain
                                .get(":name", PathSpecificHandler.class)
                                .get(IndexHandler.class)
                        )
                        .all(chain -> chain.notFound())
                )
        );
    }
}
