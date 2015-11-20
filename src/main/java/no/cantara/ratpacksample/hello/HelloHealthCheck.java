package no.cantara.ratpacksample.hello;

import ratpack.exec.Promise;
import ratpack.health.HealthCheck;
import ratpack.registry.Registry;

public class HelloHealthCheck implements HealthCheck {
    @Override
    public String getName() {
        return "hello";
    }

    @Override
    public Promise<Result> check(Registry registry) throws Exception {
        return Promise.value(HealthCheck.Result.healthy());
    }
}
