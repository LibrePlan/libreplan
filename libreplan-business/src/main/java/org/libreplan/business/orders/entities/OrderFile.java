/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.orders.entities;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.users.entities.User;

import java.util.Date;

/**
 * OrderFile entity representing table: files.
 * This class is intended to work as a Hibernate component.
 * It represents the LibrePlan File to be stored in customer`s data storage.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

public class OrderFile extends BaseEntity {

    private String name;

    private String type;

    private Date date;

    private User uploader;

    private OrderElement parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public OrderElement getParent() {
        return parent;
    }

    public void setParent(OrderElement parent) {
        this.parent = parent;
    }
}
