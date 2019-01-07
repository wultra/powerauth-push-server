/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.push.configuration;

import io.getlime.push.client.PushServerClient;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.net.ssl.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Default PowerAuth Service configuration.
 *
 * @author Petr Dvorak
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security", "io.getlime.push"})
public class PowerAuthWebServiceConfiguration {

    @Value("${powerauth.service.url}")
    private String powerAuthServiceUrl;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    /**
     * Return WS-Security interceptor instance using UsernameToken authentication.
     *
     * @return Wss4jSecurityInterceptor instance.
     */
    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
        wss4jSecurityInterceptor.setSecurementActions("UsernameToken");
        wss4jSecurityInterceptor.setSecurementUsername(clientToken);
        wss4jSecurityInterceptor.setSecurementPassword(clientSecret);
        wss4jSecurityInterceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
        return wss4jSecurityInterceptor;
    }

    /**
     * Marshaller for PowerAuth SOAP service communication.
     *
     * @return JAXB marshaller with correctly configured context path.
     */
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths("io.getlime.powerauth.soap.v2", "io.getlime.powerauth.soap.v3");
        return marshaller;
    }

    /**
     * Prepare a correctly configured PowerAuthServiceClient instance with the service
     * URL specified using 'powerauth.service.url' server property.
     *
     * @param marshaller JAXB marshaller
     * @return Correctly configured PowerAuthServiceClient instance with the service
     * URL specified using 'powerauth.service.url' server property
     */
    @Bean
    public PowerAuthServiceClient powerAuthClient(Jaxb2Marshaller marshaller) {
        PowerAuthServiceClient client = new PowerAuthServiceClient();
        client.setDefaultUri(powerAuthServiceUrl);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        // if invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

            }};

            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                // ... ignore
            }

        }


        // if there is a configuration with security credentials, add interceptor
        if (!clientToken.isEmpty()) {
            ClientInterceptor[] interceptors = new ClientInterceptor[]{
                    securityInterceptor()
            };
            client.setInterceptors(interceptors);
        }
        return client;
    }


    /**
     * Initialize PowerAuth 2.0 Push server client.
     * @return Push server client.
     */
    @Bean
    public PushServerClient pushServerClient() {
        // TODO - this code will be obsoleted by universal PA admin in release 2018.12, the resolution of service URI is only temporary
        String host;
        Integer port;
        String scheme;
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            host = InetAddress.getLocalHost().getHostAddress();
            ObjectName objectName = objectNames.iterator().next();
            port = (Integer) beanServer.getAttribute(objectName, "localPort");
            scheme = (String) beanServer.getAttribute(objectName, "scheme");
        } catch (JMException | UnknownHostException ex) {
            host = "127.0.0.1";
            port = 8080;
            scheme = "http";
        }
        return new PushServerClient(scheme + "://" + host + ":" + port + "/powerauth-push-server");
    }

}
