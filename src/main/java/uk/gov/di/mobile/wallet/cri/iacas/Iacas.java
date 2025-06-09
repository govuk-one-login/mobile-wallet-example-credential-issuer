package uk.gov.di.mobile.wallet.cri.iacas;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Represents a collection of IACA (Issuing Authority Certificate Authority) certificates. */
@Getter
@Setter
public class Iacas {
    /** The list of IACA certificates. */
    private List<Iaca> data;
}
