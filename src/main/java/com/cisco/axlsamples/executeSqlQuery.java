package com.cisco.axlsamples;

// Performs a <executeSqlQuery> operation for the applicationusers table and extracts
// the name and pkid from the response using the AXL API.

// Copyright (c) 2019 Cisco and/or its affiliates.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javax.xml.ws.BindingProvider;

import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

// Import only the AXL package modules needed for this sample
import com.cisco.axlsamples.api.AXLAPIService;
import com.cisco.axlsamples.api.AXLError_Exception;
import com.cisco.axlsamples.api.AXLPort;
import com.cisco.axlsamples.api.ExecuteSQLQueryReq;
import com.cisco.axlsamples.api.ExecuteSQLQueryRes;

// To import the entire AXL package contents:
//
// import com.cisco.axlsamples.api.*;

import io.github.cdimascio.dotenv.Dotenv;

public class executeSqlQuery {

    public static void main( String[] args ) throws NoSuchAlgorithmException, KeyManagementException, AXLError_Exception {

        // Retrieve environment variables from .env, if present
        Dotenv dotenv = Dotenv.load();

        Boolean debug = dotenv.get( "DEBUG" ).equals( "True" );

        if ( debug ) { 
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            // Increase the dump output permitted size
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
        }

        // Verify the JVM has a console for user input
        if ( System.console() == null ) {
            System.err.println( "Error: This sample app requires a console" );
            System.exit( 1 );
        }

        // Instantiate the generated AXL API Service client
        AXLAPIService axlService = new AXLAPIService();

        // Get access to the request context so we can set custom params
        AXLPort axlPort = axlService.getAXLPort();
        Map< String, Object > requestContext = ( ( BindingProvider ) axlPort ).getRequestContext();

        // Set the AXL API endpoint address, user, and password
        //   for our particular environment in the JAX-WS client.
        //   Configure these values in .env
        requestContext.put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://" + dotenv.get( "CUCM" ) + ":8443/axl/");
        requestContext.put( BindingProvider.USERNAME_PROPERTY, dotenv.get( "AXL_USER" ) );
        requestContext.put( BindingProvider.PASSWORD_PROPERTY, dotenv.get( "AXL_PASSWORD" ) );
        // Enable cookies for AXL authentication session reuse
        requestContext.put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );

        // Uncomment the section below to disable HTTPS certificate checking,
        //   otherwise import the CUCM Tomcat certificate - see README.md

        // X509TrustManager[] trustAll = new X509TrustManager[] { new X509TrustManager() {
        //     public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
        //     public void checkClientTrusted( X509Certificate[] arg0, String arg1) throws CertificateException { };
        //     public void checkServerTrusted( X509Certificate[] arg0, String arg1) throws CertificateException { };
        //     }
        // };
        // SSLContext context = SSLContext.getInstance( "TLS" );
        // context.init( null, trustAll, new java.security.SecureRandom() );
        // provider.getRequestContext().put( "com.sun.xml.ws.transport.https.client.SSLSocketFactory", context.getSocketFactory() );
        
        // Use a local trust store file to validate HTTPS certificates.
        //   Requires importing the CUCM Tomcat certificate from CUCM into file certificate/local_truststore, see README.md
        System.setProperty( "javax.net.ssl.trustStore", "certificate/cacerts" );
        System.setProperty( "javax.net.ssl.trustStorePassword", "changeit" );

        // Create an executeSqlQuery request object
        ExecuteSQLQueryReq query = new ExecuteSQLQueryReq();

        // Set the text of the SQL query
        query.setSql( "select name, pkid from applicationuser" );

        List<Object> user_list = null;

        try {
            // Prepare a ExecuteSQLQueryRes object to receive the response from AXL
            ExecuteSQLQueryRes resp = axlPort.executeSQLQuery( query );

            // getRow() returns all of the rows as a List<Object> type
            user_list = resp.getReturn().getRow();
        } catch ( Exception err ) {
            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }

        // Create an iterator to cycle through each row, below
        Iterator<Object> itr = user_list.iterator();

        // While the iterator indicates there is at least one more row...
        while ( itr.hasNext() ) {

            // The individual row object is of org.w3c.dom.Element type - we'll need to cast from generic Object here
            org.w3c.dom.Element el = (org.w3c.dom.Element) itr.next();

            // Print out the formatted name and pkid values
            System.out.println(
                "Name: " + String.format( "%-20s", el.getElementsByTagName( "name" ).item( 0 ).getTextContent() )+
                " PKID: " + el.getElementsByTagName( "pkid" ).item( 0 ).getTextContent() 
                );
        }

    }
}