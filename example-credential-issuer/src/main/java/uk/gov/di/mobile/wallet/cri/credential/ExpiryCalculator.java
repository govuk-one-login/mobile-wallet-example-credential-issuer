package uk.gov.di.mobile.wallet.cri.credential;

public interface ExpiryCalculator {
    long calculateExpiry(Document document);
}
