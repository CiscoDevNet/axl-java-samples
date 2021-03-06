package com.cisco.axlsamples;

// Performs <addMobilityProfile>, and then <removeMobilityProfile> AXL API operations
// with some hard-coded values.

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
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

// import only the XL package modules needed for this sample
import com.cisco.axlsamples.api.AXLAPIService;
import com.cisco.axlsamples.api.AXLPort;
import com.cisco.axlsamples.api.AddMobilityProfileReq;
import com.cisco.axlsamples.api.NameAndGUIDRequest;
import com.cisco.axlsamples.api.ObjectFactory;
import com.cisco.axlsamples.api.StandardResponse;
import com.cisco.axlsamples.api.XDirn;
import com.cisco.axlsamples.api.XFkType;
import com.cisco.axlsamples.api.XMobilityProfile;
import io.github.cdimascio.dotenv.Dotenv;

// To import the entire AXL package contents:
//
// import com.cisco.axl.api.*;

public class addMobilityProfile {

    public static void main(String[] args) {

        // Retrieve environment variables from .env, if present
        Dotenv dotenv = Dotenv.load();

        Boolean debug = dotenv.get( "DEBUG" ).equals( "True" );

        if ( debug ) { System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true"); }

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

        // Create an objectFactory for creating AXL specific objects
        ObjectFactory objectFactory = new ObjectFactory();

        // Create a new <addMobilityProfile> request object and its sub-objects
        // Indentation here attempts to represent the object/sub-object hierarchy
        // The AXL schema documentation can help in understanding parent/child
        // object relations/types
        AddMobilityProfileReq addReq = new AddMobilityProfileReq();
            XMobilityProfile profile = new XMobilityProfile();
                profile.setName("testMobilityProfile");
                profile.setMobileClientCallingOption("Dial via Office Reverse");
                profile.setDvofServiceAccessNumber("1000");
                XDirn dn = new XDirn();
                    dn.setPattern("1000");
                        XFkType foreignKey = new XFkType();
                            foreignKey.setValue(null);
                    JAXBElement<XFkType> partition = objectFactory.createXDirnRoutePartitionName(foreignKey);
                    dn.setRoutePartitionName(partition);
                profile.setDirn(dn);
                JAXBElement<String> callerid = objectFactory.createXMobilityProfileDvorCallerId("1003");
                profile.setDvorCallerId(callerid);
            addReq.setMobilityProfile(profile);
        
        // Execute the request, wrapped in try/catch in case an exception is thrown
        try {

            StandardResponse response = axlPort.addMobilityProfile(addReq);

            // Dive into the response object's hierarchy to retrieve the <return> value
            System.console().format("%nAdded Mobility Profile pkid: " + response.getReturn()  + "%n%n");
            
        } catch (Exception err) {

            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }
        
        System.console().readLine("Press Enter to continue...");

        // Remove the newly created mobility profile

        // NameAndGUIDRequest type is commonly used for <removeXXX> requests 
        NameAndGUIDRequest removeReq = new NameAndGUIDRequest();

        removeReq.setName("testMobilityProfile");

        try {

            StandardResponse response = axlPort.removeMobilityProfile(removeReq);

            System.console().format("%nRemoved object pkid: " + response.getReturn()  + "%n%n");
            
        } catch (Exception err) {

            err.printStackTrace();
        }
    }
}