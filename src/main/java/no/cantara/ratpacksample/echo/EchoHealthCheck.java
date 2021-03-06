package no.cantara.ratpacksample.echo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ratpack.exec.Promise;
import ratpack.health.HealthCheck;
import ratpack.registry.Registry;

@Singleton
public class EchoHealthCheck implements HealthCheck {

    private final String nameOfThisHealthcheck;

    @Inject
    public EchoHealthCheck(@Named("healthcheck.echo.name") String nameOfThisHealthcheck) {
        this.nameOfThisHealthcheck = nameOfThisHealthcheck;
    }

    @Override
    public String getName() {
        return nameOfThisHealthcheck;
    }

    @Override
    public Promise<Result> check(Registry registry) throws Exception {
        return Promise.value(HealthCheck.Result.healthy());
    }
}
