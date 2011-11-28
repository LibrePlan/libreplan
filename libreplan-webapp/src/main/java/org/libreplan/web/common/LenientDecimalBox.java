/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.libreplan.web.common;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

import org.zkoss.util.Locales;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Decimalbox;

/**
 * Same behavior as a {@link Decimalbox}, but it always interprets <b>.</b> as
 * decimal separator, even when the locale uses a different separator.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 *
 */
public class LenientDecimalBox extends Decimalbox {

    public LenientDecimalBox() {
        super();
    }

    public LenientDecimalBox(BigDecimal value) throws WrongValueException {
        super(value);
    }

}
