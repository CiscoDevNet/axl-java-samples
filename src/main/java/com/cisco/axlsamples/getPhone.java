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

        if ( debug ) {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dumpTreshold", "999999");
        }

        // Retrieve config environment variables from .env, if present
        Dotenv dotenv = Dotenv.load();

        // Verify the JVM has a console for user input
        if (System.console() == null) {
            System.err.println("Error: This sample app requires a console");
            System.exit(1);
        } else {
            System.console().format("%nWelcome to the getPhone sample app!%n");
        }

        // Instantiate the wsimport generated AXL API Service client --
        // see the wsimport comments in the comments above
        AXLAPIService axlService = new AXLAPIService();
        AXLPort axlPort = axlService.getAXLPort();	
        
        // Set the endpoint address, user, and password 
        // for our particular environment in the JAX-WS client.
        // Edit creds.java to configure these values
        ((BindingProvider) axlPort).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://" + dotenv.get("CUCM") + ":8443/axl/");
        ((BindingProvider) axlPort).getRequestContext().put(
                BindingProvider.USERNAME_PROPERTY, dotenv.get("AXL_USER"));
        ((BindingProvider) axlPort).getRequestContext().put(
                BindingProvider.PASSWORD_PROPERTY, dotenv.get("AXL_PASSWORD"));

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