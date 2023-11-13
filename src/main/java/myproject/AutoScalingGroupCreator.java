package myproject;

import com.pulumi.aws.autoscaling.Group;
import com.pulumi.aws.autoscaling.GroupArgs;
import com.pulumi.aws.autoscaling.inputs.GroupLaunchTemplateArgs;
import com.pulumi.aws.autoscaling.inputs.GroupTagArgs;
import com.pulumi.aws.ec2.LaunchTemplate;


public class AutoScalingGroupCreator {

    public static Group createAutoScalingGroup(LaunchTemplate launchTemplate) {

        return new Group("bar", GroupArgs.builder()
                .availabilityZones("us-west-2a","us-west-2b")
                .desiredCapacity(1)
                .maxSize(3)
                .minSize(1)
                .launchTemplate(GroupLaunchTemplateArgs.builder()
                        .id(launchTemplate.id())
                        .version("$Latest")
                        .build())
                .defaultCooldown(60)
                .tags(GroupTagArgs.builder()
                        .key("Name")
                        .value("asg_launch_config")
                        .propagateAtLaunch(true)
                        .build())
                .build());

    }
}
