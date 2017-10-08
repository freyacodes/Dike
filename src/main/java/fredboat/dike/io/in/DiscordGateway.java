/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import fredboat.dike.cache.Session;
import fredboat.dike.io.in.handle.InForwardingHandler;
import fredboat.dike.io.in.handle.InNoOpHandler;
import fredboat.dike.io.in.handle.IncomingHandler;
import fredboat.dike.util.JsonHandler;
import fredboat.dike.util.OpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiscordGateway extends WebSocketAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordGateway.class);

    private final ArrayList<IncomingHandler> handlers = new ArrayList<>();
    private final JsonHandler jsonHandler = new JsonHandler();
    private final Session session;
    private final WebSocket socket;

    public DiscordGateway(Session session, URI uri) throws IOException, WebSocketException {
        this.session = session;

        handlers.add(OpCodes.OP_0_DISPATCH, new InForwardingHandler(this));
        handlers.add(OpCodes.OP_1_HEARTBEAT, new InNoOpHandler(this)); // We may want to implement this
        handlers.add(OpCodes.OP_2_IDENTIFY, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_3_PRESENCE, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_4_VOICE_STATE, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_5_VOICE_PING, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_6_RESUME, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_7_RECONNECT, new InNoOpHandler(this)); //TODO
        handlers.add(OpCodes.OP_8_REQUEST_MEMBERS, new InNoOpHandler(this));
        handlers.add(OpCodes.OP_9_INVALIDATE_SESSION, new InNoOpHandler(this)); //TODO
        handlers.add(OpCodes.OP_10_HELLO, new InForwardingHandler(this)); //TODO
        handlers.add(OpCodes.OP_11_HEARTBEAT_ACK, new InForwardingHandler(this)); // We may want to implement this
        handlers.add(OpCodes.OP_12_GUILD_SYNC, new InNoOpHandler(this));

        socket = new WebSocketFactory()
                .createSocket(uri)
                .addListener(this)
                .addHeader("Accept-Encoding", "gzip");

        socket.connect();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        log.info(text);

        int op = jsonHandler.getOp(text);

        if (op == -1) throw new RuntimeException("Unable to parse op: " + text);

        IncomingHandler incoming = handlers.get(op);
        if (incoming != null) {
            try {
                incoming.handle(text);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Unhandled opcode: " + op + " Forwarding the message");
            forward(text);
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        log.info("Connected to " + websocket.getURI());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        String str = "Got disconnected from websocket by "
                + (closedByServer ? "server" : "client")
                + "!";

        if (serverCloseFrame != null) {
            str += "\n\tRemote code: " + serverCloseFrame.getCloseCode();
            str += "\n\tRemote reason: " + serverCloseFrame.getCloseReason();
        }
        if (clientCloseFrame != null) {
            str += "\n\tClient code: " + clientCloseFrame.getCloseCode();
            str += "\n\tClient reason: " + clientCloseFrame.getCloseReason();
        }

        log.info(str);
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        log.error("Error in websocket", cause);
    }

    public void forward(String message) {
        session.sendLocal(message);
    }

    public WebSocket getSocket() {
        return socket;
    }
}
