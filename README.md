auth-java
=========

Java authorization API example


Build
===========

Maven is used to manage dependencies and build the jar file for the authorization client.
From the base directory (auth-java), run:

mvn clean install

Running
=============
The maven build generates two jar files in the /target directory:
-  auth-example-1.0-jar-with-dependencies.jar is runnable (using java -jar)
-  auth-example-1.0.jar can be included as a dependency within a project

Notes
==============
Classes in com.bluetarp.authorization.api.v1.model were generated from the Authorization.xsd version 1.0 using xjc.
There are two places where the responses from the bluetarp auth service do not match the schema:

- In Transaction, the annotation for authseqNumber was changed to @XmlElement(name = "auth-seq")

- In AuthorizationResponse, the annotation for transactionId was changed to @XmlElement(name = "transactionId", required = true)