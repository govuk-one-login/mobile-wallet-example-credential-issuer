package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSEKey;

public record DeviceKeyInfo(COSEKey deviceKey, KeyAuthorizations keyAuthorizations) {}
