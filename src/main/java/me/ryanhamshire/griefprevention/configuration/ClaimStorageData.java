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
package me.ryanhamshire.griefprevention.configuration;

import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.api.claim.ClaimType;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ClaimStorageData {

    // MAIN
    public static final String MAIN_WORLD_UUID = "world-uuid";
    public static final String MAIN_OWNER_UUID = "owner-uuid";
    public static final String MAIN_CLAIM_NAME = "claim-name";
    public static final String MAIN_CLAIM_GREETING = "claim-greeting";
    public static final String MAIN_CLAIM_FAREWELL = "claim-farewell";
    public static final String MAIN_CLAIM_SPAWN = "claim-spawn";
    public static final String MAIN_CLAIM_TYPE = "claim-type";
    public static final String MAIN_CLAIM_CUBOID = "cuboid";
    public static final String MAIN_CLAIM_RESIZABLE = "resizable";
    public static final String MAIN_CLAIM_PVP = "pvp";
    public static final String MAIN_CLAIM_DATE_CREATED = "date-created";
    public static final String MAIN_CLAIM_DATE_LAST_ACTIVE = "date-last-active";
    public static final String MAIN_CLAIM_FOR_SALE = "for-sale";
    public static final String MAIN_CLAIM_SALE_PRICE = "sale-price";
    public static final String MAIN_REQUIRES_CLAIM_BLOCKS = "requires-claim-blocks";
    public static final String MAIN_LESSER_BOUNDARY_CORNER = "lesser-boundary-corner";
    public static final String MAIN_GREATER_BOUNDARY_CORNER = "greater-boundary-corner";
    public static final String MAIN_ACCESSORS = "accessors";
    public static final String MAIN_BUILDERS = "builders";
    public static final String MAIN_CONTAINERS = "containers";
    public static final String MAIN_MANAGERS = "managers";
    public static final String MAIN_ACCESSOR_GROUPS = "accessor-groups";
    public static final String MAIN_BUILDER_GROUPS = "builder-groups";
    public static final String MAIN_CONTAINER_GROUPS = "container-groups";
    public static final String MAIN_MANAGER_GROUPS = "manager-groups";
    public static final String MAIN_ALLOW_DENY_MESSAGES = "deny-messages";
    public static final String MAIN_ALLOW_FLAG_OVERRIDES = "flag-overrides";
    public static final String MAIN_ALLOW_CLAIM_EXPIRATION = "claim-expiration";
    public static final String MAIN_TAX_PAST_DUE_DATE = "tax-past-due-date";
    public static final String MAIN_TAX_BALANCE = "tax-balance";
    // SUB
    public static final String MAIN_INHERIT_PARENT = "inherit-parent";
    public Path filePath;
    public Path folderPath;
    protected HoconConfigurationLoader loader;
    protected ObjectMapper<ClaimDataConfig>.BoundInstance configMapper;
    protected ClaimDataConfig configBase;
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode.root(ConfigurationOptions.defaults());

    // Used for new claims after server startup
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ClaimStorageData(Path path, UUID worldUniqueId, UUID ownerUniqueId, ClaimType type, boolean cuboid) {
        this.filePath = path;
        this.folderPath = path.getParent();
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            if (type == ClaimType.TOWN) {
                this.configMapper = (ObjectMapper.BoundInstance) ObjectMapper.forClass(TownDataConfig.class).bindToNew();
            } else {
                this.configMapper = ObjectMapper.forClass(ClaimDataConfig.class).bindToNew();
            }
            this.configMapper.getInstance().setWorldUniqueId(worldUniqueId);
            this.configMapper.getInstance().setOwnerUniqueId(ownerUniqueId);
            this.configMapper.getInstance().setType(type);
            this.configMapper.getInstance().setCuboid(cuboid);
            this.configMapper.getInstance().setClaimStorageData(this);
            load();
            ((EconomyDataConfig) this.configMapper.getInstance().getEconomyData()).activeConfig = GriefPreventionPlugin.getActiveConfig(Sponge.getServer().getWorld(worldUniqueId).get().getProperties());
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize configuration", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ClaimStorageData(Path path, UUID worldUniqueId, ClaimDataConfig claimData) {
        this.filePath = path;
        this.folderPath = path.getParent();
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            this.configMapper = ObjectMapper.forClass(ClaimDataConfig.class).bind(claimData);
            this.configMapper.getInstance().setClaimStorageData(this);
            load();
            ((EconomyDataConfig) this.configMapper.getInstance().getEconomyData()).activeConfig = GriefPreventionPlugin.getActiveConfig(worldUniqueId);
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize configuration", e);
        }
    }

    // Used during server load
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ClaimStorageData(Path path, UUID worldUniqueId) {
        this.filePath = path;
        this.folderPath = path.getParent();
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            if (path.getParent().endsWith("town")) {
                this.configMapper = (ObjectMapper.BoundInstance) ObjectMapper.forClass(TownDataConfig.class).bindToNew();
            } else {
                this.configMapper = ObjectMapper.forClass(ClaimDataConfig.class).bindToNew();
            }
            this.configMapper.getInstance().setClaimStorageData(this);
            load();
            ((EconomyDataConfig) this.configMapper.getInstance().getEconomyData()).activeConfig = GriefPreventionPlugin.getActiveConfig(worldUniqueId);
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize configuration", e);
        }
    }

    public ClaimDataConfig getConfig() {
        return this.configBase;
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root.getNode(GriefPreventionPlugin.MOD_ID));
            this.loader.save(this.root);
            this.configBase.setRequiresSave(false);
        } catch (IOException | ObjectMappingException e) {
            SpongeImpl.getLogger().error("Failed to save configuration", e);
        }
    }

    public void load() {
        try {
            this.root = this.loader.load(ConfigurationOptions.defaults());
            this.configBase = this.configMapper.populate(this.root.getNode(GriefPreventionPlugin.MOD_ID));
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to load configuration", e);
        }
    }
}
