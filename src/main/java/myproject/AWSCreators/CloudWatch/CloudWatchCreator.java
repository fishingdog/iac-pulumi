package myproject.AWSCreators.CloudWatch;

import com.pulumi.aws.cloudwatch.LogGroup;
import com.pulumi.aws.cloudwatch.LogStream;
import com.pulumi.aws.cloudwatch.LogStreamArgs;

public class CloudWatchCreator {

    public static void logGroupCreator() {
        var logGroup = new LogGroup("csye6225");

        var foo = new LogStream("webapp", LogStreamArgs.builder()
                .logGroupName(logGroup.name())
                .build());

    }

}
