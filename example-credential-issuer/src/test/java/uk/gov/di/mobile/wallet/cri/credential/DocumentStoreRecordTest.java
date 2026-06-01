package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

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
                    "credentialTtlSeconds": 60
                }
                """;

        DocumentStoreRecord documentStoreRecord =
                objectMapper.readValue(json, DocumentStoreRecord.class);

        assertEquals("123", documentStoreRecord.getItemId());
        assertEquals("456", documentStoreRecord.getDocumentId());
        assertEquals("value", documentStoreRecord.getData().get("key"));
        assertEquals("ExampleCredentialType", documentStoreRecord.getVcType());
        assertEquals(60, documentStoreRecord.getCredentialTtlSeconds());
    }

    @Test
    void Should_DeserializeExpectedUpdateSeconds_When_Present() throws Exception {
        String json =
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlSeconds": 60,
                    "expectedUpdateSeconds": 30
                }
                """;

        DocumentStoreRecord documentStoreRecord =
                objectMapper.readValue(json, DocumentStoreRecord.class);

        assertEquals(Optional.of(30L), documentStoreRecord.getExpectedUpdateSeconds());
    }

    @Test
    void Should_ReturnEmptyOptional_When_ExpectedUpdateSecondsAbsent() throws Exception {
        String json =
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlSeconds": 60
                }
                """;

        DocumentStoreRecord documentStoreRecord =
                objectMapper.readValue(json, DocumentStoreRecord.class);

        assertEquals(Optional.empty(), documentStoreRecord.getExpectedUpdateSeconds());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                """
                {
                    "documentId": "456",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlSeconds": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "data": {"key": "value"},
                    "vcType": "ExampleCredentialType",
                    "credentialTtlSeconds": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "vcType": "ExampleCredentialType",
                    "credentialTtlSeconds": 60
                }
                """,
                """
                {
                    "itemId": "123",
                    "documentId": "456",
                    "data": {"key": "value"},
                    "credentialTtlSeconds": 60
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
