package uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialSecurityDocument {
    private String title;
    private String givenName;
    private String familyName;
    private String nino;
}
