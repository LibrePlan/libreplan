/*
 * This file is part of LibrePlan
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

package org.libreplan.web.users.services;

/**
 * Service for encoding passwords when information about users
 * is stored in the database. In particular, it must be used to encode a
 * password when creating a user and to change a user's password.
 * <b/>
 * When information about users is maintained externally (e.g. in a LDAP
 * server), this service is not used, since the Web application is not
 * in charge of creating users or changing passwords.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IDBPasswordEncoderService {

    /**
     * Encodes a clear password. The second parameter (which must be the
     * username) may be used as a salt.
     */
    public String encodePassword(String clearPassword, String loginName);

}
