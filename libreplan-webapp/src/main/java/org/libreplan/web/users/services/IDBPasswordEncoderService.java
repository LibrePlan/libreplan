/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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
 * Service for encoding passwords when information about users is stored in the database.
 * In particular, it must be used to encode a password when creating a user and to change a user's password.
 * <b/>
 * When information about users is maintained externally (e.g. in a LDAP server), this service is not used,
 * since the Web application is not in charge of creating users or changing passwords.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IDBPasswordEncoderService {

    /**
     * Encodes a clear password using the current (BCrypt) scheme. The second parameter
     * (loginName) is kept for source compatibility with existing callers, but is not used:
     * BCrypt generates and embeds its own random salt, unlike the legacy SHA-512 scheme this
     * replaced (see jdk25-migration-baseline/CHANGES-and-WHY.md).
     */
    String encodePassword(String clearPassword, String loginName);

    /**
     * Checks a clear password against a previously encoded one. Supports both current
     * (BCrypt) hashes and hashes produced by the legacy SHA-512 scheme this replaced, so
     * that passwords encoded before the migration keep working. See
     * jdk25-migration-baseline/CHANGES-and-WHY.md for why both are needed.
     */
    boolean matches(String clearPassword, String loginName, String encodedPassword);

    /**
     * True if the given previously-encoded password uses the legacy SHA-512 scheme rather
     * than the current one, i.e. it should be re-encoded (with {@link #encodePassword}) and
     * the result saved next time the caller has the matching clear password on hand (for
     * example right after a successful {@link #matches} check during login). Re-encoding
     * unconditionally on every login is not a safe way to detect this: encoding the same
     * password twice with the current (BCrypt) scheme never produces the same string twice,
     * since it embeds a fresh random salt every time.
     */
    boolean needsRehash(String encodedPassword);

}
