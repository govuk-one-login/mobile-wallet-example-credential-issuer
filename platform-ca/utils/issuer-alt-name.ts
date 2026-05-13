import { AsnSerializer } from "@peculiar/asn1-schema";
import { GeneralName, GeneralNames, IssueAlternativeName } from "@peculiar/asn1-x509";
import { Buffer } from "buffer";
import { Command } from "commander";

const program = new Command();

function issuerAltName(program: Command) {
  program
    .name("issuer-alt-name")
    .description("CLI tool to generate a base64-encoded ASN.1 representation of an Issuer Alternative Name for an X.509v3 certificate extension")
    .version("0.0.1")
    .option("-e, --email <email>", "Email address to include in the Issuer Alternative Name")
    .option("-u, --uri <uri>", "URI to include in the Issuer Alternative Name")
    .action((opts, command) => {
      if (!opts.email && !opts.uri) {
        command.error("At least one of email address and URI must be supplied");
      }
    });

  program.parse();

  const generalNames: GeneralNames = [];

  if (program.opts().email) {
    generalNames.push(
      new GeneralName({
        rfc822Name: program.opts().email,
      }),
    );
  }

  if (program.opts().uri) {
    generalNames.push(
      new GeneralName({
        uniformResourceIdentifier: program.opts().uri,
      }),
    );
  }

  try {
    const asn1Structure = new IssueAlternativeName(generalNames);
    const serializedAsn1 = AsnSerializer.serialize(asn1Structure);
    const base64EncodedSerializedAsn1 = Buffer.from(serializedAsn1).toString("base64");
    console.log(base64EncodedSerializedAsn1);
  } catch (e) {
    console.error("An error occurred generating the Issuer Alternative Name:", e);
    process.exit(1);
  }
}

issuerAltName(program);
