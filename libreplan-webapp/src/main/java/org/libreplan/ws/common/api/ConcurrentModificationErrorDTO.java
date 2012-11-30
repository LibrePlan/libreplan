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

package org.libreplan.ws.common.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for representing any concurrent modification exception in the web
 * services.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@XmlRootElement(name = "concurrent-modification-error")
public class ConcurrentModificationErrorDTO {

    @XmlAttribute(name = "message")
    public String message;

    @XmlElement(name = "stack-trace")
    public String stackTrace;

    public ConcurrentModificationErrorDTO() {
    }

    public ConcurrentModificationErrorDTO(String message, String stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
    }

}
