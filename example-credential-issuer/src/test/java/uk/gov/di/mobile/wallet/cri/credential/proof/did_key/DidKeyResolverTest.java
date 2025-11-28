package uk.gov.di.mobile.wallet.cri.credential.proof.did_key;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DidKeyResolverTest {

    private DidKeyResolver didKeyResolver;

    @BeforeEach
    void setup() {
        didKeyResolver = new DidKeyResolver();
    }

    @Test
    void should_Decode_DidKey_Into_A_PublicKey()
            throws AddressFormatException, InvalidDidKeyException {
        DidKeyResolver.DecodedKeyData decodedKeyData =
                didKeyResolver.decodeDidKey(
                        "did:key:zDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX");
        assertEquals(
                "A85_TEO57jfsASQWD-4bhUvr7Yn0qE8XS4GA_ydRFx3O", decodedKeyData.publicKeyBase64());
        assertEquals(Multicodec.P256_PUB, decodedKeyData.multicodecValue());
        assertEquals(33, decodedKeyData.rawPublicKeyBytes().length);
    }

    @Test
    @DisplayName("should Throw Invalid Did Key Exception when Did Key Prefix is invalid")
    void should_ThrowException_When_DidKey_Prefix_Is_Invalid() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDidKey(
                                        "did:keyzDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX"));
        assertEquals("Expected did:key to start with prefix did:key:", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "should Throw Invalid Did Key Exception when Did Key Multibase Encoding Prefix Code is not Z")
    void should_ThrowException_When_DidKey_MultibaseEncoding_Prefix_Code_Is_Not_Z() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDidKey(
                                        "did:key:DnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX"));
        assertEquals(
                "did:key must be base58 encoded but found multibase code D instead",
                thrown.getMessage());
    }

    @Test
    @DisplayName("should Throw Invalid Did Key Exception when Did Key Base58 Encoding is invalid")
    void should_ThrowException_When_DidKey_Base58Encoding_Is_Invalid() {
        AddressFormatException thrown =
                assertThrows(
                        AddressFormatException.class,
                        () ->
                                didKeyResolver.decodeDidKey(
                                        "did:key:zDnaewZMz7MN6xSaAFADkDZJzMLbGSV25uKHAeXaxnPCwZomX="));
        assertEquals("Invalid character '=' at position 48", thrown.getMessage());
    }

    @Test
    @DisplayName(
            "should Throw Invalid Did Key Exception when Did Key Multicodec Value is not supported")
    void should_ThrowException_When_DidKey_Multicodec_Value_Is_Not_Supported() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDidKey(
                                        "did:key:z82Lm1MpAkeJcix9K8TMiLd5NMAhnwkjjCBeWHXyu3U4oT2MVJJKXkcVBgjGhnLBn2Kaau9"));
        assertEquals("did:key multicodec value is not supported", thrown.getMessage());
    }

    @Test
    @DisplayName("should Throw Invalid Did Key Exception when Public Key Length is not 33 Bytes")
    void should_ThrowException_When_PublicKey_Length_Is_Not_33Bytes() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDidKey(
                                        "did:key:z3u1stSg9rUT8ZygCoovJN9GSV7CQzETPPuPMG3D7b6RqsFK"));
        assertEquals("Expected key length equal to 33, but found 32 instead", thrown.getMessage());
    }

    @Test
    @DisplayName("Should Throw Invalid Did Key Exception when PublicKey Prefix is not 2Or3")
    void should_ThrowException_When_PublicKey_Prefix_Is_Not_2Or3() {
        InvalidDidKeyException thrown =
                assertThrows(
                        InvalidDidKeyException.class,
                        () ->
                                didKeyResolver.decodeDidKey(
                                        "did:key:zDnag8UdU6WNW8dXEXVMR8G2B8DoMSTbNt9ZYajQGE4v1dckd"));
        assertEquals(
                "Expected key prefix equal to 2 or 3, but found 7 instead", thrown.getMessage());
    }
}
