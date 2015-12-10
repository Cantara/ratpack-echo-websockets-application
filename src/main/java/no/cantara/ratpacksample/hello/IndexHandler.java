package no.cantara.ratpacksample.hello;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ratpack.handling.Context;
import ratpack.handling.Handler;

@Singleton
public class IndexHandler implements Handler {

    private final String helloWorldGreeting;

    @Inject
    public IndexHandler(@Named("hello.greeting") String helloWorldGreeting) {
        this.helloWorldGreeting = helloWorldGreeting;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.render(helloWorldGreeting);
    }
}
