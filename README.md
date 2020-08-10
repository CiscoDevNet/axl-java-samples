# axl-java-samples

## Overview

Sample Visual Studio Code project demonstrating usage of the Cisco CUCM AXL SOAP API using Java 8 and the Oracle JAX-WS packages or OpenJDK 11 and the Eclipse EEE4J JAX-WS packages.

Visit the [AXL Developer Site](https://developer.cisco.com/site/axl/)

>Note: this project was tested using:

* Ubuntu 19.10/20.04 / Mac 10.15.6
* Oracle JDK 8 / OpenJDK 11
* Oracle JAX-WS / Eclipse EEE4J Metro JAX-WS

## Getting started

1. Make sure you have Oracle JDK 1.8 or OpenJDK 11 installed, `java` is available in the path, and the JAVA_HOME environment variable is set:

    ```shell
    $ java -version

    java version "1.8.0_231"
    Java(TM) SE Runtime Environment (build 1.8.0_231-b11)
    Java HotSpot(TM) 64-Bit Server VM (build 25.231-b11, mixed mode)
    ```

    ```shell
    $ echo $JAVA_HOME

    /usr/lib/jvm/jdk1.8.0_231
    ```

1. Open a terminal and use `git` to clone this repository:

    ```bash
    git clone https://github.com/CiscoDevNet/axl-java-samples.git
    ```

1.  If you need to use Oracle JDK8, checkout branch `java8` (future additional samples may appear only in branch `master`, currently supporting OpenJDK 11):

    ```bash
    cd axl-java-samples
    git checkout java8
    ```

1. Download the CUCM Tomcat HTTPS certificate and place in the `certificate/` folder:

    1. Browse to the CUCM OS admin web pages: `https://{cucm}/cmplatform/showHome.do`

    1. Navigate to **Security** / **Certificate Management**

    1. Click **Find** and select the **tomcat** item:

        ![cert_list](images/cert_list.png)

    1. Click **Download .PEM File**, and save into this project's `certificate/` folder.  You may want to give it a recognizable name e.g. the CUCM host name.

1. Import the CUCM Tomcat HTTPS certificate into a local Java keystore.

    >Note: you may want to first copy the default Java `cacerts` file from `$JAVA_HOME/lib/security/cacerts` to `certificates/cacerts` to retain the default certs that ship with Java.  Be sure to set file ownership appropriately.

    The following command works on Ubuntu/Mac, see the Oracle Java documentation for more info on managing Java certificates. Be sure to replace `{CUCM_NAME}` and `{CERT_FILE_NAME}` with your particular values:

    ```bash
    $JAVA_HOME/bin/keytool -trustcacerts -keystore certificate/cacerts -alias {CUCM_NAME} -import -file certificate/{CERT_FILE_NAME}
    ```

    >Note: the default password for the default `cacerts` keystore is: `changeit`

1. The CUCM 12.5 version of the AXL WSDL files are included in this proect.  If want to use a different AXL version, download the AXL WSDL files for your CUCM version:

    1. From the CUCM Administration UI, download the 'Cisco AXL Tookit' from **Applications** / **Plugins**

    1. Unzip the Toolkit, and navigate to the `schema/current` folder

    1. Copy/replace the three WSDL files into this project's `schema/` folder:

        ```bash
        AXLAPI.wsdl
        AXLEnums.xsd
        AXLSoap.xsd
        ```

1. Open the `axl-java-samples` Java project in [Visual Studio Code](https://code.visualstudio.com/):

    ```bash
    code .
    ```

    Upon first opening the project in VS Code, allow some time for VS Code and Maven to auto-generate the AXL API sources/code from the AXL WSDL
  
1. Rename the `.env.example` file to `.env`, and edit it to specify your CUCM hostname and AXL API user credentials.

    >Note: you can specify the config values as environment variables using your preferred method, if desired

1. Finally, to launch one of the available samples, in VS Code select the **Debug** panel, choose a launch configuration from the dropdown-list in the upper-left, and click the green **Start Debugging** arrow:

    ![Launch](images/launch.png)

## Available samples

* `getPhone.java` - Demonstrates querying for configuration information for a specific phone by device name (`<getPhone>`)

* `addMobilityProfile.java` - Add a new Mobility Profile (`<addMobilityProfile>`, `<removeMobilityProfile>`)

* `executeSqlQuery.java` - Performs an executeSqlQuery operation for the applicationusers table and extracts the name and pkid from the response  (`<executeSqlQuery>`)
