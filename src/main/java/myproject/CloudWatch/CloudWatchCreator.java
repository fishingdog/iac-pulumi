package myproject.CloudWatch;

import com.pulumi.Context;
import com.pulumi.aws.cloudwatch.LogGroup;
import com.pulumi.aws.cloudwatch.LogGroupArgs;
import com.pulumi.aws.cloudwatch.LogStream;
import com.pulumi.aws.cloudwatch.LogStreamArgs;

import java.util.Map;

public class CloudWatchCreator {

    public static void logGroupCreator() {
        var logGroup = new LogGroup("csye6225");

        var foo = new LogStream("webapp", LogStreamArgs.builder()
                .logGroupName(logGroup.name())
                .build());

    }

}
