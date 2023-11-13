package myproject.SecurityGroup;

import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.vpc.SecurityGroupEgressRule;
import com.pulumi.aws.vpc.SecurityGroupEgressRuleArgs;
import com.pulumi.aws.vpc.SecurityGroupIngressRule;
import com.pulumi.aws.vpc.SecurityGroupIngressRuleArgs;


import java.util.Map;
public class SecurityGroupCreatorEC2 {

    public static SecurityGroup createApplicationSecurityGroup(Vpc myvpc, SecurityGroup loadBalancerSecurityGroup) {
        SecurityGroup appSecurityGroup = new SecurityGroup("appSecurityGroup", SecurityGroupArgs.builder()
                .vpcId(myvpc.id())
                .description("app security group for EC2 instances")
                .tags(Map.of("Name", "app_security_group"))
                .build());

        addIngressRules(appSecurityGroup, loadBalancerSecurityGroup);
        addEgressRules(appSecurityGroup);
        return loadBalancerSecurityGroup;
    }

    private static void addIngressRules(SecurityGroup securityGroup, SecurityGroup loadBalancerSecurityGroup) {
        // Allow SSH
        new SecurityGroupIngressRule("sshIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(22)
                .toPort(22)
                .cidrIpv4("0.0.0.0/0")
                .build());

        // Allow webapp on 8080
        new SecurityGroupIngressRule("webIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(8080)
                .toPort(8080)
                .referencedSecurityGroupId(loadBalancerSecurityGroup.id())
                .build());
    }

    private static void addEgressRules(SecurityGroup securityGroup) {
        // Example egress rule to allow all outbound traffic
        new SecurityGroupEgressRule("ec2Outbound", SecurityGroupEgressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("-1") // Represents all protocols
                .fromPort(0)
                .toPort(65535)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }
}


