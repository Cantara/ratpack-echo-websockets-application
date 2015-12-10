package no.cantara.ratpacksample.hello;

import com.google.inject.AbstractModule;

public class HelloModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IndexHandler.class);
        bind(PathSpecificHandler.class);
        bind(HelloHealthCheck.class);
    }
}
