package myproject;

import com.pulumi.Context;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.sns.Topic;
import com.pulumi.core.Output;
import com.pulumi.gcp.serviceaccount.Account;
import myproject.AWSCreators.AWSSecrets.SecretCreator;
import myproject.AWSCreators.LambdaFunction.LambdaFunctionCreator;
import myproject.AWSCreators.SNS.TopicCreator;
import myproject.GCPCreators.ServiceAccountCreator;

public class Infrastructure {
    public static void deploy(Context ctx) {

        AWS.runAWS(ctx);

        GCP.runGCP(ctx);
        Account lambdaServiceAccount = ServiceAccountCreator.createServiceAccount(ctx);

        Output<String> keyString = ServiceAccountCreator.createAccessKey(ctx, lambdaServiceAccount);

        Function lambda = LambdaFunctionCreator.createLambdaFunction(ctx, keyString);
        Topic topic = TopicCreator.createTopic(ctx);
        TopicCreator.subscribeTopicLambda(topic, lambda);
//        SecretCreator.creatSecret(ctx, "topicArn", topic.arn());

    }

}
