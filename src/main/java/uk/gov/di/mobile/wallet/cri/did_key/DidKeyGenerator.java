package uk.gov.di.mobile.wallet.cri.did_key;

import org.bitcoinj.core.Base58;

import java.util.HexFormat;

public class DidKeyGenerator {
    public record DecodedData(
            Multicodec multicodecValue, byte[] rawPublicKeyBytes, String publicKeyBase64) {}

    public static String encodeDIDKey(Multicodec codec, byte[] publicKey) {
        byte[] multicodecHeader = HexFormat.of().parseHex(codec.uvarintcode);
        byte[] multicodec = new byte[multicodecHeader.length + publicKey.length];

        // Concatenate the header and byte array of the public key
        System.arraycopy(multicodecHeader, 0, multicodec, 0, multicodecHeader.length);
        System.arraycopy(publicKey, 0, multicodec, multicodecHeader.length, publicKey.length);

        String base58Encoded = Base58.encode(multicodec);

        return "did:key:z" + base58Encoded;
    }
}
