package myproject;

import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.aws.ec2.outputs.SecurityGroupIngress;
import com.pulumi.aws.vpc.SecurityGroupEgressRule;
import com.pulumi.aws.vpc.SecurityGroupEgressRuleArgs;
import com.pulumi.aws.vpc.SecurityGroupIngressRule;
import com.pulumi.aws.vpc.SecurityGroupIngressRuleArgs;


import java.util.Map;
public class SecurityGroupCreator {

    public static SecurityGroup createApplicationSecurityGroup(Vpc myvpc) {
        SecurityGroup appSecurityGroup = new SecurityGroup("appSecurityGroup", SecurityGroupArgs.builder()
                .vpcId(myvpc.id())
                .description("Application security group for EC2 instances")
                .tags(Map.of("Name", "application_security_group"))
                .build());

        addIngressRules(appSecurityGroup);

        return appSecurityGroup;
    }

    private static void addIngressRules(SecurityGroup securityGroup) {
        // Allow SSH
        new SecurityGroupIngressRule("sshIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(22)
                .toPort(22)
                .cidrIpv4("0.0.0.0/0")
                .build());

        // Allow HTTP
        new SecurityGroupIngressRule("httpIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(80)
                .toPort(80)
                .cidrIpv4("0.0.0.0/0")
                .build());

        // Allow HTTPS
        new SecurityGroupIngressRule("httpsIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(443)
                .toPort(443)
                .cidrIpv4("0.0.0.0/0")
                .build());

        // Allow webapp on 8080
        new SecurityGroupIngressRule("webIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(8080)
                .toPort(8080)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }

    private static void addEgressRules(SecurityGroup securityGroup) {
        // Example egress rule to allow all outbound traffic
        new SecurityGroupEgressRule("allOutbound", SecurityGroupEgressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("-1") // Represents all protocols
                .fromPort(0)
                .toPort(65535)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }
}


