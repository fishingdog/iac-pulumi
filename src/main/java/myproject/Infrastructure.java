package myproject;

import com.pulumi.Context;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.sns.Topic;
import myproject.AWSCreators.AWSSecrets.SecretCreator;
import myproject.AWSCreators.LambdaFunction.LambdaFunctionCreator;
import myproject.AWSCreators.SNS.TopicCreator;

public class Infrastructure {
    public static void deploy(Context ctx) {

        AWS.runAWS(ctx);

        GCP.runGCP(ctx);

        Function lambda = LambdaFunctionCreator.createLambdaFunction(ctx);
        Topic topic = TopicCreator.createTopic(ctx);
        TopicCreator.subscribeTopicLambda(topic, lambda);
//        SecretCreator.creatSecret(ctx, "topicArn", topic.arn());

    }

}
