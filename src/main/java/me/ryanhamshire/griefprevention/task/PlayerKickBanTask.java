/*
 * This file is part of GriefPrevention, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ryan Hamshire
 * Copyright (c) bloodmc
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.ryanhamshire.griefprevention.task;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

//kicks or bans a player
//need a task for this because async threads (like the chat event handlers) can't kick or ban.
//but they CAN schedule a task to run in the main thread to do that job
public class PlayerKickBanTask implements Runnable {

    private final BanService banService = Sponge.getServiceManager().getRegistration(BanService.class).get().getProvider();

    // player to kick or ban
    private final Player player;

    // message to send player.
    private final Text reason;

    // source of ban
    private final Text source;

    // whether to ban
    private final boolean ban;

    public PlayerKickBanTask(Player player, Text reason, Text source, boolean ban) {
        this.player = player;
        this.reason = reason;
        this.source = source;
        this.ban = ban;
    }

    @Override
    public void run() {
        if (this.ban) {
            final Ban banProfile = Ban.builder()
                    .type(BanTypes.PROFILE)
                    .source(this.source)
                    .profile(player.getProfile())
                    .reason(this.reason)
                    .build();
            this.banService.addBan(banProfile);
            this.player.kick(this.reason);
        } else {
            this.player.kick(this.reason);
        }
    }
}
