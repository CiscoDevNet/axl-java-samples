package com.cisco.axlsamples;

// Performs a <getEnterprisePhoneConfig> operation, then parses/prints
// a simple report to the console.

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

import javax.xml.ws.BindingProvider;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
import com.cisco.axlsamples.api.GetEnterprisePhoneConfigReq;
import com.cisco.axlsamples.api.GetEnterprisePhoneConfigRes;

// To import the entire AXL package contents:
//
// import com.cisco.axlsamples.api.*;

import io.github.cdimascio.dotenv.Dotenv;

public class getEnterprisePhoneConfig {

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

        // Create request/response objects - nothing needs to be configured on the request object
        GetEnterprisePhoneConfigReq request = new GetEnterprisePhoneConfigReq();
        GetEnterprisePhoneConfigRes response = null;

        try { 
            // Execute the request
            response = axlPort.getEnterprisePhoneConfig( request );

        } catch ( Exception err ) {
            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }

        // Extract the vendorConfig contents, which will be a Java List of Element objects
        List<Element> vendorConfig = response.getReturn().getEnterprisePhoneConfig().getVendorConfig().getValue().getAny();

        System.out.println( "\nProduct Specific Configuration Layout" );
        System.out.println( "=====================================" );

        for (Element el : vendorConfig) {

            // If this element's first child contains a text value...
            if ( el.getFirstChild().getNodeType() == Element.TEXT_NODE ) {

                System.out.println( el.getTagName() + ": " + el.getFirstChild().getTextContent() );
            }
            else {

                // This element contains subelements...
                System.out.println( "\n" + el.getTagName() );
                System.out.println( "-".repeat( el.getTagName().length() ) );

                NodeList children = el.getChildNodes();

                // Loop through all the child elements and print out the name/value
                for ( int i = 0; i < children.getLength()-1; i++  ) {

                        System.out.println( "\t" + children.item( i ).getNodeName() + ": " + children.item( i ).getTextContent() );
                }
            }

        }
    }

}