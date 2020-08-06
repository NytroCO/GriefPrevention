/*
 * This file is part of GriefPrevention, licensed under the MIT License (MIT).
 *
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
package me.ryanhamshire.griefprevention.listener;

import me.ryanhamshire.griefprevention.GPTimings;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.claim.GPClaimManager;
import me.ryanhamshire.griefprevention.task.TaxApplyTask;
import me.ryanhamshire.griefprevention.util.TaskUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.ConstructWorldPropertiesEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.common.SpongeImpl;

import java.util.concurrent.TimeUnit;

public class WorldEventHandler {

    @Listener(order = Order.EARLY)
    public void onConstructWorldProperties(ConstructWorldPropertiesEvent event) {
        if (!SpongeImpl.getServer().isServerRunning() || !GriefPreventionPlugin.instance.claimsEnabledForWorld(event.getWorldProperties())) {
            return;
        }

        GriefPreventionPlugin.instance.dataStore.registerWorld(event.getWorldProperties());
    }

    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onWorldLoad(LoadWorldEvent event) {
        if (!SpongeImpl.getServer().isServerRunning() || !GriefPreventionPlugin.instance.claimsEnabledForWorld(event.getTargetWorld().getProperties())) {
            return;
        }

        GPTimings.WORLD_LOAD_EVENT.startTimingIfSync();
        GriefPreventionPlugin.instance.dataStore.loadWorldData(event.getTargetWorld());
        net.minecraft.world.World world = (net.minecraft.world.World) event.getTargetWorld();
        world.addEventListener(new EntityRemovalListener());
        GPTimings.WORLD_LOAD_EVENT.stopTimingIfSync();
        if (!GriefPreventionPlugin.getActiveConfig(event.getTargetWorld().getProperties()).getConfig().claim.bankTaxSystem) {
            return;
        }
        if (GriefPreventionPlugin.instance.economyService.isPresent()) {
            // run tax task
            TaxApplyTask taxTask = new TaxApplyTask(event.getTargetWorld().getProperties());
            int taxHour = GriefPreventionPlugin.getActiveConfig(event.getTargetWorld().getProperties()).getConfig().claim.taxApplyHour;
            long delay = TaskUtils.computeDelay(taxHour, 0, 0);
            Sponge.getScheduler().createTaskBuilder().delay(delay, TimeUnit.SECONDS).interval(1, TimeUnit.DAYS).execute(taxTask).submit(GriefPreventionPlugin.instance);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onWorldUnload(UnloadWorldEvent event) {
        if (!SpongeImpl.getServer().isServerRunning() || !GriefPreventionPlugin.instance.claimsEnabledForWorld(event.getTargetWorld().getProperties())) {
            return;
        }

        GriefPreventionPlugin.instance.dataStore.removeClaimWorldManager(event.getTargetWorld().getProperties());
    }

    @Listener
    public void onWorldSave(SaveWorldEvent.Post event) {
        if (!GriefPreventionPlugin.instance.claimsEnabledForWorld(event.getTargetWorld().getProperties())) {
            return;
        }

        GPTimings.WORLD_SAVE_EVENT.startTimingIfSync();
        GPClaimManager claimWorldManager = GriefPreventionPlugin.instance.dataStore.getClaimWorldManager(event.getTargetWorld().getProperties());
        if (claimWorldManager == null) {
            GPTimings.WORLD_SAVE_EVENT.stopTimingIfSync();
            return;
        }

        claimWorldManager.save();
        GPTimings.WORLD_SAVE_EVENT.stopTimingIfSync();
    }

    @Listener
    public void onChunkLoad(LoadChunkEvent event) {

    }

    @Listener
    public void onChunkUnload(UnloadChunkEvent event) {

    }
}
