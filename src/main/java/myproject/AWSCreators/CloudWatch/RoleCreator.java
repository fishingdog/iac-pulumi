package myproject.AWSCreators.CloudWatch;

import com.pulumi.aws.iam.*;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementArgs;
import com.pulumi.aws.iam.inputs.GetPolicyDocumentStatementPrincipalArgs;
import com.pulumi.aws.iam.outputs.GetPolicyDocumentResult;

public class RoleCreator {

    public static Role createRole() {
        final var instanceAssumeRolePolicy = IamFunctions.getPolicyDocument(GetPolicyDocumentArgs.builder()
                .statements(GetPolicyDocumentStatementArgs.builder()
                        .actions("sts:AssumeRole")
                        .principals(GetPolicyDocumentStatementPrincipalArgs.builder()
                                .type("Service")
                                .identifiers("ec2.amazonaws.com")
                                .build())
                        .build())
                .build());

        Role cloudWatchRole = new Role("instance", RoleArgs.builder()
                .assumeRolePolicy(instanceAssumeRolePolicy.applyValue(GetPolicyDocumentResult::json))
                .path("/")
                .name("cloudWatchRole")
                .build());

        attachRolePolicy(cloudWatchRole, "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy", "cloudWatchAgentPolicyAttachment");
        attachRolePolicy(cloudWatchRole, "arn:aws:iam::aws:policy/AmazonSNSFullAccess", "SNSPolicyAttachment");

        return cloudWatchRole;
    }

    public static RolePolicyAttachment attachRolePolicy(Role role, String porlicyArn, String name) {
        return new RolePolicyAttachment(name,
                new RolePolicyAttachmentArgs.Builder()
                        .role(role.name())
                        .policyArn(porlicyArn)
                        .build());
    }

}
