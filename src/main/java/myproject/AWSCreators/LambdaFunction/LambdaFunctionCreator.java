package myproject.AWSCreators.LambdaFunction;

import com.pulumi.Context;
import com.pulumi.asset.FileArchive;
import com.pulumi.aws.iam.*;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementPrincipalArgs;
import com.pulumi.aws.iam.outputs.GetPolicyDocumentResult;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.lambda.FunctionArgs;
import com.pulumi.aws.lambda.inputs.FunctionEnvironmentArgs;
import com.pulumi.core.Output;

import java.util.Map;

public class LambdaFunctionCreator {

    public static Function createLambdaFunction(Context ctx, Output<String> GCPServiceAccountKey, String mailgunAPIKey) {
        final var assumeRole = IamFunctions.getPolicyDocument(GetPolicyDocumentArgs.builder()
                .statements(GetPolicyDocumentStatementArgs.builder()
                        .effect("Allow")
                        .principals(GetPolicyDocumentStatementPrincipalArgs.builder()
                                .type("Service")
                                .identifiers("lambda.amazonaws.com")
                                .build())
                        .actions("sts:AssumeRole")
                        .build())
                .build());

        var iamForLambda = new Role("iamForLambda", RoleArgs.builder()
                .assumeRolePolicy(assumeRole.applyValue(GetPolicyDocumentResult::json))
                .build());

        // Create a policy for CloudWatch Logs
        var logPolicy = new Policy("lambdaLogPolicy", PolicyArgs.builder()
                .policy(
                        // JSON policy document granting necessary CloudWatch Logs permissions
                        Output.of("{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"logs:CreateLogGroup\",\"logs:CreateLogStream\",\"logs:PutLogEvents\"],\"Resource\":\"arn:aws:logs:*:*:*\"}]}")
                )
                .build());

// Attach the policy to the IAM role
        new RolePolicyAttachment("lambdaLogPolicyAttachment", RolePolicyAttachmentArgs.builder()
                .role(iamForLambda.name())
                .policyArn(logPolicy.arn())
                .build());


        var lambdaFunction = new Function("testLambda", FunctionArgs.builder()
                .name("testLambda")
                .environment(FunctionEnvironmentArgs.builder()
                        .variables(GCPServiceAccountKey.applyValue(key -> Map.of(
                                "GCP_SERVICE_ACCOUNT_KEY", key,
                                "MAILGUN_API", mailgunAPIKey
                        )))
                        .build())
                .code(new FileArchive("src/main/java/myproject/resources/Serverless-1.0.jar"))
                .role(iamForLambda.arn())
                .handler("Serverless.Serverless::handleRequest")
                .runtime("java17")
                .timeout(100)
                .build());

        ctx.export("lambdaFunctionName", lambdaFunction.name());

        return lambdaFunction;
    }


}
