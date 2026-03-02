package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentStoreRecordTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void Should_DeserializeSuccessfully_When_AllFieldsPresent() throws Exception {
        String json =
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlMinutes": 60
                }
                """;

        DocumentStoreRecord record = objectMapper.readValue(json, DocumentStoreRecord.class);

        assertEquals("123", record.getItemId());
        assertEquals("456", record.getDocumentId());
        assertEquals("value", record.getData().get("key"));
        assertEquals("ExampleCredentialType", record.getVcType());
        assertEquals(60, record.getCredentialTtlMinutes());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                """
                {
                    "documentId": "456",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlMinutes": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlMinutes": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "vcType": "ExampleCredentialType",
                    "credentialTtlMinutes": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "data": {"key": "value"},
                    "credentialTtlMinutes": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType"
                }
                """
            })
    void Should_ThrowException_When_RequiredFieldMissing(String json) {
        assertThrows(
                MismatchedInputException.class,
                () -> objectMapper.readValue(json, DocumentStoreRecord.class));
    }
}
