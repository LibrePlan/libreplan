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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * For maximum flexibility, the implementation uses the password encoder configured in the
 * Spring Security configuration file (in consequence, it is possible to change the
 * configuration to use a different password encoder without modifying this class).
 *
 * <p>Passwords are encoded with the configured {@link PasswordEncoder} (BCrypt as of this
 * writing). Verification also accepts hashes produced by the legacy SHA-512 + username-salt
 * scheme this replaced, so that passwords encoded before the migration to BCrypt keep
 * working; a legacy hash is transparently re-encoded with the current scheme by the caller
 * on the next successful login (see {@link LDAPCustomAuthenticationProvider}). See
 * jdk25-migration-baseline/CHANGES-and-WHY.md for the full rationale, including how the
 * legacy algorithm below was verified byte-for-byte against the actual Spring Security 4.2.3
 * source rather than reconstructed from memory.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class DBPasswordEncoderService implements IDBPasswordEncoderService {

    private static final String LEGACY_DIGEST_ALGORITHM = "SHA-512";

    private PasswordEncoder passwordEncoder;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encodePassword(String clearPassword, String loginName) {
        return passwordEncoder.encode(clearPassword);
    }

    @Override
    public boolean matches(String clearPassword, String loginName, String encodedPassword) {
        if ( encodedPassword == null ) {
            return false;
        }

        if ( isLegacyHash(encodedPassword) ) {
            return encodedPassword.equals(legacyEncode(clearPassword, loginName));
        }

        return passwordEncoder.matches(clearPassword, encodedPassword);
    }

    @Override
    public boolean needsRehash(String encodedPassword) {
        return isLegacyHash(encodedPassword);
    }

    /**
     * BCrypt hashes always start with "$2" (e.g. "$2a$", "$2b$", "$2y$" depending on the
     * variant); the legacy scheme's hex-encoded SHA-512 digests never do, so this prefix
     * reliably tells the two formats apart without needing a separate marker column.
     */
    private boolean isLegacyHash(String encodedPassword) {
        return !encodedPassword.startsWith("$2");
    }

    /**
     * Reimplements the pre-migration scheme (Spring Security 4.2.3's
     * {@code ShaPasswordEncoder(512)} + {@code ReflectionSaltSource} configured with
     * "username" as the salt property) using plain JDK APIs, since Spring Security 5
     * removed those classes outright. Verified byte-for-byte against their actual source
     * (not reconstructed from memory) - see jdk25-migration-baseline/CHANGES-and-WHY.md.
     */
    private String legacyEncode(String clearPassword, String loginName) {
        String merged = (clearPassword == null ? "" : clearPassword) + "{" + loginName + "}";

        try {
            byte[] digest = MessageDigest.getInstance(LEGACY_DIGEST_ALGORITHM)
                    .digest(merged.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-512 is a mandatory algorithm for every JDK implementation (see the
            // java.security.MessageDigest javadoc's standard algorithm names), so this
            // can only happen if the JVM itself is broken - not a recoverable condition.
            throw new IllegalStateException(LEGACY_DIGEST_ALGORITHM + " not available", e);
        }
    }

}
