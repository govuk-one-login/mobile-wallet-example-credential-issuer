process.env.ENVIRONMENT = "local";
import { mockClient } from "aws-sdk-client-mock";
import {
  PutCommand,
  DynamoDBDocumentClient,
  GetCommand,
} from "@aws-sdk/lib-dynamodb";
import { saveDocument, getDocument } from "../../src/services/databaseService";
import "aws-sdk-client-mock-jest";
import { UUID } from "node:crypto";
import { CredentialType } from "../../src/types/CredentialType";

describe("databaseService.ts", () => {
  const item = {
    itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46" as UUID,
    documentId: "QQ123456A",
    data: {
      title: "Ms",
      givenName: "Rose",
      familyName: "Andrews",
      nino: "QQ123456A",
      credentialTtlSeconds: 43200,
    },
    vcType: "SocialSecurityCredential" as CredentialType,
    timeToLive: 1760174135,
    credentialTtlSeconds: 43200,
  };

  it("should save a document to the database table", async () => {
    const putItemCommand = {
      TableName: "testTable",
      Item: item,
    };
    const dynamoDbMock = mockClient(DynamoDBDocumentClient);
    dynamoDbMock.on(PutCommand).resolvesOnce({
      $metadata: {
        httpStatusCode: 200,
      },
    });

    await expect(saveDocument("testTable", item)).resolves.not.toThrow();
    expect(dynamoDbMock).toHaveReceivedCommandWith(PutCommand, putItemCommand);
  });

  it("should save a document with undefined values in nested objects", async () => {
    const itemWithUndefined = {
      ...item,
      data: {
        ...item.data,
        codes: undefined,
        nested: {
          value: "test",
          undefinedField: undefined,
        },
      },
    };
    const dynamoDbMock = mockClient(DynamoDBDocumentClient);
    dynamoDbMock.on(PutCommand).resolvesOnce({
      $metadata: {
        httpStatusCode: 200,
      },
    });

    await expect(
      saveDocument("testTable", itemWithUndefined),
    ).resolves.not.toThrow();
    expect(dynamoDbMock).toHaveReceivedCommand(PutCommand);
  });

  it("should throw the error thrown by the DynamoDB client when trying to save a document", async () => {
    const putItemCommand = {
      TableName: "testTable",
      Item: item,
    };
    const dynamoDbMock = mockClient(DynamoDBDocumentClient);
    dynamoDbMock.on(PutCommand).rejectsOnce("SOME_DATABASE_ERROR");

    await expect(saveDocument("testTable", item)).rejects.toThrow(
      "SOME_DATABASE_ERROR",
    );
    expect(dynamoDbMock).toHaveReceivedCommandWith(PutCommand, putItemCommand);
  });

  it("should get a document from the database table by ID and return it", async () => {
    const getCommandInput = {
      TableName: "testTable",
      Key: {
        itemId: "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
      },
    };
    const databaseMockClient = mockClient(DynamoDBDocumentClient);
    databaseMockClient.on(GetCommand).resolvesOnce({
      $metadata: {
        httpStatusCode: 200,
      },
      Item: item,
    });

    const response = await getDocument(
      "testTable",
      "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
    );

    expect(response).toEqual(item);
    expect(databaseMockClient).toHaveReceivedCommandWith(
      GetCommand,
      getCommandInput,
    );
  });

  it("should return 'undefined' if the document does not exist", async () => {
    const databaseMockClient = mockClient(DynamoDBDocumentClient);
    databaseMockClient.on(GetCommand).resolvesOnce({
      $metadata: {
        httpStatusCode: 200,
      },
    });

    const response = await getDocument(
      "testTable",
      "2e0fac05-4b38-480f-9cbd-b046eabe1e46",
    );

    expect(response).toEqual(undefined);
  });

  it("should throw the error thrown by the DyanamoDB client when trying to get a document", async () => {
    const databaseMockClient = mockClient(DynamoDBDocumentClient);
    databaseMockClient.on(GetCommand).rejectsOnce("SOME_ERROR");

    await expect(
      getDocument("testTable", "2e0fac05-4b38-480f-9cbd-b046eabe1e46"),
    ).rejects.toThrow("SOME_ERROR");
  });
});
