package uk.gov.di.mobile.wallet.cri.credential.basicDiscloureCredential;

public class BasicCheckRecord {
    private String certificateNumber;
    private String applicationNumber;
    private String certificateType;
    private String outcome;
    private String policeRecordsCheck;

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getPoliceRecordsCheck() {
        return policeRecordsCheck;
    }

    public void setPoliceRecordsCheck(String policeRecordsCheck) {
        this.policeRecordsCheck = policeRecordsCheck;
    }
}
