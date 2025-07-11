package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;

public record DeviceKeyInfo(COSEKey deviceKey, KeyAuthorizations keyAuthorizations
        //        KeyInfo keyInfo
        ) {}
