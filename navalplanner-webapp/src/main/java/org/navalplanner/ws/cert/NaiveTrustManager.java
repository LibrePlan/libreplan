/*
 * This file is part of NavalPlan
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

package org.navalplanner.ws.cert;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * This Trust Manager is "naive" because it trusts everyone.
 **/
public class NaiveTrustManager implements X509TrustManager {

    /**
     * Doesn't throw an exception, so this is how it approves a certificate.
     *
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
     *      String)
     **/
    public void checkClientTrusted(X509Certificate[] cert, String authType)
            throws CertificateException {
    }

    /**
     * Doesn't throw an exception, so this is how it approves a certificate.
     *
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
     *      String)
     **/
    public void checkServerTrusted(X509Certificate[] cert, String authType)
            throws CertificateException {
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     **/
    public X509Certificate[] getAcceptedIssuers() {
        return null; // I've seen someone return new X509Certificate[ 0 ];
    }

}
