package uk.gov.di.mobile.wallet.cri.credential.basic_check_credential;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BasicCheckRecord {
    private String certificateNumber;
    private String applicationNumber;
    private String certificateType;
    private String outcome;
    private String policeRecordsCheck;
}
