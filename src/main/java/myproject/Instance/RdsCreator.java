package myproject.Instance;


import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.rds.*;
import com.pulumi.aws.rds.inputs.ParameterGroupParameterArgs;

import java.util.List;
import java.util.Map;

public class RdsCreator {

    public static Instance createRDSInstance(ParameterGroup parameterGroup, SubnetGroup mySubnetGroup, SecurityGroup securityGroup) {
        return new Instance("default", InstanceArgs.builder()
                .identifier("csye6225")
                .allocatedStorage(20)
                .dbName("csye6225")
                .engine("mariadb")
                .engineVersion("10.11.4")
                .instanceClass("db.t3.micro")
                .parameterGroupName(parameterGroup.name())
                .password("Qweqweqwe!23")
                .skipFinalSnapshot(true)
                .username("csye6225")
                .dbSubnetGroupName(mySubnetGroup.id())
                .vpcSecurityGroupIds(securityGroup.id().applyValue(List::of))
                .multiAz(false)
                .tags(Map.of("Name", "csye6225"))
                .build());
    }

    public static ParameterGroup createRDSParameterGroup() {
        return new ParameterGroup("myparametergroup", ParameterGroupArgs.builder()
                .family("mariadb10.11")
                .parameters(
                        ParameterGroupParameterArgs.builder()
                                .name("character_set_server")
                                .value("utf8")
                                .build(),
                        ParameterGroupParameterArgs.builder()
                                .name("character_set_client")
                                .value("utf8")
                                .build())
                .build());

    }
}
