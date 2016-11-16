package io.getlime.push.controller.web.model.form;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Form sent when uploading iOS / APNs credentials for the application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UploadIosCredentialsForm {

    @NotNull
    @Size(min = 2)
    @Pattern(flags = { Pattern.Flag.CASE_INSENSITIVE },regexp = "^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$")
    private String bundle;

    @NotNull
    private MultipartFile certificate;

    @AssertTrue
    public boolean isCertificateValid() {
        try {
            final ApnsClient apnsClient = new ApnsClientBuilder()
                    .setClientCredentials(new ByteArrayInputStream(certificate.getBytes()), password)
                    .build();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String password;

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public MultipartFile getCertificate() {
        return certificate;
    }

    public void setCertificate(MultipartFile certificate) {
        this.certificate = certificate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override public String toString() {
        return "UploadIosCredentialsForm{" +
                "bundle='" + bundle + '\'' +
                ", certificate=" + certificate +
                ", password='" + password + '\'' +
                '}';
    }

}
