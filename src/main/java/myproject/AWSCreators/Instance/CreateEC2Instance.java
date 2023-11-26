package myproject.AWSCreators.Instance;


import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.InstanceRootBlockDeviceArgs;
import com.pulumi.aws.iam.InstanceProfile;
import com.pulumi.aws.iam.InstanceProfileArgs;
import com.pulumi.aws.iam.Role;
import com.pulumi.core.Output;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class CreateEC2Instance {

    public static Instance createEC2Instance(SecurityGroup sg, String ami, Subnet mysubnet, String keyName, com.pulumi.aws.rds.Instance rdsInstance, Role myRole) {
        Output<String> dbURL = rdsInstance.address();
        Output<String> userData = Output.format("#!/bin/bash\n" +
                "ENV_FILE=\"/opt/application.properties\"\n" +
                "echo \"DATABASE_URL=jdbc:mysql://%s:3306/csye6225?useSSL=false\\&serverTimezone=UTC\\&allowPublicKeyRetrieval=true\" > ${ENV_FILE}\n" +
                "echo \"DATABASE_USERNAME=csye6225\" >> ${ENV_FILE}\n" +
                "echo \"DATABASE_PASSWORD=Qweqweqwe!23\" >> ${ENV_FILE}\n" +
                "echo \"DB_DIALECT=org.hibernate.dialect.MariaDB103Dialect\" >> ${ENV_FILE}\n" +
                "sudo chmod 644 ${ENV_FILE}\n"+
                "cat <<EOT >> /opt/amazon-cloudwatch-agent.json\n" +
                "{\n" +
                "  \"agent\": {\n" +
                "    \"metrics_collection_interval\": 5,\n" +
                "    \"logfile\": \"/var/logs/amazon-cloudwatch-agent.log\"\n" +
                "  },\n" +
                "  \"logs\": {\n" +
                "    \"logs_collected\": {\n" +
                "      \"files\": {\n" +
                "        \"collect_list\": [\n" +
                "          {\n" +
                "            \"file_path\": \"/opt/webapp.log\",\n" +
                "            \"log_group_name\": \"csye6225\",\n" +
                "            \"log_stream_name\": \"webapp\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    \"log_stream_name\": \"cloudwatch_log_stream\"\n" +
                "  },\n" +
                "  \"metrics\":{\n" +
                "    \"metrics_collected\":{\n" +
                "      \"statsd\":{\n" +
                "        \"service_address\":\":8125\",\n" +
                "        \"metrics_collection_interval\":5,\n" +
                "        \"metrics_aggregation_interval\":60\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "EOT\n" +
                "sudo chmod 644 /opt/amazon-cloudwatch-agent.json\n", dbURL);

        InstanceProfile myRoleInstanceProfile = new InstanceProfile("myRoleInstanceProfile", InstanceProfileArgs.builder()
                .role(myRole.name())
                .build());

        return new Instance("MyEc2Instance", InstanceArgs.builder()
                .ami(ami)
                .instanceType("t2.micro")
                .subnetId(mysubnet.id())
                .vpcSecurityGroupIds(sg.id().applyValue(List::of))
                .keyName(keyName)
                .rootBlockDevice(InstanceRootBlockDeviceArgs.builder()
                        .volumeType("gp2")
                        .volumeSize(25)
                        .deleteOnTermination(true)
                        .build())
                .userData(userData.applyValue(i -> {
                    return Base64.getEncoder().encodeToString(i.getBytes());
                }))
                .iamInstanceProfile(myRoleInstanceProfile.name())
                .tags(Map.of("Name", "MyEC2Instance"))
                .build());
    }
}
