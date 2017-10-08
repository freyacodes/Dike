/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.DiscordGateway;

import java.io.IOException;

public class InNoOpHandler extends IncomingHandler {
    public InNoOpHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    @Override
    public void handle(String message) throws IOException {
        // Ignore
    }
}
