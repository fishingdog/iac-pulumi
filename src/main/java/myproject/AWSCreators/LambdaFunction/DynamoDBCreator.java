package myproject.AWSCreators.LambdaFunction;

import com.pulumi.Context;
import com.pulumi.aws.dynamodb.Table;
import com.pulumi.aws.dynamodb.TableArgs;
import com.pulumi.aws.dynamodb.inputs.TableAttributeArgs;

public class DynamoDBCreator {

    public static Table createDynamoDB(Context ctx, String tableName) {
        Table dynamoDBTable = new Table(tableName, TableArgs.builder()
                .attributes(
                        TableAttributeArgs.builder()
                                .name("emailId")
                                .type("S")
                                .build())
                .hashKey("emailId")
                .readCapacity(5)
                .writeCapacity(5)
                .build());

        // Export the name of the table
        ctx.export("tableName", dynamoDBTable.name());

        return dynamoDBTable;
    }

}
