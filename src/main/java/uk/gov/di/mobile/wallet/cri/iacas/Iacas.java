package uk.gov.di.mobile.wallet.cri.iacas;

import java.util.List;

/**
 * Represents a list of IACA (Issuing Authority Certificate Authority) certificates.
 *
 * @param data The list of IACA certificates.
 */
public record Iacas(List<Iaca> data) {}
