package myproject;

import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.vpc.SecurityGroupEgressRule;
import com.pulumi.aws.vpc.SecurityGroupEgressRuleArgs;
import com.pulumi.aws.vpc.SecurityGroupIngressRule;
import com.pulumi.aws.vpc.SecurityGroupIngressRuleArgs;


import java.util.Map;
public class SecurityGroupCreatorDB {

    public static SecurityGroup createDatabaseSecurityGroup(Vpc myvpc) {
        SecurityGroup dbSecurityGroup = new SecurityGroup("dbSecurityGroup", SecurityGroupArgs.builder()
                .vpcId(myvpc.id())
                .description("security group for Database")
                .tags(Map.of("Name", "database_security_group"))
                .build());

        addIngressRules(dbSecurityGroup);
        addEgressRules(dbSecurityGroup);
        return dbSecurityGroup;
    }

    private static void addIngressRules(SecurityGroup securityGroup) {

        // Allow mysql on 3306
        new SecurityGroupIngressRule("DBIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(3306)
                .toPort(3306)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }

    private static void addEgressRules(SecurityGroup securityGroup) {
        // Example egress rule to allow all outbound traffic
        new SecurityGroupEgressRule("dbOutbound", SecurityGroupEgressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("-1") // Represents all protocols
                .fromPort(0)
                .toPort(65535)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }

}


