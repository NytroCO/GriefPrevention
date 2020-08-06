package me.ryanhamshire.griefprevention.command;

import me.ryanhamshire.griefprevention.GPPlayerData;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.api.claim.ClaimResult;
import me.ryanhamshire.griefprevention.api.claim.ClaimResultType;
import me.ryanhamshire.griefprevention.api.claim.TrustType;
import me.ryanhamshire.griefprevention.api.data.PlayerData;
import me.ryanhamshire.griefprevention.claim.GPClaim;
import me.ryanhamshire.griefprevention.logging.CustomLogEntryTypes;
import me.ryanhamshire.griefprevention.permission.GPPermissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.UUID;

public class CommandClaimTransfer implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) {
        Player player;
        try {
            player = GriefPreventionPlugin.checkPlayer(src);
        } catch (CommandException e1) {
            src.sendMessage(e1.getText());
            return CommandResult.success();
        }

        final User targetPlayer = args.<User>getOne("user").orElse(null);
        if (targetPlayer == null) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "Player is not valid."));
            return CommandResult.success();
        }

        // which claim is the user in?
        final GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        final GPClaim claim = GriefPreventionPlugin.instance.dataStore.getClaimAtPlayer(playerData, player.getLocation());

        if (claim == null || claim.isWilderness()) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "There's no claim here."));
            return CommandResult.empty();
        }

        final UUID ownerId = claim.getOwnerUniqueId();
        final boolean isAdmin = playerData.canIgnoreClaim(claim);
        // check permission
        if (!isAdmin && claim.isAdminClaim() && !player.hasPermission(GPPermissions.COMMAND_ADMIN_CLAIMS)) {
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "You don't have permission to transfer admin claims."));
            return CommandResult.empty();
        } else if (!isAdmin && !player.getUniqueId().equals(ownerId) && claim.isUserTrusted(player, TrustType.MANAGER)) {
            if (claim.parent == null) {
                // Managers can only transfer child claims
                GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "This isn't your claim."));
                return CommandResult.success();
            }
        } else if (!isAdmin && !claim.isAdminClaim() && !player.getUniqueId().equals(ownerId)) {
            // verify ownership
            GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "This isn't your claim."));
            return CommandResult.success();
        }

        // change ownership
        final ClaimResult claimResult = claim.transferOwner(targetPlayer.getUniqueId());
        if (!claimResult.successful()) {
            PlayerData targetPlayerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), targetPlayer.getUniqueId());
            if (claimResult.getResultType() == ClaimResultType.INSUFFICIENT_CLAIM_BLOCKS) {
                src.sendMessage(Text.of(TextColors.RED, "Could not transfer claim to player with UUID " + targetPlayer.getUniqueId() + "."
                        + " Player only has " + targetPlayerData.getRemainingClaimBlocks() + " claim blocks remaining."
                        + " The claim requires a total of " + claim.getClaimBlocks() + " claim blocks to own."));
            } else if (claimResult.getResultType() == ClaimResultType.WRONG_CLAIM_TYPE) {
                src.sendMessage(Text.of(TextColors.RED, "The wilderness claim cannot be transferred."));
            } else if (claimResult.getResultType() == ClaimResultType.CLAIM_EVENT_CANCELLED) {
                src.sendMessage(Text.of(TextColors.RED, "Could not transfer the claim. A plugin has cancelled the TransferClaimEvent."));
            }
            return CommandResult.success();
        }

        // confirm
        GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.GREEN, "Claim transferred."));
        GriefPreventionPlugin.addLogEntry(player.getName() + " transferred a claim at "
                        + GriefPreventionPlugin.getfriendlyLocationString(claim.getLesserBoundaryCorner()) + " to " + targetPlayer.getName() + ".",
                CustomLogEntryTypes.AdminActivity);

        return CommandResult.success();

    }
}
