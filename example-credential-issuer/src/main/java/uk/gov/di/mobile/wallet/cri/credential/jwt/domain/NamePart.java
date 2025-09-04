package uk.gov.di.mobile.wallet.cri.credential.jwt.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NamePart {
    private String type;
    private String value;
}
