package myproject.AWSCreators.AutoScalingAndLoadBalancer;

import com.pulumi.aws.ec2.LaunchTemplate;
import com.pulumi.aws.ec2.LaunchTemplateArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.inputs.*;
import com.pulumi.aws.iam.InstanceProfile;
import com.pulumi.aws.iam.InstanceProfileArgs;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.sns.Topic;
import com.pulumi.core.Output;

import java.util.Base64;
import java.util.List;

public class LaunchTemplateCreator {

    public static LaunchTemplate createLaunchTemplate(String ami, String keyName, com.pulumi.aws.rds.Instance rdsInstance, Role myRole, SecurityGroup securityGroup, Topic topic) {
        Output<String> dbURL = rdsInstance.address();
        Output<String> topicArn = topic.arn();

        Output<String> userData = Output.all(dbURL, topicArn).applyValue(values -> {
            String databaseUrl = values.get(0);
            String snsTopicArn = values.get(1);
            // Generate the user data script
            return "#!/bin/bash\n" +
                    "ENV_FILE=\"/opt/application.properties\"\n" +
                    "echo \"DATABASE_URL=jdbc:mysql://" + databaseUrl + ":3306/csye6225?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true\" > ${ENV_FILE}\n" +
                    "echo \"DATABASE_USERNAME=csye6225\" >> ${ENV_FILE}\n" +
                    "echo \"DATABASE_PASSWORD=Qweqweqwe!23\" >> ${ENV_FILE}\n" +
                    "echo \"DB_DIALECT=org.hibernate.dialect.MariaDB103Dialect\" >> ${ENV_FILE}\n" +
                    "echo \"TOPIC_ARN=" + snsTopicArn + "\" >> ${ENV_FILE}\n" +
                    "sudo chmod 644 ${ENV_FILE}\n" +
                    "sudo chmod 644 /opt/amazon-cloudwatch-agent.json\n";
        });



        InstanceProfile myRoleInstanceProfile = new InstanceProfile("myRoleInstanceProfile", InstanceProfileArgs.builder()
                .role(myRole.name())
                .build());

        return new LaunchTemplate("myLaunchTemplate", LaunchTemplateArgs.builder()
                .name("myLaunchTemplate")
                .imageId(ami)
                .iamInstanceProfile(LaunchTemplateIamInstanceProfileArgs.builder()
                      .name(myRoleInstanceProfile.name())
                      .build())
                .instanceType("t2.micro")
                .keyName(keyName)
                .vpcSecurityGroupIds(securityGroup.id().applyValue(List::of))
                .userData(userData.applyValue(i -> {
                    return Base64.getEncoder().encodeToString(i.getBytes());
                    }))
                .build());

    }
}
