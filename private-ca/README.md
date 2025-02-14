# private-ca Spike

# Generating the CSR and signing it with the KMS key

Generate a new temporary EC256 keypair:

```bash
openssl ecparam -genkey -name prime256v1 -noout -out ec256-key-pair.pem
```

Generate a skeleton CSR:

```bash
openssl req -new -key ec256-key-pair.pem -nodes -out test.csr
```

Run the script to replace with the KMS public key signed by the KMS private key

```bash
./aws-kms-sign-csr.py --keyid alias/private-ca-ddunford-doc-signing-key --profile mp-dev-admin --signalgo ECDSA test.csr
```

