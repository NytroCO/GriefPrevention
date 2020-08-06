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
package me.ryanhamshire.griefprevention.command;

import me.ryanhamshire.griefprevention.GPPlayerData;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.configuration.GriefPreventionConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;

public class CommandClaimBuyBlocks implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) {
        Player player;
        try {
            player = GriefPreventionPlugin.checkPlayer(src);
        } catch (CommandException e) {
            src.sendMessage(e.getText());
            return CommandResult.success();
        }

        // if economy is disabled, don't do anything
        if (!GriefPreventionPlugin.instance.economyService.isPresent()) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "Economy plugin not installed!."));
            return CommandResult.success();
        }

        Account playerAccount = GriefPreventionPlugin.instance.economyService.get().getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (playerAccount == null) {
            final Text message = Text.of(TextColors.RED, "No economy account found for user " + player.getName() + ".");
            GriefPreventionPlugin.sendMessage(player, message);
            return CommandResult.success();
        }

        GriefPreventionConfig<?> activeConfig = GriefPreventionPlugin.getActiveConfig(player.getWorld().getProperties());
        if (activeConfig.getConfig().economy.economyClaimBlockCost == 0 && activeConfig.getConfig().economy.economyClaimBlockSell == 0) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "Sorry, buying and selling claim blocks is disabled."));
            return CommandResult.success();
        }

        // if purchase disabled, send error message
        if (activeConfig.getConfig().economy.economyClaimBlockCost == 0) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "Claim blocks may only be sold, not purchased."));
            return CommandResult.success();
        }

        Optional<Integer> blockCountOpt = ctx.getOne("numberOfBlocks");
        double balance = playerAccount.getBalance(GriefPreventionPlugin.instance.economyService.get().getDefaultCurrency()).doubleValue();
        // if no parameter, just tell player cost per block and balance
        if (!blockCountOpt.isPresent()) {
            final Text message = Text.of("Each claim block costs " + activeConfig.getConfig().economy.economyClaimBlockCost + " Your balance is " + balance + ".");
            GriefPreventionPlugin.sendMessage(player, message);
            return CommandResult.success();
        } else {
            GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());

            // try to parse number of blocks
            int blockCount = blockCountOpt.get();

            if (blockCount <= 0) {
                GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "Block count must be greater than 0."));
                return CommandResult.success();
            }

            final double totalCost = blockCount * activeConfig.getConfig().economy.economyClaimBlockCost;
            final int newClaimBlockTotal = playerData.getAccruedClaimBlocks() + blockCount;
            if (newClaimBlockTotal > playerData.getMaxAccruedClaimBlocks()) {
                // player has exceeded limit
                final Text message = Text.of(TextColors.RED, "The new claim block total of ", TextColors.GOLD, newClaimBlockTotal + " will exceed your claim block limit of ",
                        TextColors.GREEN, playerData.getMaxAccruedClaimBlocks() + ". The " +
                                "transaction has been cancelled.");
                GriefPreventionPlugin.sendMessage(player, message);
                return CommandResult.success();
            }

            // attempt to withdraw cost
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().addContext(GriefPreventionPlugin.PLUGIN_CONTEXT, GriefPreventionPlugin.instance);
                TransactionResult transactionResult = playerAccount.withdraw
                        (GriefPreventionPlugin.instance.economyService.get().getDefaultCurrency(), BigDecimal.valueOf(totalCost),
                                Sponge.getCauseStackManager().getCurrentCause());

                if (transactionResult.getResult() != ResultType.SUCCESS) {
                    final Text message = Text.of(TextColors.RED, "Could not withdraw funds. Reason: " + transactionResult.getResult().name() + ".");
                    GriefPreventionPlugin.sendMessage(player, message);
                    return CommandResult.success();
                }
            }

            // add blocks
            playerData.addAccruedClaimBlocks(blockCount);
            playerData.getStorageData().save();

            final Text message = Text.of(TextColors.GREEN, "Withdrew " + totalCost + " from your account.  You now have " + playerData.getRemainingClaimBlocks() + " available claim blocks.");
            // inform player
            GriefPreventionPlugin.sendMessage(player, message);
        }
        return CommandResult.success();
    }
}
