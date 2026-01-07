package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.di.mobile.wallet.cri.credential.proof.Proof;

/*
As per the OID4VCI specification, the request body should include either
'credential_configuration_id' or 'credential_identifier',
depending on what is returned in the response body from the authorization server token response.
However, the One Login authorization server works slightly differently
and does not return those values in the token response body.
Instead, it returns 'credential_identifiers' (an array containing one 'credential_identifier')
as part of the access token payload. Since the Wallet includes this access token in the
credential request, the credential can be uniquely identified,
making it unnecessary to include these parameters in the request body.
https://govukverify.atlassian.net/browse/DCMAW-13558?focusedCommentId=279751
*/

@Getter
public class RequestBody {

    @JsonProperty("proof")
    private Proof proof;

    @JsonCreator
    public RequestBody(@JsonProperty(value = "proof", required = true) Proof proof) {
        this.proof = proof;
    }
}
