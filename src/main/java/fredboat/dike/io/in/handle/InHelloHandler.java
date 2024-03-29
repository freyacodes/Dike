/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;
import fredboat.dike.io.in.Heartbeater;
import fredboat.dike.session.cache.Cache;
import fredboat.dike.util.OpCodes;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InHelloHandler extends IncomingHandler {

    private static final Logger log = LoggerFactory.getLogger(InHelloHandler.class);

    private final String op2;
    private final Cache cache;

    public InHelloHandler(DiscordGateway discordGateway, String op2) {
        super(discordGateway);
        this.op2 = op2;
        this.cache = discordGateway.getSession().getCache();
    }

    @Override
    public void handle(String message) {
        // Start heartbeating
        int interval = new JSONObject(message).getJSONObject("d").getInt("heartbeat_interval");
        Heartbeater heartbeater = discordGateway.getHeartbeater();
        heartbeater.setInterval(interval);
        heartbeater.setEnabled(true);

        switch (discordGateway.getState()) {
            case WAITING_FOR_HELLO_TO_IDENTIFY:
                synchronized (cache) {
                    cache.invalidate(); // Throw away old entities
                }
                discordGateway.sendAsync(op2, true);
                discordGateway.setState(DiscordGateway.State.IDENTIFYING);
                break;
            case WAITING_FOR_HELLO_TO_RESUME:
                discordGateway.setState(DiscordGateway.State.RESUMING);

                JSONObject op6 = new JSONObject();
                op6.put("op", OpCodes.OP_6_RESUME);

                JSONObject d = new JSONObject();
                d.put("seq", ((InDispatchHandler) discordGateway.getHandler(OpCodes.OP_0_DISPATCH)).getSequence());
                d.put("token", discordGateway.getSession().getIdentifier().getToken());
                d.put("session_id", ((InDispatchHandler) discordGateway.getHandler(OpCodes.OP_0_DISPATCH)).getSessionId());
                op6.put("d", d);

                discordGateway.sendAsync(op6.toString(), true);
                break;
            default:
                throw new IllegalStateException("Not expecting OP 10 when in state: " + discordGateway.getState());
        }
    }
}
