package myproject.AWSCreators.LambdaFunction;

import com.pulumi.Context;
import com.pulumi.asset.FileArchive;
import com.pulumi.aws.iam.IamFunctions;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.iam.RoleArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementPrincipalArgs;
import com.pulumi.aws.iam.outputs.GetPolicyDocumentResult;
import com.pulumi.aws.lambda.Function;
import com.pulumi.aws.lambda.FunctionArgs;
import com.pulumi.aws.lambda.inputs.FunctionEnvironmentArgs;

import java.util.Map;

public class LambdaFunctionCreator {

    public static Function createLambdaFunction(Context ctx) {
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

//        final var lambda = ArchiveFunctions.getFile(GetFileArgs.builder()
//                .type("zip")
//                .sourceFile("lambda.js")
//                .outputPath("lambda_function_payload.zip")
//                .build());

        var lambdaFunction = new Function("testLambda", FunctionArgs.builder()
                .name("testLambda")
                .code(new FileArchive("src/main/java/myproject/resources/Serverless-1.0.jar"))
                .role(iamForLambda.arn())
                .handler("Serverless.Serverless::handleRequest")
                .runtime("java17")
//                .environment(FunctionEnvironmentArgs.builder()
//                        .variables(Map.of("foo", "bar"))
//                        .build())
                .build());

        ctx.export("lambdaFunctionName", lambdaFunction.name());

        return lambdaFunction;
    }


}
