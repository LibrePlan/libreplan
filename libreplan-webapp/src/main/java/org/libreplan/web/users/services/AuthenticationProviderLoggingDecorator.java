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

package org.libreplan.web.users.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;

public class AuthenticationProviderLoggingDecorator implements AuthenticationProvider {

    private static final Log LOG = LogFactory
            .getLog(AuthenticationProviderLoggingDecorator.class);

    private AuthenticationProvider decoratedProvider;

    public AuthenticationProvider getDecoratedProvider() {
        return decoratedProvider;
    }

    public void setDecoratedProvider(AuthenticationProvider decoratedProvider) {
        this.decoratedProvider = decoratedProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        Object principal = authentication != null ? authentication
                .getPrincipal() : null;
        LOG.info("trying to authenticate " + principal);
        try {
            Authentication result = decoratedProvider
                    .authenticate(authentication);
            if (result != null) {
                LOG.info("successful authentication for: " + principal
                        + " with provider: " + decoratedProvider);
            }
            return result;
        } catch (AuthenticationException e) {
            LOG.info("unsuccessful authentication of " + principal
                    + " with provider: " + decoratedProvider);
            throw e;
        }
    }

    @Override
    public boolean supports(Class authentication) {
        return decoratedProvider.supports(authentication);
    }

}
