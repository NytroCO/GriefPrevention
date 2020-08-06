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

import com.google.common.reflect.TypeToken;
import me.ryanhamshire.griefprevention.FlatFileDataStore;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.IpSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class ClaimTemplateStorage {

    public Path filePath;
    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode root = SimpleCommentedConfigurationNode.root(ConfigurationOptions.defaults()
            .setHeader(GriefPreventionPlugin.CONFIG_HEADER));
    private ObjectMapper<ClaimTemplateConfig>.BoundInstance configMapper;
    private ClaimTemplateConfig configBase;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ClaimTemplateStorage(Path path) {
        this.filePath = path;
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            this.configMapper = ObjectMapper.forClass(ClaimTemplateConfig.class).bindToNew();

            reload();
            save();
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize claim template data", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ClaimTemplateStorage(String templateName, Optional<String> description, IClaimData claimData, UUID creator) {
        this.filePath = FlatFileDataStore.rootWorldSavePath.resolve(FlatFileDataStore.claimTemplatePath.resolve(UUID.randomUUID().toString()));
        try {
            if (Files.notExists(this.filePath.getParent())) {
                Files.createDirectories(this.filePath.getParent());
            }
            if (Files.notExists(this.filePath)) {
                Files.createFile(this.filePath);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(this.filePath).build();
            this.configMapper = ObjectMapper.forClass(ClaimTemplateConfig.class).bindToNew();

            reload();
            this.configBase.templateName = templateName;
            if (description.isPresent()) {
                this.configBase.templateDescription = description.get();
            }
            this.configBase.ownerUniqueId = creator;
            this.configBase.accessors = new ArrayList<UUID>(claimData.getAccessors());
            this.configBase.builders = new ArrayList<UUID>(claimData.getBuilders());
            this.configBase.containers = new ArrayList<UUID>(claimData.getContainers());
            this.configBase.coowners = new ArrayList<UUID>(claimData.getManagers());
            //this.configBase.flags = new HashMap<String, Tristate>(claimData.getFlags());
            save();
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize claim template data", e);
        }
    }

    public ClaimTemplateConfig getConfig() {
        return this.configBase;
    }

    public void save() {
        try {
            this.configMapper.serialize(this.root.getNode(GriefPreventionPlugin.MOD_ID));
            this.loader.save(this.root);
        } catch (IOException | ObjectMappingException e) {
            SpongeImpl.getLogger().error("Failed to save configuration", e);
        }
    }

    public void reload() {
        try {
            this.root = this.loader.load(ConfigurationOptions.defaults()
                    .setSerializers(
                            TypeSerializers.getDefaultSerializers().newChild().registerType(TypeToken.of(IpSet.class), new IpSet.IpSetSerializer()))
                    .setHeader(GriefPreventionPlugin.CONFIG_HEADER));
            this.configBase = this.configMapper.populate(this.root.getNode(GriefPreventionPlugin.MOD_ID));
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to load configuration", e);
        }
    }

    public CommentedConfigurationNode getRootNode() {
        return this.root.getNode(GriefPreventionPlugin.MOD_ID);
    }
}
