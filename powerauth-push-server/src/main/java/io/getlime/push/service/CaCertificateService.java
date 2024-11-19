/*
 * Copyright 2021 Wultra s.r.o.
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

package io.getlime.push.service;

import io.getlime.push.configuration.PushServiceConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.*;

/**
 * Service for working with CA Certificates.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaCertificateService {

    // Include those constants to remove dependency on X509Factory.BEGIN_CERT and X509Factory.END_CERT.
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";

    private static final List<String> EMBEDDED_CERTIFICATES = List.of(
            "classpath:/cacert/GeoTrust_Global_CA.pem",
            "classpath:/cacert/AAACertificateServices.pem",
            "classpath:/cacert/COMODORSAAAACA.pem",
            "classpath:/cacert/USERTrustRSAAAACA.pem"
    );

    private final PushServiceConfiguration pushServiceConfiguration;

    private final ResourceLoader resourceLoader;

    private List<X509Certificate> allCertificates;

    /**
     * Obtain all registered CA certificates.
     * @return All registered CA certificates.
     */
    public X509Certificate[] allCerts() {
        return allCertificates.toArray(X509Certificate[]::new);
    }

    /**
     * Load the locally stored CA certificates required by Apple for APNs.
     */
    @PostConstruct
    protected void loadAllCertificates() throws IOException, KeyStoreException, InvalidAlgorithmParameterException, CertificateException, NoSuchAlgorithmException {
        final List<X509Certificate> result = new ArrayList<>();

        final String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
        try (FileInputStream is = new FileInputStream(filename)) {
            // Load the JDK's cacerts keystore file
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            final String password = pushServiceConfiguration.getJavaCaCertificatesPassword();
            keystore.load(is, password.toCharArray());

            // This class retrieves the most-trusted CAs from the keystore
            final PKIXParameters params = new PKIXParameters(keystore);

            // Get the set of trust anchors, which contain the most-trusted CA certificates
            for (TrustAnchor ta : params.getTrustAnchors()) {
                final X509Certificate cert = ta.getTrustedCert();
                result.add(cert);
            }

        }

        result.addAll(loadEmbeddedCertificate());

        allCertificates = result;
    }

    /**
     * Load the locally stored CA certificates required by Apple for APNs.
     *
     * @throws CertificateException If unable to create the certificate.
     * @throws IOException If unable to load file.
     */
    private List<X509Certificate> loadEmbeddedCertificate() throws CertificateException, IOException {
        final List<X509Certificate> result = new ArrayList<>();
        for (final String certPath : EMBEDDED_CERTIFICATES) {
            logger.info("Importing embedded certificate: {}", certPath);
            final Resource resource = resourceLoader.getResource(certPath);
            try (final InputStream inputStream = resource.getInputStream()) {
                final String certString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                final X509Certificate cert = certificateFromPem(certString);
                result.add(cert);
            }
        }
        return result;
    }

    private static X509Certificate certificateFromPem(String pem) throws CertificateException {
        final byte[] decoded = Base64.getDecoder().decode(pem
                .replace(BEGIN_CERT, "")
                .replace(END_CERT, "")
                .replaceAll("\\s", "")
        );
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
    }

}
