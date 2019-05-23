# Migration from 0.21.0 to 0.22.0

## Java 11 Support

PowerAuth Push Server now supports Java 11.

### Tomcat on Java 11

We have tested PowerAuth on Tomcat `9.0.16` with Java 11, so please use this version or higher. Older versions of Tomcat may not work properly with Java 11.

### Other Web Containers on Java 11

Make sure you upgrade the web container to a version which supports Java 11 before deploying PowerAuth server. 

## Improved JBoss / Wildfly Support

PowerAuth Push Server now contains a JBoss Deployment Descriptor. For more details see: [Deploy Push Server on JBoss / Wildfly](./Deploying-Wildfly.md)
