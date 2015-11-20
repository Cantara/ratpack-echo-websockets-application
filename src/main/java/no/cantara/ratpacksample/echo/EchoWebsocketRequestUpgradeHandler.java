package no.cantara.ratpacksample.echo;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.websocket.WebSockets;

public class EchoWebsocketRequestUpgradeHandler implements Handler {
    @Override
    public void handle(Context ctx) throws Exception {
        WebSockets.websocket(ctx, new EchoVowelReplaceWebsocketHandler());
    }
}
