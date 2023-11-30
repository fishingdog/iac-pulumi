package myproject.AWSCreators.Instance;


import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.InstanceRootBlockDeviceArgs;
import com.pulumi.aws.iam.InstanceProfile;
import com.pulumi.aws.iam.InstanceProfileArgs;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.sns.Topic;
import com.pulumi.core.Output;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class CreateEC2Instance {

    public static Instance createEC2Instance(SecurityGroup sg, String ami, Subnet mysubnet, String keyName, com.pulumi.aws.rds.Instance rdsInstance, Role myRole, Topic topic) {
        Output<String> dbURL = rdsInstance.address();
        Output<String> topicArn = topic.arn();
//        Output<String> userData = Output.format("#!/bin/bash\n" +
//                "ENV_FILE=\"/opt/application.properties\"\n" +
//                "echo \"DATABASE_URL=jdbc:mysql://%s:3306/csye6225?useSSL=false\\&serverTimezone=UTC\\&allowPublicKeyRetrieval=true\" > ${ENV_FILE}\n" +
//                "echo \"DATABASE_USERNAME=csye6225\" >> ${ENV_FILE}\n" +
//                "echo \"DATABASE_PASSWORD=Qweqweqwe!23\" >> ${ENV_FILE}\n" +
//                "echo \"DB_DIALECT=org.hibernate.dialect.MariaDB103Dialect\" >> ${ENV_FILE}\n" +
//                "sudo chmod 644 ${ENV_FILE}\n", dbURL);
        Output<String> userData = Output.tuple(dbURL, topicArn).applyValue(t -> String.format(
                String.join(
                        "#!/bin/bash\n" +
                                "ENV_FILE=\"/opt/application.properties\"\n" +
                                "echo \"DATABASE_URL=jdbc:mysql://%s:3306/csye6225?useSSL=false\\&serverTimezone=UTC\\&allowPublicKeyRetrieval=true\" > ${ENV_FILE}\n" +
                                "echo \"DATABASE_USERNAME=csye6225\" >> ${ENV_FILE}\n" +
                                "echo \"DATABASE_PASSWORD=Qweqweqwe!23\" >> ${ENV_FILE}\n" +
                                "echo \"DB_DIALECT=org.hibernate.dialect.MariaDB103Dialect\" >> ${ENV_FILE}\n" +
                                "echo \"TOPIC_ARN=%s\" >> ${ENV_FILE}\n" +
                                "sudo chmod 644 ${ENV_FILE}\n"
                ),t.t1, t.t2
        ));


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
//                .userData(userData.applyValue(i -> {
//                    return Base64.getEncoder().encodeToString(i.getBytes());
//                }))
                .userData(userData)
                .iamInstanceProfile(myRoleInstanceProfile.name())
                .tags(Map.of("Name", "MyEC2Instance"))
                .build());
    }
}
