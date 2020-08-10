package com.cisco.axlsamples;

// Performs a <getPhone> operation and extracts the 'product' type
// using the AXL API.

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

import javax.xml.ws.BindingProvider;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

// Import only the AXL package modules needed for this sample
import com.cisco.axlsamples.api.AXLAPIService;
import com.cisco.axlsamples.api.AXLPort;
import com.cisco.axlsamples.api.GetPhoneReq;
import com.cisco.axlsamples.api.GetPhoneRes;

// Dotenv for Java
import io.github.cdimascio.dotenv.Dotenv;

// To import the entire AXL package contents:
//
// import com.cisco.axlsamples.api.*;

public class getPhone {
    
    public static void main(String[] args) {

        Boolean debug = false;

        if ( debug ) { System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true"); }

        // Retrieve config environment variables from .env, if present
        Dotenv dotenv = Dotenv.load();

        // Verify the JVM has a console for user input
        if (System.console() == null) {
            System.err.println("Error: This sample app requires a console");
            System.exit(1);
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

        // Create a new <getPhone> request object
        GetPhoneReq req = new GetPhoneReq();

        // Get the device name to retrieve from the user via the console
        String phoneName = System.console().readLine("%nPhone device name to retrieve: ");

        req.setName(phoneName);
        
        // Prepare a GetPhoneRes object to receive the response from AXL
        GetPhoneRes getPhoneResponse;
        
        // Execute the request, wrapped in try/catch in case an exception is thrown
        try {
            getPhoneResponse = axlPort.getPhone(req);

            // Dive into the response object's hierarchy to retrieve the <product> value
            System.console().format("%nPhone product type is: " + 
                getPhoneResponse.getReturn().getPhone().getProduct() + "%n%n");
            
        } catch (Exception err) {

            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }
    }
}