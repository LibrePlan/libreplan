/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.importers;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.common.util.Base64Utility;
import org.libreplan.importers.tim.RosterResponseDTO;

/**
 * Client to interact with Tim SOAP server.
 *
 * This client creates SOAP message, makes connection to the SOAP server and sends the request.
 * It is also the task of this client to convert the response(xml document) to java objects.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class TimSoapClient {

    private static final Log LOG = LogFactory.getLog(TimSoapClient.class);

    /**
     * Creates request message to be send to the SOAP server
     *
     * @param clazz
     *            object to be marshaled
     * @param userName
     *            the user name
     * @param password
     *            the password
     * @return the created soap message
     * @throws SOAPException
     *             if unable to create message or envelope
     * @throws JAXBException
     *             if unable to marshal the clazz
     */
    private static <T> SOAPMessage createRequest(
            T clazz, String userName, String password) throws SOAPException, JAXBException {

        SOAPMessage message = createMessage();

        addAuthorization(message, userName, password);

        SOAPEnvelope soapEnvelope = createEnvelope(message.getSOAPPart());

        SOAPBody soapBody = soapEnvelope.getBody();
        marshal(clazz, soapBody);

        message.saveChanges();

        return message;
    }

    /**
     * Creates SOAP message to be send to the SOAP server
     *
     * @return the created SOAP message
     * @throws SOAPException
     *             if unable to create soap message
     */
    private static SOAPMessage createMessage() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();

        return messageFactory.createMessage();
    }

    /**
     * Adds authorization to the specified parameter <code>message</code>
     *
     * @param message
     *            the message
     * @param username
     *            the user name
     * @param password
     *            the password
     */
    private static void addAuthorization(SOAPMessage message, String username, String password) {
        String encodeUserInfo = username + ":" + password;
        encodeUserInfo = Base64Utility.encode(encodeUserInfo.getBytes());
        message.getMimeHeaders().setHeader("Authorization", "Basic " + encodeUserInfo);
    }

    /**
     * Creates SOAP envelope and adds namespace declaration and sets encoding
     * style
     *
     * @param soapPart
     *            the message part
     * @return the SOAP envelope
     * @throws SOAPException
     */
    private static SOAPEnvelope createEnvelope(SOAPPart soapPart) throws SOAPException {
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        addNamespaceDeclaration(soapEnvelope);
        setEncodingStyle(soapEnvelope);

        return soapEnvelope;
    }

    /**
     * Adds namespace declaration to the specified parameter
     * <code>soapEnvelop</code>
     *
     * @param soapEnvelope
     *            the SOAP envelope
     * @throws SOAPException
     */
    private static void addNamespaceDeclaration(SOAPEnvelope soapEnvelope) throws SOAPException {
        soapEnvelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        soapEnvelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        soapEnvelope.addNamespaceDeclaration("enc", "http://schemas.xmlsoap.org/soap/encoding/");
        soapEnvelope.addNamespaceDeclaration("env", "http://schemas.xmlsoap.org/soap/envelop/");

    }

    /**
     * Sets the encoding style to the specified parameter
     * <code>soapEnvelop</code>
     *
     * @param soapEnvelope
     *            the SOAP envelope
     * @throws SOAPException
     */
    private static void setEncodingStyle(SOAPEnvelope soapEnvelope) throws SOAPException {
        soapEnvelope.setEncodingStyle("http://schemas.xmlsoap.org/soap/encoding/");
    }

    /**
     * Marshals the specified parameter <code>clazz</code> to the specified
     * <code>soapBody</code>
     *
     * @param clazz
     *            the object to be marshaled
     * @param soapBody
     *            the SOAP body, result of marshal
     * @throws JAXBException
     *             if marshaling failed
     */
    private static <T> void marshal(T clazz, SOAPBody soapBody) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.marshal(clazz, soapBody);
    }

    /**
     * Unmarshals the specified paramter <code>soapBody</code> to the specified
     * <code>clazz</code>
     *
     * @param clazz
     *            object to hold unmarashal result
     * @param soapBody
     *            the soap body to be unmarshalled
     * @return the unmarashalled object
     * @throws JAXBException
     *             if unmarshal failed
     */
    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(Class<T> clazz, SOAPBody soapBody) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Node bindElement = (Node) soapBody.getFirstChild();

        while (bindElement.getNodeType() != Node.ELEMENT_NODE) {
            bindElement = (Node) bindElement.getNextSibling();
        }

        return unmarshaller.unmarshal(bindElement, clazz).getValue();
    }

    /**
     * Sends the SOAP message request to the SOAP server
     *
     * @param url
     *            the endpoint of the web service
     * @param message
     *            the SOAP message to be send
     * @return the response, SOAP message
     * @throws SOAPException
     *             if unable to send request
     */
    private static SOAPMessage sendRequest(String url, SOAPMessage message) throws SOAPException {
        SOAPConnection connection = null;
        SOAPMessage response = null;
        try {
            connection = createConnection();
            response = connection.call(message, url);
        } finally {
            if ( connection != null ) {
                closeConnection(connection);
            }
        }
        return response;
    }


    /**
     * Creates a SOAP connection to the SOAP server
     *
     * @return the SOAPconnection object
     * @throws SOAPException
     *             if unable to create connection
     */
    private static SOAPConnection createConnection() throws SOAPException {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();

        return soapConnectionFactory.createConnection();
    }

    /**
     * Closes the SOAP connection
     *
     * @param connection
     *            the SOAP connection
     * @throws SOAPException
     *             if unable to close connection
     */
    private static void closeConnection(SOAPConnection connection) throws SOAPException {
        connection.close();
    }

    /**
     * Sends soap request to the SOAP server. Receives and unmarshals the
     * response
     *
     * @param url
     *            the SOAP server url(endpoint)
     * @param userName
     *            the user
     * @param password
     *            the password
     * @param request
     *            the request object
     * @param response
     *            the response class
     * @return the expected object or null
     */
    public static <T, U> T sendRequestReceiveResponse(
            String url, String userName, String password, U request, Class<T> response) {

        try {
            SOAPMessage requestMsg = createRequest(request, userName, password);
            SOAPMessage responseMsg = sendRequest(url, requestMsg);

            return unmarshal(response, responseMsg.getSOAPBody());
        } catch (SOAPException soapExp) {
            LOG.error("SOAPException: ", soapExp);
        } catch (JAXBException jaxbExp) {
            LOG.error("JAXBException: ", jaxbExp);
        }

        return null;
    }

    /**
     * Checks authorization for the specified <code>username</code> and
     * <code>password</code>
     *
     * @param url
     *            webservices url
     * @param username
     *            the user
     * @param password
     *            the password
     * @return true if user is authorized otherwise false
     */
    public static boolean checkAuthorization(String url, String username, String password) {
        try {
            SOAPMessage message = createMessage();
            addAuthorization(message, username, password);
            sendRequest(url, message);

            return true;
        } catch (SOAPException e) {
            LOG.error("SOAP Exception: ", e);
        }
        return false;
    }

    /**
     * simulates roster response, to be used for example by unit test
     *
     * unmarshals the roster xml from the specified <code>file</code> and
     * returns {@link RosterResponseDTO}
     *
     * @param file
     *            file with xml contents
     * @return exportRosterDTO if unmarshal succeeded otherwise null
     */
    public static RosterResponseDTO unmarshalRosterFromFile(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(RosterResponseDTO.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            return (RosterResponseDTO) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            LOG.error("Error processing response: ", e);
        }
        return null;
    }
}
