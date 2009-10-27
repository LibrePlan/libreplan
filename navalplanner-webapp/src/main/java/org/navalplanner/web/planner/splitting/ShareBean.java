/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner.splitting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class ShareBean {

    public static int[] toHours(ShareBean... shares) {
        Validate.noNullElements(shares);
        int[] result = new int[shares.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = shares[i].getHours();
        }
        return result;
    }

    public static int sum(Collection<? extends ShareBean> shareBeans) {
        Validate.noNullElements(shareBeans);
        int result = 0;
        for (ShareBean shareBean : shareBeans) {
            result += shareBean.getHours();
        }
        return result;
    }

    public static List<ShareBean> toShareBeans(String name, int[] hours) {
        ArrayList<ShareBean> result = new ArrayList<ShareBean>();
        for (int i = 0; i < hours.length; i++) {
            ShareBean s = new ShareBean();
            s.setName(name + "." + (i + 1));
            s.setHours(hours[i]);
            result.add(s);
        }
        return result;
    }

    private String name;

    private Integer hours;

    public ShareBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (StringUtils.isEmpty(name))
            return;
        this.name = name;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer share) {
        if (share == null || share <= 0)
            return;
        this.hours = share;
    }

}
