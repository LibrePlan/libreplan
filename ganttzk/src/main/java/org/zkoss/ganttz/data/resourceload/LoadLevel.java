/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.zkoss.ganttz.data.resourceload;

import org.apache.commons.lang.Validate;

public class LoadLevel {

    public enum Category {

        NO_LOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage == 0;
            }
        },
        SOME_LOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage > 0 && percentage < 100;
            }
        },
        FULL_LOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage == 100;
            }
        },
        OVERLOAD {
            @Override
            public boolean contains(int percentage) {
                return percentage > 100;
            }
        };

        protected abstract boolean contains(int percentage);
        public static Category categoryFor(int percentage) {
            for (Category category : values()) {
                if (category.contains(percentage)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("couldn't handle " + percentage);
        }
    }

    private final int percentage;

    public LoadLevel(int percentage) {
        Validate.isTrue(percentage >= 0);
        this.percentage = percentage;

    }

    public int getPercentage() {
        return percentage;
    }

    public Category getCategory() {
        return Category.categoryFor(percentage);
    }


}
