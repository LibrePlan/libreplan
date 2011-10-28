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

package org.libreplan.ws.cert;

import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

/**
 * Provides all secure socket factories, with a socket that ignores problems in
 * the chain of certificate trust. This is good for embedded applications that
 * just want the encryption aspect of SSL communication, without worrying too
 * much about validating the identify of the server at the other end of the
 * connection. In other words, this may leave you vulnerable to a
 * man-in-the-middle attack.
 */
public final class NaiveTrustProvider extends Provider {

    /** The name of our algorithm **/
    private static final String TRUST_PROVIDER_ALG = "NaiveTrustAlgorithm";

    /** Need to refer to ourselves somehow to know if we're already registered **/
    private static final String TRUST_PROVIDER_ID = "NaiveTrustProvider";

    /**
     * Hook in at the provider level to handle libraries and 3rd party utilities
     * that use their own factory. Requires permission to execute
     * AccessController.doPrivileged, so this probably won't work in applets or
     * other high-security jvms
     **/

    public NaiveTrustProvider() {
        super(
                TRUST_PROVIDER_ID,
                (double) 0.1,
                "NaiveTrustProvider (provides all secure socket factories by ignoring problems in the chain of certificate trust)");

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                put("TrustManagerFactory."
                        + NaiveTrustManagerFactory.getAlgorithm(),
                        NaiveTrustManagerFactory.class.getName());
                return null;
            }
        });
    }

    /**
     * This is the only method the client code need to call. Yup, just put
     * NaiveTrustProvider.setAlwaysTrust() into your initialization code and
     * you're good to go
     *
     * @param enableNaiveTrustProvider
     *            set to true to always trust (set to false it not yet
     *            implemented)
     **/

    public static void setAlwaysTrust(boolean enableNaiveTrustProvider) {
        if (enableNaiveTrustProvider) {
            Provider registered = Security.getProvider(TRUST_PROVIDER_ID);
            if (null == registered) {
                Security.insertProviderAt(new NaiveTrustProvider(), 1);
                Security.setProperty("ssl.TrustManagerFactory.algorithm",
                        TRUST_PROVIDER_ALG);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Disable Naive trust provider not yet implemented");
        }
    }

    /**
     * The factory for the NaiveTrustProvider
     **/
    public final static class NaiveTrustManagerFactory extends
            TrustManagerFactorySpi {
        public NaiveTrustManagerFactory() {
        }

        protected void engineInit(ManagerFactoryParameters mgrparams) {
        }

        protected void engineInit(KeyStore keystore) {
        }

        /**
         * Returns a collection of trust managers that are naive. This
         * collection is just a single element array containing our
         * {@link NaiveTrustManager} class.
         **/
        protected TrustManager[] engineGetTrustManagers() {
            // Returns a new array of just a single NaiveTrustManager.
            return new TrustManager[] { new NaiveTrustManager() };
        }

        /**
         * Returns our "NaiveTrustAlgorithm" string.
         *
         * @return The string, "NaiveTrustAlgorithm"
         */
        public static String getAlgorithm() {
            return TRUST_PROVIDER_ALG;
        }

    }

}
