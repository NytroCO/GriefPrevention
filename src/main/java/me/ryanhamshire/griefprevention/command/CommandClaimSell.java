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
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.claim.GPClaim;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class CommandClaimSell implements CommandExecutor {

    private static Consumer<CommandSource> createSaleConfirmationConsumer(CommandSource src, Claim claim, double price) {
        return confirm -> {
            claim.getEconomyData().setSalePrice(price);
            claim.getEconomyData().setForSale(true);
            claim.getData().save();
            final Text text = Text.of(TextColors.GREEN, "You have successfully put your claim up for sale for the amount of ", TextColors.GOLD, price);
            GriefPreventionPlugin.sendMessage(src, text);
        };
    }

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

        final GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        final GPClaim claim = GriefPreventionPlugin.instance.dataStore.getClaimAt(player.getLocation());

        if (claim.isAdminClaim() || claim.isWilderness()) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "This claim is not for sale."));
            return CommandResult.success();
        }

        if (!playerData.canIgnoreClaim(claim) && !player.getUniqueId().equals(claim.getOwnerUniqueId())) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "You don't have permission to sell this claim."));
            return CommandResult.success();
        }

        Double salePrice = ctx.<Double>getOne("price").orElse(null);
        String arg = ctx.<String>getOne("cancel").orElse(null);
        if (salePrice == null) {
            if (!claim.getEconomyData().isForSale()) {
                GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "This claim is not for sale."));
                return CommandResult.success();
            }
            if (arg.equalsIgnoreCase("cancel")) {
                claim.getEconomyData().setForSale(false);
                claim.getEconomyData().setSalePrice(-1);
                GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.GREEN, "You have cancelled your claim sale."));
                return CommandResult.success();
            }
            return CommandResult.success();
        }

        if (salePrice < 0) {
            final Text message = Text.of(TextColors.RED, "The sale price of, ", TextColors.GOLD, salePrice + " must be greater than or equal to 0.");
            GriefPreventionPlugin.sendMessage(player, message);
            return CommandResult.success();
        }

        final Text message = Text.of(TextColors.GREEN, "Are you sure you want to sell your claim for " + salePrice + " ? If your claim is sold, all items and blocks will be transferred to the buyer. Click confirm if this is OK" +
                ".");
        GriefPreventionPlugin.sendMessage(player, message);

        final Text saleConfirmationText = Text.builder().append(Text.of(
                TextColors.WHITE, "\n", TextColors.WHITE, "[", TextColors.GREEN, "Confirm", TextColors.WHITE, "]\n"))
                .onClick(TextActions.executeCallback(createSaleConfirmationConsumer(src, claim, salePrice))).build();
        GriefPreventionPlugin.sendMessage(player, saleConfirmationText);

        return CommandResult.success();
    }
}
