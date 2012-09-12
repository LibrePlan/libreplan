/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.reports.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.trees.ITreeNode;

/**
 * Utilities methods for report DTOs.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class Util {

    public static final String INDENT_PREFIX = "    ";

    public static BigDecimal getIntegerPart(BigDecimal value) {
        if (value == null) {
            return value;
        }
        return value.setScale(2, RoundingMode.DOWN);
    }

    public static BigDecimal getFractionalPart(BigDecimal value) {
        if (value == null) {
            return value;
        }
        BigDecimal fractionalPart = value.subtract(value.setScale(0,
                RoundingMode.FLOOR));
        return (fractionalPart.compareTo(BigDecimal.ZERO) != 0) ? fractionalPart
                : null;
    }

    public static String getPrefixSpacesDependingOnDepth(ITreeNode<?> node) {
        int depth = 0;
        while (node.getParent() != null && node.getParent().getParent() != null) {
            depth++;
            node = node.getParent();
        }

        return StringUtils.repeat(INDENT_PREFIX, depth);
    }

    public static Boolean isRoot(ITreeNode<?> node) {
        return node.getParent() != null && node.getParent().getParent() == null;
    }

}
