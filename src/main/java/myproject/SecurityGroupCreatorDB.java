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
        SecurityGroup appSecurityGroup = new SecurityGroup("appSecurityGroup", SecurityGroupArgs.builder()
                .vpcId(myvpc.id())
                .description("security group for Database")
                .tags(Map.of("Name", "database_security_group"))
                .build());

        addIngressRules(appSecurityGroup);

        return appSecurityGroup;
    }

    private static void addIngressRules(SecurityGroup securityGroup) {

        // Allow mysql on 3306
        new SecurityGroupIngressRule("webIngress", SecurityGroupIngressRuleArgs.builder()
                .securityGroupId(securityGroup.id())
                .ipProtocol("tcp")
                .fromPort(3306)
                .toPort(3306)
                .cidrIpv4("0.0.0.0/0")
                .build());
    }

}


