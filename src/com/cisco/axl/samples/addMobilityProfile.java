package com.cisco.axl.samples;

// Performs <addMobilityProfile>, and then <removeMobilityProfile> AXL API operations
// with some hard-coded values.
//
// The Java package used to build specific AXL objects and execute API
// requests needs to be auto-generated using the AXL WSDL and the JAX-WS 
// wsimport command (run from the project root directory):
//
//   wsimport -keep -b schema/AXLSoap.xsd -Xnocompile  -s src -d bin -p com.cisco.axl.api -verbose schema/AXLAPI.wsdl
//
// As AXL uses HTTPS, you will need to download and install AXL's HTTPS certificate
// into the local Java keystore.  The following command works on Linux,
// see the Oracle Java documentation for more info on managing Java certificates.
// (Be sure to replace {ANYNAME} and {CERTFILE} with your particular values)
//
//   sudo $JAVA_HOME/bin/keytool -import -alias {ANYNAME} -file certificate/{CERTFILE} -keystore  $JAVA_HOME/jre/lib/security/cacerts
//
// Tested using:
//
// Ubuntu Linux 19.04
// Java 1.8u201
// CUCM 11.5

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

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

// import only the XL package modules needed for this sample
import com.cisco.axl.api.AXLAPIService;
import com.cisco.axl.api.AXLPort;
import com.cisco.axl.api.AddMobilityProfileReq;
import com.cisco.axl.api.NameAndGUIDRequest;
import com.cisco.axl.api.ObjectFactory;
import com.cisco.axl.api.StandardResponse;
import com.cisco.axl.api.XDirn;
import com.cisco.axl.api.XFkType;
import com.cisco.axl.api.XMobilityProfile;

// To import the entire AXL package contents:
//
// import com.cisco.axl.api.*;

public class addMobilityProfile {

    public static void main(String[] args) {

        // Verify the JVM has a console for user input
        if (System.console() == null) {
            System.err.println("Error: This sample app requires a console");
            System.exit(1);
        } else {
            System.console().format("%nWelcome to the addMobilityProfile sample app.%n%n");
        }

        // Instantiate the wsimport generated AXL API Service client --
        // see the wsimport comments in the comments above
        AXLAPIService axlService = new AXLAPIService();
        AXLPort axlPort = axlService.getAXLPort();	
        
        // Set the endpoint address, user, and password 
        // for our particular environment in the JAX-WS client.
        // Edit creds.java to configure these values
        ((BindingProvider) axlPort).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://" + creds.CUCM + ":8443/axl/");
        ((BindingProvider) axlPort).getRequestContext().put(
                BindingProvider.USERNAME_PROPERTY, creds.USER);
        ((BindingProvider) axlPort).getRequestContext().put(
                BindingProvider.PASSWORD_PROPERTY, creds.PASSWORD);

        // Create an object representing a <getPhone> request
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

                    foreignKey.setValue("testPartition");

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