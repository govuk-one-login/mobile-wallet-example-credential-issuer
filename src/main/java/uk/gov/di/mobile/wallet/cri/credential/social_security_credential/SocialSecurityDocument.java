package uk.gov.di.mobile.wallet.cri.credential.social_security_credential;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SocialSecurityDocument {
    private String title;
    private String givenName;
    private String familyName;
    private String nino;
    private long credentialTtlMinutes;
}
