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
package me.ryanhamshire.griefprevention.command;

import me.ryanhamshire.griefprevention.GPPlayerData;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.api.claim.ClaimFlag;
import me.ryanhamshire.griefprevention.claim.GPClaim;
import me.ryanhamshire.griefprevention.permission.GPPermissionHandler;
import me.ryanhamshire.griefprevention.permission.GPPermissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tristate;

import java.util.ArrayList;
import java.util.List;

public class CommandClaimFlag extends ClaimFlagBase implements CommandExecutor {

    public CommandClaimFlag() {
        super(ClaimSubjectType.GLOBAL);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) {
        String flag = ctx.<String>getOne("flag").orElse(null);
        String source = ctx.<String>getOne("source").orElse(null);
        String target = null;
        // Workaround command API issue not handling onlyOne arguments with sequences properly
        List<String> targetValues = new ArrayList<>(ctx.getAll("target"));
        if (targetValues.size() > 0) {
            if (targetValues.size() > 1) {
                target = targetValues.get(1);
            } else {
                target = targetValues.get(0);
            }
        }

        if (source != null && source.equalsIgnoreCase("any")) {
            source = null;
        }
        if (source != null && source.equalsIgnoreCase("hand")) {
            if (!(src instanceof Player)) {
                src.sendMessage(Text.of(TextColors.RED, "The variable hand can only be used with a Player!."));
                return CommandResult.success();
            }
            final Player player = (Player) src;
            ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
            if (stack != null) {
                source = GPPermissionHandler.getPermissionIdentifier(stack);
                source = filterMeta(stack, source);
            }
        }
        if (target != null && target.equalsIgnoreCase("hand")) {
            if (!(src instanceof Player)) {
                src.sendMessage(Text.of(TextColors.RED, "The variable hand can only be used with a Player!."));
                return CommandResult.success();
            }
            final Player player = (Player) src;
            ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
            if (stack != null) {
                target = GPPermissionHandler.getPermissionIdentifier(stack);
                target = filterMeta(stack, target);
            }
        }

        Tristate value = ctx.<Tristate>getOne("value").orElse(null);
        String context = ctx.<String>getOne("context").orElse(null);
        String reason = ctx.<String>getOne("reason").orElse(null);
        Text reasonText = null;
        Player player;
        if (reason != null) {
            reasonText = TextSerializers.FORMATTING_CODE.deserialize(reason);
        }

        try {
            player = GriefPreventionPlugin.checkPlayer(src);
        } catch (CommandException e) {
            src.sendMessage(e.getText());
            return CommandResult.success();
        }

        this.subject = GriefPreventionPlugin.GLOBAL_SUBJECT;
        this.friendlySubjectName = "ALL";
        GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        GPClaim claim = GriefPreventionPlugin.instance.dataStore.getClaimAtPlayer(playerData, player.getLocation());
        if (claim != null) {
            if (flag == null && value == null && src.hasPermission(GPPermissions.COMMAND_LIST_CLAIM_FLAGS)) {
                showFlagPermissions(src, claim, FlagType.ALL, source);
                return CommandResult.success();
            } else if (flag != null && value != null) {
                if (!ClaimFlag.contains(flag)) {
                    src.sendMessage(Text.of(TextColors.RED, "Flag not found."));
                    return CommandResult.success();
                }
                Context claimContext = claim.getContext();
                if (context != null) {
                    claimContext = CommandHelper.validateCustomContext(src, claim, context);
                    if (claimContext == null) {
                        final Text message = Text.of(TextColors.RED, "Invalid context '" + context + "' entered for base flag " + flag + ".");
                        GriefPreventionPlugin.sendMessage(src, message);
                        return CommandResult.success();
                    }
                }

                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    Sponge.getCauseStackManager().pushCause(src);
                    claim.setPermission(GriefPreventionPlugin.GLOBAL_SUBJECT, "ALL", ClaimFlag.getEnum(flag), source, target, value, claimContext,
                            reasonText);
                }
                return CommandResult.success();
            }

            GriefPreventionPlugin.sendMessage(src, CommandMessageFormatting.error(Text.of("Usage: /cf [<flag> <target> <value> [subject|context]]")));
        } else {
            GriefPreventionPlugin.sendMessage(src, Text.of(TextColors.RED, "There's no claim here."));
        }
        return CommandResult.success();
    }
}
