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
package me.ryanhamshire.griefprevention.configuration.type;

import me.ryanhamshire.griefprevention.configuration.category.*;
import ninja.leaping.configurate.objectmapping.Setting;

public class ConfigBase {

    @Setting(value = "blacklist", comment = "Controls which item/block/entity id's are blacklisted from events either on a per-flag basis or globally. "
            + "\nNote: Id's support wildcards '?' and '*' by using Apache's wildcard matcher."
            + "\nThe wildcard '?' represents a single character."
            + "\nThe wildcard '*' represents zero or more characters."
            + "\nFor more information on usage, see https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FilenameUtils.html#wildcardMatch(java.lang.String,%20java.lang.String)")
    public BlacklistCategory blacklist = new BlacklistCategory();

    @Setting
    public ClaimCategory claim = new ClaimCategory();

    @Setting
    public EconomyCategory economy = new EconomyCategory();

    @Setting
    public FlagCategory flags = new FlagCategory();

    @Setting
    public GeneralCategory general = new GeneralCategory();

    @Setting
    public PvpCategory pvp = new PvpCategory();

    @Setting
    public TownCategory town = new TownCategory();
}