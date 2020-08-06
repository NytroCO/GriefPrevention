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

import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.event.BorderClaimEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;

import javax.annotation.Nullable;
import java.util.Optional;

public class GPBorderClaimEvent extends GPClaimEvent implements BorderClaimEvent {

    private final Entity targetEntity;
    private final Claim exitClaim;
    private Text enterMessage;
    private Text exitMessage;
    private ChatType enterChatType = ChatTypes.CHAT;
    private ChatType exitChatType = ChatTypes.CHAT;

    public GPBorderClaimEvent(Entity entity, Claim exit, Claim enter) {
        super(enter);
        this.targetEntity = entity;
        this.exitClaim = exit;
    }

    @Override
    public Claim getExitClaim() {
        return this.exitClaim;
    }

    @Override
    public Entity getTargetEntity() {
        return this.targetEntity;
    }

    @Override
    public Optional<Text> getEnterMessage() {
        if (this.enterMessage == null) {
            return this.getClaim().getData().getGreeting();
        }

        return Optional.of(this.enterMessage);
    }

    @Override
    public Optional<Text> getExitMessage() {
        if (this.exitMessage == null) {
            return this.exitClaim.getData().getFarewell();
        }

        return Optional.of(this.exitMessage);
    }

    @Override
    public void setEnterMessage(@Nullable Text message, ChatType chatType) {
        this.enterMessage = message;
        this.enterChatType = chatType;
    }

    @Override
    public void setExitMessage(@Nullable Text message, ChatType chatType) {
        this.exitMessage = message;
        this.exitChatType = chatType;
    }

    @Override
    public ChatType getExitMessageChatType() {
        return exitChatType;
    }

    @Override
    public ChatType getEnterMessageChatType() {
        return enterChatType;
    }


}