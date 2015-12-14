package no.cantara.ratpacksample.hello;

import com.google.inject.Singleton;
import ratpack.handling.Context;
import ratpack.handling.Handler;

@Singleton
public class PathSpecificHandler implements Handler {
    @Override
    public void handle(Context ctx) throws Exception {
        ctx.render("Hello " + ctx.getPathTokens().get("name") + "!");
    }
}
