package uk.gov.di.mobile.wallet.cri.did_key;

import org.bitcoinj.core.Base58;

import java.util.HexFormat;

public class DidKeyGenerator {
    public record DecodedData(
            Multicodec multicodecValue, byte[] rawPublicKeyBytes, String publicKeyBase64) {}

    public static String encodeDIDKey(Multicodec codec, byte[] publicKey) {
        // get the multicodec (i.e. a prefix with the unsigned varint of the algorithm type)
        byte[] multicodec = HexFormat.of().parseHex(codec.uvarintcode);
        // create a new byte array that "fits" the multicodec + compressed public key
        byte[] buffer = new byte[multicodec.length + publicKey.length];

        // concatenate the multicodec and the compressed public key
        System.arraycopy(multicodec, 0, buffer, 0, multicodec.length);
        System.arraycopy(publicKey, 0, buffer, multicodec.length, publicKey.length);

        // base58 encode the buffer
        String base58Encoded = Base58.encode(buffer);
        String base58Encoded2 = Base58Utils.encode(buffer);
        System.out.println(base58Encoded);
        System.out.println(base58Encoded2);

        // prefix with `z` to indicate multi-base base58btc encoding
        return "did:key:z" + base58Encoded;
    }
}
