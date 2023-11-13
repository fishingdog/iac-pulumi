package myproject.SecurityGroup;

import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.vpc.SecurityGroupEgressRule;
import com.pulumi.aws.vpc.SecurityGroupEgressRuleArgs;
import com.pulumi.aws.vpc.SecurityGroupIngressRule;
import com.pulumi.aws.vpc.SecurityGroupIngressRuleArgs;

import java.util.Map;

public class SecurityGroupCreatorLoadBalancer {

    public static SecurityGroup createLoadBalancerSecurityGroup(Vpc myvpc) {
        SecurityGroup appSecurityGroup = new SecurityGroup("lBSecurityGroup", SecurityGroupArgs.builder()
                .vpcId(myvpc.id())
                .description("Security group for Load Balancer")
                .tags(Map.of("Name", "load_balancer_security_group"))
                .build());

        addIngressRules(appSecurityGroup);
        addEgressRules(appSecurityGroup);
        return appSecurityGroup;
    }

    private static void addIngressRules(SecurityGroup securityGroup) {

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
    }

    private static void addEgressRules(SecurityGroup securityGroup) {
        // Example egress rule to allow all outbound traffic
        new SecurityGroupEgressRule("loadBalancerOutbound", SecurityGroupEgressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("-1") // Represents all protocols
                .fromPort(0)
                .toPort(65535)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }
}
