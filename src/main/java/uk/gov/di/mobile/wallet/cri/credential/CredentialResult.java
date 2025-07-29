package uk.gov.di.mobile.wallet.cri.credential;

import java.util.Date;

public record CredentialResult(String credential, Date expirationTime) {}
