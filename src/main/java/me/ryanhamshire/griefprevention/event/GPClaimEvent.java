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
package me.ryanhamshire.griefprevention.event;

import com.google.common.collect.ImmutableList;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.event.ClaimEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class GPClaimEvent extends AbstractEvent implements ClaimEvent {

    private final Cause cause;
    private final List<Claim> claims;
    private Text message;
    private boolean isCancelled = false;

    public GPClaimEvent(Claim claim) {
        this.claims = ImmutableList.of(claim);
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    public GPClaimEvent(List<Claim> claims) {
        this.claims = ImmutableList.copyOf(claims);
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public List<Claim> getClaims() {
        return this.claims;
    }

    @Override
    public Optional<Text> getMessage() {
        return Optional.ofNullable(this.message);
    }

    @Override
    public void setMessage(Text message) {
        this.message = message;
    }

}
