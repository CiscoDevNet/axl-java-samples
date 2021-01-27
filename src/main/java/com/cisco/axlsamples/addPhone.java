package com.cisco.axlsamples;

// Performs <addLine>, then creates a new CTI Port using the line with
// <addPhone>.  Finally, <removePhone> and <removeLine> AXL API operations
// are used to clean up.

// Copyright (c) 2021 Cisco and/or its affiliates.
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
import java.util.List;

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
import com.cisco.axlsamples.api.AddPhoneReq;
import com.cisco.axlsamples.api.AddLineReq;
import com.cisco.axlsamples.api.NameAndGUIDRequest;
import com.cisco.axlsamples.api.ObjectFactory;
import com.cisco.axlsamples.api.RemoveLineReq;
import com.cisco.axlsamples.api.StandardResponse;
import com.cisco.axlsamples.api.XPhone.Lines;
import com.cisco.axlsamples.api.XLine;
import com.cisco.axlsamples.api.XNumplanIdentifier;
import com.cisco.axlsamples.api.XFkType;
import com.cisco.axlsamples.api.XPhone;
import io.github.cdimascio.dotenv.Dotenv;

// To import the entire AXL package contents:
//
// import com.cisco.axl.api.*;

public class addPhone {

    public static void main(String[] args) {

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

        // Create an objectFactory for creating AXL specific objects
        ObjectFactory objectFactory = new ObjectFactory();

        // Create a new line, which will be added along with the CTI RP

        AddLineReq addlineReq = new AddLineReq();
            XLine newLine = new XLine();
            newLine.setPattern( "9999" );
                XFkType routePartitionForeignKey = new XFkType();
                routePartitionForeignKey.setValue( null );
                JAXBElement<XFkType> routePartitionName = objectFactory.createXCtiRoutePointDevicePoolName( routePartitionForeignKey );
            newLine.setRoutePartitionName( routePartitionName );            

        addlineReq.setLine( newLine );

        // Execute the request, wrapped in try/catch in case an exception is thrown
        try {

            StandardResponse response = axlPort.addLine( addlineReq );

            // Dive into the response object's hierarchy to retrieve the <return> value
            System.console().format( "%nAdded Line pkid: " + response.getReturn()  + "%n%n" );
            
        } catch (Exception err) {

            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }
        
        System.console().readLine( "Press Enter to continue..." );

        // Create a new <addCtiRoutePoint> request object and its sub-objects
        // Indentation here attempts to represent the object/sub-object hierarchy
        // The AXL schema documentation can help in understanding parent/child
        // object relations/types
        AddPhoneReq addPhoneReq = new AddPhoneReq();
            XPhone phone = new XPhone();
            phone.setName( "testCtiPort" );
            phone.setProduct( "CTI Port" );
            phone.setClazz( "Phone" );
            phone.setProtocol( "SCCP" );
            phone.setProtocolSide( "User" );
                XFkType devicePoolForeignKey = new XFkType();
                devicePoolForeignKey.setValue( "Default" );
                JAXBElement<XFkType> devicePoolName = objectFactory.createXCtiRoutePointDevicePoolName( devicePoolForeignKey );
            phone.setDevicePoolName( devicePoolName );
                XFkType locationForeignKey = new XFkType();
                locationForeignKey.setValue( "Hub_None" );
            phone.setLocationName( locationForeignKey );
                Lines lines = new Lines();
                // This provides a List object pointing to the <lines><listIdentifier> array
                List<XNumplanIdentifier> linesList = lines.getLineIdentifier();
                    XNumplanIdentifier line = new XNumplanIdentifier();
                    line.setDirectoryNumber( "9999" );
                    line.setRoutePartitionName( null );
                // We can use listLines to manipulate the Lines object, i.e. to add a lineIdentifier
                linesList.add( line );
            phone.setLines( lines );
        addPhoneReq.setPhone( phone );                

        // Execute the request, wrapped in try/catch in case an exception is thrown
        try {

            StandardResponse response = axlPort.addPhone( addPhoneReq );

            // Dive into the response object's hierarchy to retrieve the <return> value
            System.console().format("%nAdded Phone pkid: " + response.getReturn()  + "%n%n");
            
        } catch (Exception err) {

            // If an exception occurs, dump the stacktrace to the console
            err.printStackTrace();
        }
        
        System.console().readLine("Press Enter to continue...");

        // Remove the newly created objects

        // NameAndGUIDRequest type is commonly used for <removeXXX> requests 
        NameAndGUIDRequest removePhoneReq = new NameAndGUIDRequest();

        removePhoneReq.setName( "testCtiPort" );

        try {

            StandardResponse response = axlPort.removePhone( removePhoneReq );

            System.console().format( "%nRemoved Phone pkid: " + response.getReturn()  + "%n" );
            
        } catch (Exception err) {

            err.printStackTrace();
        }


        RemoveLineReq removeLineReq = new RemoveLineReq();

        removeLineReq.setPattern( "9999" );
        removeLineReq.setRoutePartitionName( routePartitionName );

        try {

            StandardResponse response = axlPort.removeLine( removeLineReq );

            System.console().format( "%nRemoved Line pkid: " + response.getReturn()  + "%n%n") ;
            
        } catch (Exception err) {

            err.printStackTrace();
        }
    }
}