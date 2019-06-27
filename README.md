# axl-java-samples

## Overview

Sample scripts demonstrating usage of the Cisco CUCM AXL SOAP API using Java and the JAX-WS package

Visit the [AXL Developer Site](https://developer.cisco.com/site/axl/)

## Getting started

1. Make sure you have Oracle Java SE 1.8 installed, and `java` is available in the path

    ```bash
    $ java -version
    java version "1.8.0_201"
    Java(TM) SE Runtime Environment (build 1.8.0_201-b09)
    Java HotSpot(TM) 64-Bit Server VM (build 25.201-b09, mixed mode)
    ```

1. Open a terminal and use `git` to clone this repository:

    ```bash
    git clone https://github.com/CiscoDevNet/axl-java-samples.git
    ```

1. Open the `axl-java-samples` Java project in [Visual Studio Code](https://code.visualstudio.com/):

    ```bash
    cd axl-java-samples
    code .
    ```

1. Download the CUCM Tomcat HTTPS certificate (format: x.509 with chain (PEM) ), and place in the `certificate/` folder
  
1. Edit `src/com/cisco/axl/samples/creds.java` to specify your CUCM location, AXL API user credentials, and CUCM HTTPS certificate file name

1. Download and import the AXL service HTTPS certificate into the local Java keystore.

    The following command works on Linux, see the Oracle Java documentation for more info on managing Java certificates. Be sure to replace `{ANYNAME}` and `{CERTFILE}` with your particular values

    ```bash
    sudo $JAVA_HOME/bin/keytool -import -alias {ANYNAME} -file certificate/{CERTFILE} -keystore  $JAVA_HOME/jre/lib/security/cacerts
    ```

1. The AXL 11.5 versions of the WSDL files are included in this proect.  If want to use a different AXL version, download the AXL WSDL files for your CUCM version:

    1. From the CUCM Administration UI, download the 'Cisco AXL Tookit' from **Applications** / **Plugins**

    1. Unzip the Toolkit, and navigate to the `schema/current` folder

    1. Copy/replace the three WSDL files into this project's `schema/` folder:

        ```bash
        AXLAPI.wsdl
        AXLEnums.xsd
        AXLSoap.xsd
        ```

1. Import the AXL WSDL and generate the AXL/JAX-WS `.java` files:

    ```bash
    wsimport -keep -b schema/AXLSoap.xsd -Xnocompile  -s src -d bin -verbose schema/AXLAPI.wsdl
    ```

1. Finally, to launch one of the available samples, in VS Code select the **Debug** panel, choose a launch configuration from the dropdown-list in the upper-left, and click the green 'Start Debugging' arrow:

    ![Launch](images/launch.png)

## Available samples

* `getPhone.java` - Demonstrates querying for configuration information for a specific phone by device name (`<getPhone>`)

* `addMobilityProfile.java` - add and remove a new Mobility Profile (`<addMobilityProfile>`, `<removeMobilityProfile>`)
