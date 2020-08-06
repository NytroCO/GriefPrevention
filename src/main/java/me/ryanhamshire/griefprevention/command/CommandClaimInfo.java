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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.ryanhamshire.griefprevention.GPPlayerData;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.claim.ClaimResult;
import me.ryanhamshire.griefprevention.api.claim.ClaimType;
import me.ryanhamshire.griefprevention.api.claim.TrustType;
import me.ryanhamshire.griefprevention.claim.GPClaim;
import me.ryanhamshire.griefprevention.claim.GPClaimManager;
import me.ryanhamshire.griefprevention.permission.GPPermissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;

public class CommandClaimInfo implements CommandExecutor {

    private static final Text NONE = Text.of(TextColors.GRAY, "none");
    private static final String ADMIN_SETTINGS = "Admin Settings";
    private static final String CLAIM_EXPIRATION = "ClaimExpiration";
    private static final String DENY_MESSAGES = "DenyMessages";
    private static final String FLAG_OVERRIDES = "FlagOverrides";
    private static final String INHERIT_PARENT = "InheritParent";
    private static final String PVP_OVERRIDE = "PvPOverride";
    private static final String RESIZABLE = "Resizable";
    private static final String REQUIRES_CLAIM_BLOCKS = "RequiresClaimBlocks";
    private static final String SIZE_RESTRICTIONS = "SizeRestrictions";
    private static final String FOR_SALE = "ForSale";
    private boolean useTownInfo = false;

    public CommandClaimInfo() {

    }

    public CommandClaimInfo(boolean useTownInfo) {
        this.useTownInfo = useTownInfo;
    }

    public static Consumer<CommandSource> createSettingsConsumer(CommandSource src, Claim claim, List<Text> textList, ClaimType type) {
        return settings -> {
            String name = type == ClaimType.TOWN ? "Town Settings" : "Admin Settings";
            PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
            PaginationList.Builder paginationBuilder = paginationService.builder()
                    .title(Text.of(TextColors.AQUA, name)).padding(Text.of(TextStyles.STRIKETHROUGH, "-")).contents(textList);
            paginationBuilder.sendTo(src);
        };
    }

    private static List<Text> generateAdminSettings(CommandSource src, GPClaim claim) {
        List<Text> textList = new ArrayList<>();
        Text returnToClaimInfo = Text.builder().append(Text.of(
                TextColors.WHITE, "\n[", TextColors.AQUA, "Return to standard settings", TextColors.WHITE, "]\n"))
                .onClick(TextActions.executeCallback(CommandHelper.createCommandConsumer(src, "claiminfo", claim.getUniqueId().toString()))).build();
        Text claimDenyMessages = Text.of(TextColors.YELLOW, DENY_MESSAGES, TextColors.WHITE, " : ", getClickableInfoText(src, claim, DENY_MESSAGES, claim.getInternalClaimData().allowDenyMessages() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text claimResizable = Text.of(TextColors.YELLOW, RESIZABLE, TextColors.WHITE, " : ", getClickableInfoText(src, claim, RESIZABLE, claim.getInternalClaimData().isResizable() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text claimRequiresClaimBlocks = Text.of(TextColors.YELLOW, REQUIRES_CLAIM_BLOCKS, TextColors.WHITE, " : ", getClickableInfoText(src, claim, REQUIRES_CLAIM_BLOCKS, claim.getInternalClaimData().requiresClaimBlocks() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text claimSizeRestrictions = Text.of(TextColors.YELLOW, SIZE_RESTRICTIONS, TextColors.WHITE, " : ", getClickableInfoText(src, claim, SIZE_RESTRICTIONS, claim.getInternalClaimData().hasSizeRestrictions() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text claimExpiration = Text.of(TextColors.YELLOW, CLAIM_EXPIRATION, TextColors.WHITE, " : ", getClickableInfoText(src, claim, CLAIM_EXPIRATION, claim.getInternalClaimData().allowExpiration() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text claimFlagOverrides = Text.of(TextColors.YELLOW, FLAG_OVERRIDES, TextColors.WHITE, " : ", getClickableInfoText(src, claim, FLAG_OVERRIDES, claim.getInternalClaimData().allowFlagOverrides() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text pvp = Text.of(TextColors.YELLOW, "PvP", TextColors.WHITE, " : ", getClickableInfoText(src, claim, PVP_OVERRIDE, claim.getInternalClaimData().getPvpOverride() == Tristate.TRUE ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, claim.getInternalClaimData().getPvpOverride().name())), TextColors.RESET);
        textList.add(returnToClaimInfo);
        textList.add(claimDenyMessages);
        if (!claim.isAdminClaim() && !claim.isWilderness()) {
            textList.add(claimRequiresClaimBlocks);
            textList.add(claimExpiration);
            textList.add(claimResizable);
            textList.add(claimSizeRestrictions);
        }
        textList.add(claimFlagOverrides);
        textList.add(pvp);
        int fillSize = 20 - (textList.size() + 4);
        for (int i = 0; i < fillSize; i++) {
            textList.add(Text.of(" "));
        }
        return textList;
    }

    private static void executeAdminSettings(CommandSource src, GPClaim claim) {
        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(Text.of(TextColors.AQUA, "Admin Settings")).padding(Text.of(TextStyles.STRIKETHROUGH, "-")).contents(generateAdminSettings(src, claim));
        paginationBuilder.sendTo(src);
    }

    public static Text getClickableInfoText(CommandSource src, Claim claim, String title, Text infoText) {
        Text onClickText = Text.of("Click here to toggle value.");
        boolean hasPermission = true;
        if (src instanceof Player) {
            Text denyReason = ((GPClaim) claim).allowEdit((Player) src);
            if (denyReason != null) {
                onClickText = denyReason;
                hasPermission = false;
            }
        }

        Text.Builder textBuilder = Text.builder()
                .append(infoText)
                .onHover(TextActions.showText(Text.of(onClickText)));
        if (hasPermission) {
            textBuilder.onClick(TextActions.executeCallback(createClaimInfoConsumer(src, claim, title)));
        }
        return textBuilder.build();
    }

    private static Consumer<CommandSource> createClaimInfoConsumer(CommandSource src, Claim claim, String title) {
        GPClaim gpClaim = (GPClaim) claim;
        return info -> {
            switch (title) {
                case INHERIT_PARENT:
                    if (!claim.getParent().isPresent() || !src.hasPermission(GPPermissions.COMMAND_CLAIM_INHERIT)) {
                        return;
                    }

                    gpClaim.getInternalClaimData().setInheritParent(!gpClaim.getInternalClaimData().doesInheritParent());
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    claim.getData().save();
                    CommandHelper.executeCommand(src, "claiminfo", gpClaim.getUniqueId().toString());
                    return;
                case CLAIM_EXPIRATION:
                    gpClaim.getInternalClaimData().setExpiration(!gpClaim.getInternalClaimData().allowExpiration());
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case DENY_MESSAGES:
                    gpClaim.getInternalClaimData().setDenyMessages(!gpClaim.getInternalClaimData().allowDenyMessages());
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case FLAG_OVERRIDES:
                    gpClaim.getInternalClaimData().setFlagOverrides(!gpClaim.getInternalClaimData().allowFlagOverrides());
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case PVP_OVERRIDE:
                    Tristate value = gpClaim.getInternalClaimData().getPvpOverride();
                    if (value == Tristate.UNDEFINED) {
                        gpClaim.getInternalClaimData().setPvpOverride(Tristate.TRUE);
                    } else if (value == Tristate.TRUE) {
                        gpClaim.getInternalClaimData().setPvpOverride(Tristate.FALSE);
                    } else {
                        gpClaim.getInternalClaimData().setPvpOverride(Tristate.UNDEFINED);
                    }
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case RESIZABLE:
                    boolean resizable = gpClaim.getInternalClaimData().isResizable();
                    gpClaim.getInternalClaimData().setResizable(!resizable);
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case REQUIRES_CLAIM_BLOCKS:
                    boolean requiresClaimBlocks = gpClaim.getInternalClaimData().requiresClaimBlocks();
                    gpClaim.getInternalClaimData().setRequiresClaimBlocks(!requiresClaimBlocks);
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case SIZE_RESTRICTIONS:
                    boolean sizeRestrictions = gpClaim.getInternalClaimData().hasSizeRestrictions();
                    gpClaim.getInternalClaimData().setSizeRestrictions(!sizeRestrictions);
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    break;
                case FOR_SALE:
                    boolean forSale = gpClaim.getEconomyData().isForSale();
                    gpClaim.getEconomyData().setForSale(!forSale);
                    gpClaim.getInternalClaimData().setRequiresSave(true);
                    gpClaim.getClaimStorage().save();
                    CommandHelper.executeCommand(src, "claiminfo", gpClaim.getUniqueId().toString());
                    return;
                default:
            }
            executeAdminSettings(src, gpClaim);
        };
    }

    private static Consumer<CommandSource> createClaimTypeConsumer(CommandSource src, Claim gpClaim, ClaimType clicked, boolean isAdmin) {
        GPClaim claim = (GPClaim) gpClaim;
        return type -> {
            if (!(src instanceof Player)) {
                // ignore
                return;
            }

            final Player player = (Player) src;
            if (!isAdmin && ((GPClaim) gpClaim).allowEdit(player) != null) {
                src.sendMessage(Text.of(TextColors.RED, "This isn't your claim."));
                return;
            }
            final ClaimResult result = claim.changeType(clicked, Optional.of(player.getUniqueId()), src);
            if (result.successful()) {
                CommandHelper.executeCommand(src, "claiminfo", gpClaim.getUniqueId().toString());
                //src.sendMessage(Text.of(TextColors.GREEN, "Successfully changed claim type to ", claim.getFriendlyNameType(), TextColors.GREEN, "." ));
            } else {
                src.sendMessage(result.getMessage().get());
            }
        };
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) {
        String claimIdentifier = ctx.<String>getOne("id").orElse(null);
        Player player = null;
        if (src instanceof Player) {
            player = (Player) src;
            if (!GriefPreventionPlugin.instance.claimsEnabledForWorld(player.getWorld().getProperties())) {
                GriefPreventionPlugin.sendMessage(src, Text.of(TextColors.RED, "Land claims are disabled in this world."));
                return CommandResult.success();
            }
        }

        if (player == null && claimIdentifier == null) {
            src.sendMessage(Text.of(TextColors.RED, "No valid player or claim UUID found."));
            return CommandResult.success();
        }

        boolean isAdmin = src.hasPermission(GPPermissions.COMMAND_ADMIN_CLAIMS);
        final GPPlayerData playerData = player != null ? GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId()) : null;
        Claim claim = null;
        if (claimIdentifier == null) {
            if (player != null) {
                claim = GriefPreventionPlugin.instance.dataStore.getClaimAt(player.getLocation());
            } else {
                src.sendMessage(Text.of(TextColors.RED, "Claim UUID is required if executing from non-player source."));
                return CommandResult.success();
            }
        } else {
            for (World world : Sponge.getServer().getWorlds()) {
                if (!GriefPreventionPlugin.instance.claimsEnabledForWorld(world.getProperties())) {
                    continue;
                }

                final GPClaimManager claimManager = GriefPreventionPlugin.instance.dataStore.getClaimWorldManager(world.getProperties());
                UUID uuid = null;
                try {
                    uuid = UUID.fromString(claimIdentifier);
                    claim = claimManager.getClaimByUUID(uuid).orElse(null);
                    if (claim != null) {
                        break;
                    }
                } catch (IllegalArgumentException e) {

                }
                if (uuid == null) {
                    final List<Claim> claimList = claimManager.getClaimsByName(claimIdentifier);
                    if (!claimList.isEmpty()) {
                        claim = claimList.get(0);
                    }
                }
            }
        }

        if (claim == null) {
            GriefPreventionPlugin.sendMessage(src, Text.of(TextColors.RED, "There's no claim here."));
            return CommandResult.success();
        }

        if (this.useTownInfo) {
            if (!claim.isInTown()) {
                GriefPreventionPlugin.sendMessage(player, Text.of(TextColors.RED, "You are not in a town."));
                return CommandResult.success();
            }
            claim = claim.getTown().get();
        }

        final GPClaim gpClaim = (GPClaim) claim;
        UUID ownerUniqueId = claim.getOwnerUniqueId();

        if (!isAdmin) {
            isAdmin = playerData.canIgnoreClaim(gpClaim);
        }
        // if not owner of claim, validate perms
        if (!isAdmin && !player.getUniqueId().equals(claim.getOwnerUniqueId())) {
            if (!gpClaim.getInternalClaimData().getContainers().contains(player.getUniqueId())
                    && !gpClaim.getInternalClaimData().getBuilders().contains(player.getUniqueId())
                    && !gpClaim.getInternalClaimData().getManagers().contains(player.getUniqueId())
                    && !player.hasPermission(GPPermissions.COMMAND_CLAIM_INFO_OTHERS)) {
                player.sendMessage(Text.of(TextColors.RED, "This isn't your claim."));
                return CommandResult.success();
            }
        }

        final Text allowEdit = gpClaim.allowEdit(player);
        User owner = null;
        if (!claim.isWilderness()) {
            owner = GriefPreventionPlugin.getOrCreateUser(ownerUniqueId);
        }

        List<Text> textList = new ArrayList<>();
        Text name = claim.getName().orElse(null);
        Text greeting = claim.getData().getGreeting().orElse(null);
        Text farewell = claim.getData().getFarewell().orElse(null);
        String accessors = "";
        String builders = "";
        String containers = "";
        String managers = "";
        String accessorGroups = "";
        String builderGroups = "";
        String containerGroups = "";
        String managerGroups = "";

        double claimY = gpClaim.getOwnerPlayerData() == null ? 65.0D : (gpClaim.getOwnerPlayerData().getMinClaimLevel() > 65.0D ? gpClaim.getOwnerPlayerData().getMinClaimLevel() : 65);
        if (gpClaim.isCuboid()) {
            claimY = gpClaim.lesserBoundaryCorner.getY();
        }
        Location<World> southWest = gpClaim.lesserBoundaryCorner.setPosition(new Vector3d(gpClaim.lesserBoundaryCorner.getX(), claimY, gpClaim.greaterBoundaryCorner.getZ()));
        Location<World> northWest = gpClaim.lesserBoundaryCorner.setPosition(new Vector3d(gpClaim.lesserBoundaryCorner.getX(), claimY, gpClaim.lesserBoundaryCorner.getZ()));
        Location<World> southEast = gpClaim.lesserBoundaryCorner.setPosition(new Vector3d(gpClaim.greaterBoundaryCorner.getX(), claimY, gpClaim.greaterBoundaryCorner.getZ()));
        Location<World> northEast = gpClaim.lesserBoundaryCorner.setPosition(new Vector3d(gpClaim.greaterBoundaryCorner.getX(), claimY, gpClaim.lesserBoundaryCorner.getZ()));
        // String southWestCorner =
        Date created = null;
        Date lastActive = null;
        try {
            Instant instant = claim.getData().getDateCreated();
            created = Date.from(instant);
        } catch (DateTimeParseException ex) {
            // ignore
        }

        try {
            Instant instant = claim.getData().getDateLastActive();
            lastActive = Date.from(instant);
        } catch (DateTimeParseException ex) {
            // ignore
        }

        final int sizeX = Math.abs(claim.getGreaterBoundaryCorner().getBlockX() - claim.getLesserBoundaryCorner().getBlockX()) + 1;
        final int sizeY = Math.abs(claim.getGreaterBoundaryCorner().getBlockY() - claim.getLesserBoundaryCorner().getBlockY()) + 1;
        final int sizeZ = Math.abs(claim.getGreaterBoundaryCorner().getBlockZ() - claim.getLesserBoundaryCorner().getBlockZ()) + 1;
        Text claimSize = Text.of();
        if (claim.isCuboid()) {
            claimSize = Text.of("  ", TextColors.YELLOW, "Area: ", TextColors.GRAY, sizeX + "x" + sizeY + "x" + sizeZ);
        } else {
            claimSize = Text.of("  ", TextColors.YELLOW, "Area: ", TextColors.GRAY, sizeX + "x" + sizeZ);
        }
        final Text claimCost = Text.of("  ", TextColors.YELLOW, "Blocks: ", TextColors.GRAY, claim.getClaimBlocks());
        if (claim.isWilderness() && name == null) {
            name = Text.of(TextColors.GREEN, "Wilderness");
        }
        Text claimName = Text.of(
                TextColors.YELLOW, "Name", TextColors.WHITE, " : ", TextColors.GRAY, name == null ? NONE : name);
        if (!claim.isWilderness() && !claim.isAdminClaim()) {
            claimName = Text.join(claimName, claimSize, claimCost);
        }
        // users
        final List<UUID> accessorList = gpClaim.getUserTrustList(TrustType.ACCESSOR, true);
        final List<UUID> builderList = gpClaim.getUserTrustList(TrustType.BUILDER, true);
        final List<UUID> containerList = gpClaim.getUserTrustList(TrustType.CONTAINER, true);
        final List<UUID> managerList = gpClaim.getUserTrustList(TrustType.MANAGER, true);
        for (UUID uuid : accessorList) {
            User user = GriefPreventionPlugin.getOrCreateUser(uuid);
            accessors += user.getName() + " ";
        }
        for (UUID uuid : builderList) {
            User user = GriefPreventionPlugin.getOrCreateUser(uuid);
            builders += user.getName() + " ";
        }
        for (UUID uuid : containerList) {
            User user = GriefPreventionPlugin.getOrCreateUser(uuid);
            containers += user.getName() + " ";
        }
        for (UUID uuid : managerList) {
            User user = GriefPreventionPlugin.getOrCreateUser(uuid);
            managers += user.getName() + " ";
        }

        // groups
        for (String group : gpClaim.getInternalClaimData().getAccessorGroups()) {
            accessorGroups += group + " ";
        }
        for (String group : gpClaim.getInternalClaimData().getBuilderGroups()) {
            builderGroups += group + " ";
        }
        for (String group : gpClaim.getInternalClaimData().getContainerGroups()) {
            containerGroups += group + " ";
        }
        for (String group : gpClaim.getInternalClaimData().getManagerGroups()) {
            managerGroups += group + " ";
        }

        /*if (gpClaim.isInTown()) {
            Text returnToClaimInfo = Text.builder().append(Text.of(
                    TextColors.WHITE, "\n[", TextColors.AQUA, "Return to standard settings", TextColors.WHITE, "]\n"))
                .onClick(TextActions.executeCallback(CommandHelper.createCommandConsumer(src, "claiminfo", ""))).build();
            Text townName = Text.of(TextColors.YELLOW, "Name", TextColors.WHITE, " : ", TextColors.RESET,
                    gpClaim.getTownClaim().getTownData().getName().orElse(NONE));
            Text townTag = Text.of(TextColors.YELLOW, "Tag", TextColors.WHITE, " : ", TextColors.RESET,
                    gpClaim.getTownClaim().getTownData().getTownTag().orElse(NONE));
            townTextList.add(returnToClaimInfo);
            townTextList.add(townName);
            townTextList.add(townTag);
            Text townSettings = Text.builder()
                    .append(Text.of(TextStyles.ITALIC, TextColors.GREEN, TOWN_SETTINGS))
                    .onClick(TextActions.executeCallback(createSettingsConsumer(src, claim, townTextList, ClaimType.TOWN)))
                    .onHover(TextActions.showText(Text.of("Click here to view town settings")))
                    .build();
            textList.add(townSettings);
        }*/

        if (isAdmin) {
            Text adminSettings = Text.builder()
                    .append(Text.of(TextStyles.ITALIC, TextColors.RED, ADMIN_SETTINGS))
                    .onClick(TextActions.executeCallback(createSettingsConsumer(src, claim, generateAdminSettings(src, gpClaim), ClaimType.ADMIN)))
                    .onHover(TextActions.showText(Text.of("Click here to view admin settings")))
                    .build();
            textList.add(adminSettings);
        }

        Text bankInfo = null;
        Text forSaleText = null;
        if (GriefPreventionPlugin.instance.economyService.isPresent()) {
            if (GriefPreventionPlugin.getActiveConfig(gpClaim.getWorld().getProperties()).getConfig().claim.bankTaxSystem) {
                bankInfo = Text.builder().append(Text.of(TextColors.GOLD, TextStyles.ITALIC, "Bank Info"))
                        .onHover(TextActions.showText(Text.of("Click to check bank information")))
                        .onClick(TextActions.executeCallback(consumer -> {
                            CommandHelper.displayClaimBankInfo(src, gpClaim, gpClaim.isTown(), true);
                        }))
                        .build();
            }
            forSaleText = Text.builder()
                    .append(Text.of(TextColors.YELLOW, "ForSale", TextColors.WHITE, " : ", getClickableInfoText(src, claim, FOR_SALE, claim.getEconomyData().isForSale() ? Text.of(TextColors.GREEN, "YES") : Text.of(TextColors.GRAY, "NO")))).build();
            if (claim.getEconomyData().isForSale()) {
                forSaleText = Text.join(forSaleText, Text.of("  ", TextColors.YELLOW, "Price", TextColors.WHITE, " : ", TextColors.GOLD, claim.getEconomyData().getSalePrice()));
            }
        }

        Text claimId = Text.join(Text.of(TextColors.YELLOW, "UUID", TextColors.WHITE, " : ",
                Text.builder()
                        .append(Text.of(TextColors.GRAY, claim.getUniqueId().toString()))
                        .onShiftClick(TextActions.insertText(claim.getUniqueId().toString())).build()));
        Text ownerLine = Text.of(TextColors.YELLOW, "Owner", TextColors.WHITE, " : ", TextColors.GOLD, owner != null && !claim.isAdminClaim() ? owner.getName() : "administrator");
        Text adminShowText = Text.of();
        Text basicShowText = Text.of();
        Text subdivisionShowText = Text.of();
        Text townShowText = Text.of();
        Text claimType = Text.of();
        final Text whiteOpenBracket = Text.of(TextColors.WHITE, "[");
        final Text whiteCloseBracket = Text.of(TextColors.WHITE, "]");
        if (allowEdit != null && !isAdmin) {
            adminShowText = allowEdit;
            basicShowText = allowEdit;
            subdivisionShowText = allowEdit;
            townShowText = allowEdit;
            Text adminTypeText = Text.builder()
                    .append(Text.of(claim.getType() == ClaimType.ADMIN ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "ADMIN")))
                    .onHover(TextActions.showText(adminShowText)).build();
            Text basicTypeText = Text.builder()
                    .append(Text.of(claim.getType() == ClaimType.BASIC ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "BASIC")))
                    .onHover(TextActions.showText(basicShowText)).build();
            Text subTypeText = Text.builder()
                    .append(Text.of(claim.getType() == ClaimType.SUBDIVISION ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "SUBDIVISION")))
                    .onHover(TextActions.showText(subdivisionShowText)).build();
            Text townTypeText = Text.builder()
                    .append(Text.of(claim.getType() == ClaimType.TOWN ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "TOWN")))
                    .onHover(TextActions.showText(townShowText)).build();
            claimType = Text.builder()
                    .append(Text.of(TextColors.GREEN, claim.isCuboid() ? "3D " : "2D "), adminTypeText, Text.of(" "), basicTypeText, Text.of(" "), subTypeText, Text.of(" "), townTypeText)
                    .build();
        } else {
            Text adminTypeText = Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket);
            Text basicTypeText = Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket);
            Text subTypeText = Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket);
            Text townTypeText = Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket);
            if (!claim.isAdminClaim()) {
                final Text message = ((GPClaim) claim).validateClaimType(ClaimType.ADMIN, ownerUniqueId, playerData).getMessage().orElse(null);
                adminShowText = message != null ? message : Text.of("Click here to change claim to ", TextColors.RED, "ADMIN ", TextColors.RESET, "type.");

                if (message == null) {
                    adminTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.ADMIN ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "ADMIN")))
                            .onClick(TextActions.executeCallback(createClaimTypeConsumer(src, claim, ClaimType.ADMIN, isAdmin)))
                            .onHover(TextActions.showText(adminShowText)).build();
                } else {
                    adminTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.ADMIN ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "ADMIN")))
                            .onHover(TextActions.showText(adminShowText)).build();
                }
            }
            if (!claim.isBasicClaim()) {
                final Text message = ((GPClaim) claim).validateClaimType(ClaimType.BASIC, ownerUniqueId, playerData).getMessage().orElse(null);
                basicShowText = message != null ? message : Text.of("Click here to change claim to ", TextColors.YELLOW, "BASIC ", TextColors.RESET, "type.");

                if (message == null) {
                    basicTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.BASIC ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "BASIC")))
                            .onClick(TextActions.executeCallback(createClaimTypeConsumer(src, claim, ClaimType.BASIC, isAdmin)))
                            .onHover(TextActions.showText(basicShowText)).build();
                } else {
                    basicTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.BASIC ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "BASIC")))
                            .onHover(TextActions.showText(basicShowText)).build();
                }
            }
            if (!claim.isSubdivision()) {
                final Text message = ((GPClaim) claim).validateClaimType(ClaimType.SUBDIVISION, ownerUniqueId, playerData).getMessage().orElse(null);
                subdivisionShowText = message != null ? message : Text.of("Click here to change claim to ", TextColors.AQUA, "SUBDIVISION ", TextColors.RESET, "type.");

                if (message == null) {
                    subTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.SUBDIVISION ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "SUBDIVISION")))
                            .onClick(TextActions.executeCallback(createClaimTypeConsumer(src, claim, ClaimType.SUBDIVISION, isAdmin)))
                            .onHover(TextActions.showText(subdivisionShowText)).build();
                } else {
                    subTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.SUBDIVISION ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "SUBDIVISION")))
                            .onHover(TextActions.showText(subdivisionShowText)).build();
                }
            }
            if (!claim.isTown()) {
                final Text message = ((GPClaim) claim).validateClaimType(ClaimType.TOWN, ownerUniqueId, playerData).getMessage().orElse(null);
                townShowText = message != null ? message : Text.of("Click here to change claim to ", TextColors.GREEN, "TOWN ", TextColors.RESET, "type.");

                if (message == null) {
                    townTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.TOWN ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "TOWN")))
                            .onClick(TextActions.executeCallback(createClaimTypeConsumer(src, claim, ClaimType.TOWN, isAdmin)))
                            .onHover(TextActions.showText(townShowText)).build();
                } else {
                    townTypeText = Text.builder()
                            .append(Text.of(claim.getType() == ClaimType.TOWN ? Text.of(whiteOpenBracket, gpClaim.getFriendlyNameType(true), whiteCloseBracket) : Text.of(TextColors.GRAY, "TOWN")))
                            .onHover(TextActions.showText(townShowText)).build();
                }
            }

            claimType = Text.builder()
                    .append(Text.of(TextColors.GREEN, claim.isCuboid() ? "3D " : "2D "), adminTypeText, Text.of(" "), basicTypeText, Text.of(" "), subTypeText, Text.of(" "), townTypeText)
                    .build();
        }
        Text claimTypeInfo = Text.of(TextColors.YELLOW, "Type", TextColors.WHITE, " : ", claimType);
        Text claimInherit = Text.of(TextColors.YELLOW, INHERIT_PARENT, TextColors.WHITE, " : ", getClickableInfoText(src, claim, INHERIT_PARENT, claim.getData().doesInheritParent() ? Text.of(TextColors.GREEN, "ON") : Text.of(TextColors.RED, "OFF")), TextColors.RESET);
        Text claimExpired = Text.of(TextColors.YELLOW, "Expired", TextColors.WHITE, " : ", claim.getData().isExpired() ? Text.of(TextColors.RED, "YES") : Text.of(TextColors.GRAY, "NO"));
        Text claimFarewell = Text.of(TextColors.YELLOW, "Farewell", TextColors.WHITE, " : ", TextColors.RESET,
                farewell == null ? NONE : farewell);
        Text claimGreeting = Text.of(TextColors.YELLOW, "Greeting", TextColors.WHITE, " : ", TextColors.RESET,
                greeting == null ? NONE : greeting);
        Text claimSpawn = null;
        if (claim.getData().getSpawnPos().isPresent()) {
            Vector3i spawnPos = claim.getData().getSpawnPos().get();
            Location<World> spawnLoc = new Location<>(claim.getWorld(), spawnPos);
            claimSpawn = Text.builder().append(Text.of(TextColors.GREEN, "Spawn", TextColors.WHITE, " : ", TextColors.GRAY, spawnPos))
                    .onClick(TextActions.executeCallback(CommandHelper.createTeleportConsumer(player, spawnLoc, claim)))
                    .onHover(TextActions.showText(Text.of("Click here to teleport to claim spawn.")))
                    .build();
        }
        Text southWestCorner = Text.builder()
                .append(Text.of(TextColors.LIGHT_PURPLE, "SW", TextColors.WHITE, " : ", TextColors.GRAY, southWest.getBlockPosition(), " "))
                .onClick(TextActions.executeCallback(CommandHelper.createTeleportConsumer(player, southWest, claim)))
                .onHover(TextActions.showText(Text.of("Click here to teleport to SW corner of claim.")))
                .build();
        Text southEastCorner = Text.builder()
                .append(Text.of(TextColors.LIGHT_PURPLE, "SE", TextColors.WHITE, " : ", TextColors.GRAY, southEast.getBlockPosition(), " "))
                .onClick(TextActions.executeCallback(CommandHelper.createTeleportConsumer(player, southEast, claim)))
                .onHover(TextActions.showText(Text.of("Click here to teleport to SE corner of claim.")))
                .build();
        Text southCorners = Text.builder()
                .append(Text.of(TextColors.YELLOW, "SouthCorners", TextColors.WHITE, " : "))
                .append(southWestCorner)
                .append(southEastCorner).build();
        Text northWestCorner = Text.builder()
                .append(Text.of(TextColors.LIGHT_PURPLE, "NW", TextColors.WHITE, " : ", TextColors.GRAY, northWest.getBlockPosition(), " "))
                .onClick(TextActions.executeCallback(CommandHelper.createTeleportConsumer(player, northWest, claim)))
                .onHover(TextActions.showText(Text.of("Click here to teleport to NW corner of claim.")))
                .build();
        Text northEastCorner = Text.builder()
                .append(Text.of(TextColors.LIGHT_PURPLE, "NE", TextColors.WHITE, " : ", TextColors.GRAY, northEast.getBlockPosition(), " "))
                .onClick(TextActions.executeCallback(CommandHelper.createTeleportConsumer(player, northEast, claim)))
                .onHover(TextActions.showText(Text.of("Click here to teleport to NE corner of claim.")))
                .build();
        Text northCorners = Text.builder()
                .append(Text.of(TextColors.YELLOW, "NorthCorners", TextColors.WHITE, " : "))
                .append(northWestCorner)
                .append(northEastCorner).build();
        Text claimAccessors = Text.of(TextColors.YELLOW, "Accessors", TextColors.WHITE, " : ", TextColors.BLUE, accessors.equals("") ? NONE : accessors, " ", TextColors.LIGHT_PURPLE, accessorGroups);
        Text claimBuilders = Text.of(TextColors.YELLOW, "Builders", TextColors.WHITE, " : ", TextColors.YELLOW, builders.equals("") ? NONE : builders, " ", TextColors.LIGHT_PURPLE, builderGroups);
        Text claimContainers = Text.of(TextColors.YELLOW, "Containers", TextColors.WHITE, " : ", TextColors.GREEN, containers.equals("") ? NONE : containers, " ", TextColors.LIGHT_PURPLE, containerGroups);
        Text claimCoowners = Text.of(TextColors.YELLOW, "Managers", TextColors.WHITE, " : ", TextColors.GOLD, managers.equals("") ? NONE : managers, " ", TextColors.LIGHT_PURPLE, managerGroups);
        Text dateCreated = Text.of(TextColors.YELLOW, "Created", TextColors.WHITE, " : ", TextColors.GRAY, created != null ? created : "Unknown");
        Text dateLastActive = Text.of(TextColors.YELLOW, "LastActive", TextColors.WHITE, " : ", TextColors.GRAY, lastActive != null ? lastActive : "Unknown");
        Text worldName = Text.of(TextColors.YELLOW, "World", TextColors.WHITE, " : ", TextColors.GRAY, claim.getWorld().getProperties().getWorldName());

        if (claimSpawn != null) {
            textList.add(claimSpawn);
        }
        if (bankInfo != null) {
            textList.add(bankInfo);
        }
        textList.add(claimName);
        textList.add(ownerLine);
        textList.add(claimTypeInfo);
        if (!claim.isAdminClaim() && !claim.isWilderness()) {
            textList.add(Text.of(claimInherit, "   ", claimExpired));
            if (forSaleText != null) {
                textList.add(forSaleText);
            }
        }
        textList.add(claimAccessors);
        textList.add(claimBuilders);
        textList.add(claimContainers);
        textList.add(claimCoowners);
        textList.add(claimGreeting);
        textList.add(claimFarewell);
        textList.add(worldName);
        textList.add(dateCreated);
        textList.add(dateLastActive);
        textList.add(claimId);
        textList.add(northCorners);
        textList.add(southCorners);
        if (!claim.getParent().isPresent()) {
            textList.remove(claimInherit);
        }
        if (claim.isAdminClaim()) {
            textList.remove(bankInfo);
            textList.remove(dateLastActive);
        }
        if (claim.isWilderness()) {
            textList.remove(bankInfo);
            textList.remove(claimInherit);
            textList.remove(claimTypeInfo);
            textList.remove(dateLastActive);
            textList.remove(northCorners);
            textList.remove(southCorners);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(Text.of(TextColors.AQUA, "Claim Info")).padding(Text.of(TextStyles.STRIKETHROUGH, "-")).contents(textList);
        paginationBuilder.sendTo(src);

        return CommandResult.success();
    }
}
