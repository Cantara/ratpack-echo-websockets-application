package no.cantara.ratpacksample.echo;

import ratpack.exec.Promise;
import ratpack.health.HealthCheck;
import ratpack.registry.Registry;

public class EchoHealthCheck implements HealthCheck {
    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public Promise<Result> check(Registry registry) throws Exception {
        return Promise.value(HealthCheck.Result.healthy());
    }
}
