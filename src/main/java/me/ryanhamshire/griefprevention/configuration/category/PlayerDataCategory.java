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
package me.ryanhamshire.griefprevention.configuration.category;

import me.ryanhamshire.griefprevention.api.claim.ClaimBlockSystem;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PlayerDataCategory extends ConfigCategory {

    @Setting(value = "use-global-storage", comment = "Whether player data should be stored globally. False will store all data per world.")
    public boolean useGlobalPlayerDataStorage = true;
    @Setting(value = "claim-block-system", comment = "Determines which claim block system to use for claims. (Default: AREA)\nIf set to VOLUME, claim blocks will use the chunk count system to balance 3d claiming."
            + "\nIf set to AREA, the standard 2d block count system will be used.")
    public ClaimBlockSystem claimBlockSystem = ClaimBlockSystem.AREA;
    @Setting(value = "migrate-volume-rate", comment = "The rate to multiply each accrued claim blocks total by."
            + "\nSet to a value greater than -1 to enable. (Default: 256)."
            + "\nNote: This should only be used when migrating from area (2D system) to volume (3D system)."
            + "\nEach chunk is worth 65,536 blocks in the new system compared to 256 in old."
            + "\nThis requires 'claim-block-system' to be set to VOLUME.")
    public int migrateVolumeRate = -1;
    @Setting(value = "migrate-area-rate", comment = "The rate to divide each accrued claim blocks total by."
            + "\nSet to a value greater than -1 to enable. (Default: 256)."
            + "\nNote: This should only be used when migrating from volume (3D system) to area (2D system)."
            + "\nIn this system, a chunk costs 256 blocks."
            + "\nThis requires 'claim-block-system' to be set to AREA.")
    public int migrateAreaRate = -1;
    @Setting(value = "reset-migrations", comment = "If enabled, resets all playerdata migration flags to allow for another migration."
            + "\nNote: Use this with caution as it can easily mess up claim block data. It is highly recommended to backup before using.")
    public boolean resetMigrations = false;
    @Setting(value = "reset-claim-block-data", comment = "If enabled, resets all playerdata claimblock data with value specified."
            + "\nExample: If set to 100, every player will start with 100 claim blocks."
            + "\nNote: This will all reset bonus claim blocks to 0. It is highly recommended to backup before using.")
    public int resetClaimBlockData = -1;
}