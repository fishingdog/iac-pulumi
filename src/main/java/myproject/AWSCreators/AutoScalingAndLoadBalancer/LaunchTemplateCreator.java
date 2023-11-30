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

//        Output<String> userData = Output.format("#!/bin/bash\n" +
//                "ENV_FILE=\"/opt/application.properties\"\n" +
//                "echo \"DATABASE_URL=jdbc:mysql://%s:3306/csye6225?useSSL=false\\&serverTimezone=UTC\\&allowPublicKeyRetrieval=true\" > ${ENV_FILE}\n" +
//                "echo \"DATABASE_USERNAME=csye6225\" >> ${ENV_FILE}\n" +
//                "echo \"DATABASE_PASSWORD=Qweqweqwe!23\" >> ${ENV_FILE}\n" +
//                "echo \"DB_DIALECT=org.hibernate.dialect.MariaDB103Dialect\" >> ${ENV_FILE}\n" +
//                "sudo chmod 644 ${ENV_FILE}\n" +
//                "sudo chmod 644 /opt/amazon-cloudwatch-agent.json\n", dbURL);

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

//        return new LaunchTemplate("myLaunchTemplate", LaunchTemplateArgs.builder()
//                .blockDeviceMappings(LaunchTemplateBlockDeviceMappingArgs.builder()
//                        .deviceName("/dev/sdf")
//                        .ebs(LaunchTemplateBlockDeviceMappingEbsArgs.builder()
//                                .volumeSize(25)
//                                .build())
//                        .build())
//
////                .capacityReservationSpecification(LaunchTemplateCapacityReservationSpecificationArgs.builder()
////                        .capacityReservationPreference("open")
////                        .build())
////                .disableApiStop(true)
////                .disableApiTermination(true)
//                .ebsOptimized("true")
////                .elasticGpuSpecifications(LaunchTemplateElasticGpuSpecificationArgs.builder()
////                        .type("test")
////                        .build())
////                .elasticInferenceAccelerator(LaunchTemplateElasticInferenceAcceleratorArgs.builder()
////                        .type("eia1.medium")
////                        .build())
//                .iamInstanceProfile(LaunchTemplateIamInstanceProfileArgs.builder()
//                        .name(myRoleInstanceProfile.name())
//                        .build())
//                .imageId(ami)
//                .instanceInitiatedShutdownBehavior("terminate")
////                .instanceMarketOptions(LaunchTemplateInstanceMarketOptionsArgs.builder()
////                        .marketType("spot")
////                        .build())
//                .instanceType("t2.micro")
////                .kernelId("test")
//                .keyName(keyName)
////                .licenseSpecifications(LaunchTemplateLicenseSpecificationArgs.builder()
////                        .licenseConfigurationArn("arn:aws:license-manager:eu-west-1:123456789012:license-configuration:lic-0123456789abcdef0123456789abcdef")
////                        .build())
//                .metadataOptions(LaunchTemplateMetadataOptionsArgs.builder()
//                        .httpEndpoint("enabled")
//                        .httpTokens("required")
//                        .httpPutResponseHopLimit(1)
//                        .instanceMetadataTags("enabled")
//                        .build())
//                .monitoring(LaunchTemplateMonitoringArgs.builder()
//                        .enabled(true)
//                        .build())
////                .networkInterfaces(LaunchTemplateNetworkInterfaceArgs.builder()
////                        .associatePublicIpAddress("true")
////                        .build())
////                .placement(LaunchTemplatePlacementArgs.builder()
////                        .availabilityZone("us-west-2a")
////                        .build())
////                .ramDiskId("test")
//                .vpcSecurityGroupIds(securityGroup.id().applyValue(List::of))
//                .tagSpecifications(LaunchTemplateTagSpecificationArgs.builder()
//                        .resourceType("instance")
//                        .tags(Map.of("Name", "test"))
//                        .build())
//                .userData(userData.applyValue(i -> {
//                    return Base64.getEncoder().encodeToString(i.getBytes());
//                }))
//                .build());
    }
}
