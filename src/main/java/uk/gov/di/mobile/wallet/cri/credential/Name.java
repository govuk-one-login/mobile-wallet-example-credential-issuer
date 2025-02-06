package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Name {
    private List<NamePart> nameParts;
}
