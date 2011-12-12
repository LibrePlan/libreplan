/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.business.externalcompanies.entities;

import java.util.Comparator;

import org.libreplan.business.externalcompanies.entities.DeadlineCommunication;

/**
* Comparator to {@link DeadlineCommunication}
*
* @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
*/
public class DeadlineCommunicationComparator implements Comparator<DeadlineCommunication> {

        public DeadlineCommunicationComparator(){
        }

        @Override
        public int compare(DeadlineCommunication arg0, DeadlineCommunication arg1) {
            if (arg0.getSaveDate() == arg1.getSaveDate()) {
                return 0;
            }
            if (arg0.getSaveDate() == null) {
                return -1;
            }
            if (arg1.getSaveDate() == null) {
                return 1;
            }
            return arg1.getSaveDate().compareTo(arg0.getSaveDate());
        }
    }
