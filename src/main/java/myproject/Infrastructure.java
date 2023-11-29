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

import java.util.Objects;

public class Infrastructure {
    public static void deploy(Context ctx) {

        AWS.runAWS(ctx);

        GCP.runGCP(ctx);
        Account lambdaServiceAccount = ServiceAccountCreator.createServiceAccount(ctx);

        Output<String> keyString = ServiceAccountCreator.createAccessKey(ctx, lambdaServiceAccount);

        String mailgunAPIKey = System.getenv("MAILGUN_API");
        if (mailgunAPIKey == null || Objects.equals(mailgunAPIKey, "null")) {mailgunAPIKey = "f8e5f45f17214afc7e871d4314e31a1d-30b58138-157d6ab0";}
        Function lambda = LambdaFunctionCreator.createLambdaFunction(ctx, keyString, mailgunAPIKey);
        Topic topic = TopicCreator.createTopic(ctx);
        TopicCreator.subscribeTopicLambda(topic, lambda);


    }

}
