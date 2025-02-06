package uk.gov.di.mobile.wallet.cri.credential.digitalVeteranCard;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VeteranCard {
    private String expiryDate;
    private String serviceNumber;
    private String serviceBranch;
    private String photo;
}
