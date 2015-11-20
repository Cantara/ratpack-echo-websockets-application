package no.cantara.ratpacksample.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.websocket.WebSocket;
import ratpack.websocket.WebSocketClose;
import ratpack.websocket.WebSocketHandler;
import ratpack.websocket.WebSocketMessage;

import java.util.concurrent.atomic.AtomicLong;

public class EchoVowelReplaceWebsocketHandler implements WebSocketHandler<String> {
    private static final Logger log = LoggerFactory.getLogger(EchoVowelReplaceWebsocketHandler.class);
    private static final AtomicLong openCounter = new AtomicLong();

    private WebSocket webSocket;

    @Override
    public String onOpen(WebSocket webSocket) throws Exception {
        this.webSocket = webSocket;
        long myNumber = openCounter.getAndIncrement();
        log.trace("Opened echo websocket {}", myNumber);
        return "ws_echo_open-" + myNumber;
    }

    @Override
    public void onClose(WebSocketClose<String> close) throws Exception {
        log.trace("Closed by {}, openResult: {}", close.isFromClient() ? "client" : "server", close.getOpenResult());
    }

    @Override
    public void onMessage(WebSocketMessage<String> frame) throws Exception {
        String text = frame.getText();
        log.trace("Messsage: {}", text);
        frame.getConnection().send(text.replaceAll("e", "a").replaceAll("o", "i").replaceAll("u", "y"));
    }
}
