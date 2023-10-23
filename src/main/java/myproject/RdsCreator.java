package myproject;


import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.rds.*;
import com.pulumi.aws.rds.inputs.ParameterGroupParameterArgs;

import java.util.List;
import java.util.Map;

public class RdsCreator {

    public static Instance createRDSInstance(ParameterGroup parameterGroup, SubnetGroup mySubnetGroup, SecurityGroup securityGroup) {
        return new Instance("default", InstanceArgs.builder()
                .allocatedStorage(20)
                .dbName("csye6225")
                .engine("mysql")
                .engineVersion("8.0.34")
                .instanceClass("db.t3.micro")
                .parameterGroupName(parameterGroup.name())
                .password("Qweqweqwe!23")
                .skipFinalSnapshot(true)
                .username("csye6225")
                .dbSubnetGroupName(mySubnetGroup.id())
                .vpcSecurityGroupIds(securityGroup.id().applyValue(List::of))
                .build());
    }

    public static ParameterGroup createRDSParameterGroup() {
        return new ParameterGroup("default", ParameterGroupArgs.builder()
                .family("mysql8.0.34")
                .parameters(
                        ParameterGroupParameterArgs.builder()
                                .name("csye6225_pg")
                                .value("utf8")
                                .build())
                .tags(Map.of("Name", "csye6225_pg"))
                .build());

    }
}
