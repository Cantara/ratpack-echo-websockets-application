package no.cantara.ratpacksample.echo;

import com.google.inject.AbstractModule;

public class EchoModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EchoHealthCheck.class);
    }
}
