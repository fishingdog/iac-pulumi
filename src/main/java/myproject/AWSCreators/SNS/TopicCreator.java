package myproject.AWSCreators.SNS;

import com.pulumi.Context;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.lambda.LambdaFunctions;
import com.pulumi.aws.lambda.Permission;
import com.pulumi.aws.lambda.PermissionArgs;
import com.pulumi.aws.sns.Topic;
import com.pulumi.aws.sns.TopicArgs;
import com.pulumi.aws.sns.TopicSubscription;
import com.pulumi.aws.sns.TopicSubscriptionArgs;
import com.pulumi.resources.CustomResourceOptions;

public class TopicCreator {
    public static Topic createTopic(Context ctx) {
        var topic = new Topic("userUpdates", TopicArgs.builder()
                .name("lambdaTopic")
                .build());

        ctx.export("TopicName", topic.name());
        return topic;
    }

    public static TopicSubscription subscribeTopicLambda(Topic topic, Function lambda) {

        Permission permission = new Permission("sns-topicLambdaPermission", PermissionArgs.builder()
                .action("lambda:InvokeFunction")
                .function(lambda.arn())
                .principal("sns.amazonaws.com")
                .sourceArn(topic.arn())
                .build());

        return new TopicSubscription("sns-topicTopicSubscription", TopicSubscriptionArgs.builder()
                .topic(topic.arn())
                .protocol("lambda")
                .endpoint(lambda.arn())
                .build());
    }

}
