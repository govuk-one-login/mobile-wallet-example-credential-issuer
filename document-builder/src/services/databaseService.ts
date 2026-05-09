import { getDatabaseConfig } from "../config/aws";
import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import {
  PutCommand,
  DynamoDBDocumentClient,
  GetCommand,
} from "@aws-sdk/lib-dynamodb";
import { logger } from "../middleware/logger";
import { TableItem } from "../types/TableItem";

const dynamoDbClient = new DynamoDBClient(getDatabaseConfig());
const documentClient = DynamoDBDocumentClient.from(dynamoDbClient, {
  marshallOptions: {
    removeUndefinedValues: true,
  },
});

export async function saveDocument(
  tableName: string,
  item: TableItem,
): Promise<void> {
  const command = new PutCommand({
    TableName: tableName,
    Item: item,
  });

  await documentClient.send(command);
}

export async function getDocument(
  tableName: string,
  itemId: string,
): Promise<TableItem | undefined> {
  const command = new GetCommand({
    TableName: tableName,
    Key: {
      itemId,
    },
  });

  const response = await documentClient.send(command);

  const item = response.Item;
  if (!item) {
    logger.error(`Item with ID ${itemId} not found`);
    return undefined;
  }
  return item as TableItem;
}
