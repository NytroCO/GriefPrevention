/*
 * This file is part of GriefPrevention, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ryan Hamshire
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

import me.ryanhamshire.griefprevention.GPPlayerData;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import org.spongepowered.api.entity.living.player.Player;

//applies a visualization for a player by sending him block change packets
class VisualizationReversionTask implements Runnable {

    private final Player player;
    private final GPPlayerData playerData;

    public VisualizationReversionTask(Player player, GPPlayerData playerData) {
        this.playerData = playerData;
        this.player = player;
    }

    @Override
    public void run() {
        // don't do anything if the player's current visualization is different
        // from the one scheduled to revert
        if (this.playerData.visualBlocks == null) {
            return;
        }

        // check for any active WECUI visuals
        if (GriefPreventionPlugin.instance.worldEditProvider != null) {
            GriefPreventionPlugin.instance.worldEditProvider.revertVisuals(this.player, this.playerData, this.playerData.visualClaimId);
        }
        this.playerData.revertActiveVisual(this.player);
    }
}
