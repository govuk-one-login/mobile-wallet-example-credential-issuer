package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Represents a list of IACA (Issuing Authority Certificate Authority) certificates. */
@Getter
@Setter
public class Iacas {
    /** The list of IACA certificates. */
    private List<Iaca> data;

    public Iacas(List<Iaca> data) {
        this.data = data;
    }
}
