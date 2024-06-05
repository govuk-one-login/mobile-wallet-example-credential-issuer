package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DidKeyResolverTest {

    private DidKeyResolver didKeyResolver;

    @BeforeEach
    void setup() {
        didKeyResolver = new DidKeyResolver();
    }

    @Test
    void shouldDecodeDidKeyIntoAPublicKey() throws AddressFormatException, InvalidDidKeyException {
        DidKeyResolver.DecodedData decodedData =
                didKeyResolver.decodeDIDKey(
                        "did:key:zDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX");
        assertEquals("A85_TEO57jfsASQWD-4bhUvr7Yn0qE8XS4GA_ydRFx3O", decodedData.publicKeyBase64());
        assertEquals(Multicodec.P256_PUB, decodedData.multicodecValue());
        assertEquals(33, decodedData.rawPublicKeyBytes().length);
    }

    @Test
    void shouldThrowInvalidDidKeyExceptionWhenDidKeyPrefixIsInvalid() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDIDKey(
                                        "did:keyzDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX"));
        assertEquals("Expected did:key to start with prefix did:key:", thrown.getMessage());
    }

    @Test
    void shouldThrowInvalidDidKeyExceptionWhenDidKeyMultibaseEncodingPrefixCodeIsNotZ() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDIDKey(
                                        "did:key:DnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX"));
        assertEquals(
                "did:key must be base58 encoded but found multibase code D instead",
                thrown.getMessage());
    }

    @Test
    void shouldThrowInvalidDidKeyExceptionWhenDidKeyBase58EncodingIsInvalid() {
        AddressFormatException thrown =
                assertThrows(
                        AddressFormatException.class,
                        () ->
                                didKeyResolver.decodeDIDKey(
                                        "did:key:zDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX="));
        assertEquals("Illegal character = at 48", thrown.getMessage());
    }

    @Test
    void shouldThrowInvalidDidKeyExceptionWhenDidKeyMulticodecValueIsNotSupported() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDIDKey(
                                        "did:key:z82Lm1MpAkeJcix9K8TMiLd5NMAhnwkjjCBeWHXyu3U4oT2MVJJKXkcVBgjGhnLBn2Kaau9"));
        assertEquals("did:key multicodec value is not supported", thrown.getMessage());
    }

    @Test
    void shouldThrowInvalidDidKeyExceptionWhenPublicKeyLengthIsNot33Bytes() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDIDKey(
                                        "did:key:z3u1stSg9rUT8ZygCoovJN9GSV7CQzETPPuPMG3D7b6RqsFK"));
        assertEquals("Expected key length of 33, but found 32 instead", thrown.getMessage());
    }
}
