package myproject.AutoScalingAndLoadBalancer;

import com.pulumi.aws.ec2.LaunchTemplate;
import com.pulumi.aws.ec2.LaunchTemplateArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.inputs.*;
import com.pulumi.aws.iam.Role;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;

public class LaunchTemplateCreator {

    public static LaunchTemplate createLaunchTemplate(String ami, String keyName, com.pulumi.aws.rds.Instance rdsInstance, Role myRole, SecurityGroup securityGroup) {
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


        return new LaunchTemplate("foo", LaunchTemplateArgs.builder()
                .blockDeviceMappings(LaunchTemplateBlockDeviceMappingArgs.builder()
                        .deviceName("/dev/sdf")
                        .ebs(LaunchTemplateBlockDeviceMappingEbsArgs.builder()
                                .volumeSize(20)
                                .build())
                        .build())
                .capacityReservationSpecification(LaunchTemplateCapacityReservationSpecificationArgs.builder()
                        .capacityReservationPreference("open")
                        .build())
                .cpuOptions(LaunchTemplateCpuOptionsArgs.builder()
                        .coreCount(4)
                        .threadsPerCore(2)
                        .build())
                .creditSpecification(LaunchTemplateCreditSpecificationArgs.builder()
                        .cpuCredits("standard")
                        .build())
                .disableApiStop(true)
                .disableApiTermination(true)
                .ebsOptimized("true")
                .elasticGpuSpecifications(LaunchTemplateElasticGpuSpecificationArgs.builder()
                        .type("test")
                        .build())
                .elasticInferenceAccelerator(LaunchTemplateElasticInferenceAcceleratorArgs.builder()
                        .type("eia1.medium")
                        .build())
                .iamInstanceProfile(LaunchTemplateIamInstanceProfileArgs.builder()
                        .name(myRole.name())
                        .build())
                .imageId(ami)
                .instanceInitiatedShutdownBehavior("terminate")
                .instanceMarketOptions(LaunchTemplateInstanceMarketOptionsArgs.builder()
                        .marketType("spot")
                        .build())
                .instanceType("t2.micro")
                .kernelId("test")
                .keyName(keyName)
                .licenseSpecifications(LaunchTemplateLicenseSpecificationArgs.builder()
                        .licenseConfigurationArn("arn:aws:license-manager:eu-west-1:123456789012:license-configuration:lic-0123456789abcdef0123456789abcdef")
                        .build())
                .metadataOptions(LaunchTemplateMetadataOptionsArgs.builder()
                        .httpEndpoint("enabled")
                        .httpTokens("required")
                        .httpPutResponseHopLimit(1)
                        .instanceMetadataTags("enabled")
                        .build())
                .monitoring(LaunchTemplateMonitoringArgs.builder()
                        .enabled(true)
                        .build())
                .networkInterfaces(LaunchTemplateNetworkInterfaceArgs.builder()
                        .associatePublicIpAddress("true")
                        .build())
                .placement(LaunchTemplatePlacementArgs.builder()
                        .availabilityZone("us-west-2a")
                        .build())
                .ramDiskId("test")
                .vpcSecurityGroupIds(securityGroup.id().applyValue(List::of))
                .tagSpecifications(LaunchTemplateTagSpecificationArgs.builder()
                        .resourceType("instance")
                        .tags(Map.of("Name", "test"))
                        .build())
                .userData(userData)
                .build());
    }
}
