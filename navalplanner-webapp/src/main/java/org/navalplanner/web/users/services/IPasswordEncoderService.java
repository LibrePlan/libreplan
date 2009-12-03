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

package org.navalplanner.web.users.services;

/**
 * Service for encoding passwords when information about users
 * is stored in the database. In particular, it must be used to encode a
 * password when creating a user and to change a user's password. For
 * maximum flexibility, the implementation of the service uses the password
 * encoder and the salt source configured in the Spring Security configuration
 * file (in consequence, it is possible to change the configuration to use
 * any password encoder and/or salt source without modifying the
 * implementation of this service). The only restriction the implementation
 * imposes is that when using a reflection-based salt source, the "username"
 * property must be specified.
 * <b/>
 * When information about users is maintained externally (e.g. in a LDAP
 * server), this service is not used, since the Web application is not
 * in charge of creating users or changing passwords.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IPasswordEncoderService {

}
