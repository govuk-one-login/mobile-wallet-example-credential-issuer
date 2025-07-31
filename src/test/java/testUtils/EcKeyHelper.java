package testUtils;

import com.nimbusds.jose.jwk.ECKey;

import java.text.ParseException;
import java.util.Base64;

public class EcKeyHelper {

    private static final String PRIVATE_KEY_JWK_BASE64 =
            "eyJrdHkiOiJFQyIsImQiOiI4SGJYN0xib1E1OEpJOGo3eHdfQXp0SlRVSDVpZTFtNktIQlVmX3JnakVrIiwidXNlIjoic2lnIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJmMDYxMWY3Zi04YTI5LTQ3ZTEtYmVhYy1mNWVlNWJhNzQ3MmUiLCJ4IjoiSlpKeE83b2JSOElzdjU4NUVzaWcwYlAwQUdfb1N6MDhSMS11VXBiYl9JRSIsInkiOiJtNjBRMmtMMExiaEhTbHRjS1lyTG8wczE1M1hveF9tVDV2UlV6Z3g4TWtFIiwiaWF0IjoxNzEyMTQ2MTc5fQ==";

    public static ECKey getEcKey() throws ParseException {
        byte[] keyBytes = Base64.getDecoder().decode(PRIVATE_KEY_JWK_BASE64);

        return ECKey.parse(new String(keyBytes));
    }
}
