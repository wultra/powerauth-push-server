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

package io.getlime.push.util;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import sun.security.provider.X509Factory;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with CA Certificates.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class CaCertUtil {

    private static final Logger logger = LoggerFactory.getLogger(CaCertUtil.class);

    public static X509Certificate[] allCerts() {
        // Prepare result list
        final List<X509Certificate> result = new ArrayList<>();

        try {
            // Load the JDK's cacerts keystore file
            final String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
            final FileInputStream is = new FileInputStream(filename);
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            final String password = "changeit";
            keystore.load(is, password.toCharArray());

            // This class retrieves the most-trusted CAs from the keystore
            final PKIXParameters params = new PKIXParameters(keystore);

            // Get the set of trust anchors, which contain the most-trusted CA certificates
            for (TrustAnchor ta : params.getTrustAnchors()) {
                final X509Certificate cert = ta.getTrustedCert();
                result.add(cert);
            }

        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException e) {
            logger.error("Certificate error: {}", e.getMessage(), e);
        }

        // Attempt to add at least GeoTrust Global CA required by Apple for APNs
        try {
            final File resource = new ClassPathResource("cacert/GeoTrust_Global_CA.pem").getFile();
            final String geoTrustCertString = new String(Files.readAllBytes(resource.toPath()));
            final X509Certificate geoTrustCert = certificateFromPem(geoTrustCertString);
            result.add(geoTrustCert);
        } catch (CertificateException | IOException e) {
            logger.error("Certificate error: {}", e.getMessage(), e);
        }

        return result.toArray(new X509Certificate[0]);
    }

    private static X509Certificate certificateFromPem(String pem) throws CertificateException {
        byte[] decoded = BaseEncoding.base64().decode(pem
                .replaceAll(X509Factory.BEGIN_CERT, "")
                .replaceAll(X509Factory.END_CERT, "")
                .replaceAll("\\s", "")
        );
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
    }

}
