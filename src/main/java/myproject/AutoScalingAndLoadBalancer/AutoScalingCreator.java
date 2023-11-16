package myproject.AutoScalingAndLoadBalancer;

import com.pulumi.aws.alb.TargetGroup;
import com.pulumi.aws.autoscaling.Group;
import com.pulumi.aws.autoscaling.GroupArgs;
import com.pulumi.aws.autoscaling.Policy;
import com.pulumi.aws.autoscaling.PolicyArgs;
import com.pulumi.aws.autoscaling.inputs.GroupLaunchTemplateArgs;
import com.pulumi.aws.autoscaling.inputs.PolicyTargetTrackingConfigurationArgs;
import com.pulumi.aws.autoscaling.inputs.PolicyTargetTrackingConfigurationPredefinedMetricSpecificationArgs;
import com.pulumi.aws.cloudwatch.MetricAlarm;
import com.pulumi.aws.cloudwatch.MetricAlarmArgs;
import com.pulumi.aws.ec2.LaunchTemplate;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;


public class AutoScalingCreator {

    public static Group createAutoScalingGroup(LaunchTemplate launchTemplate, TargetGroup targetGroup, Output<List<String>> subnetIds) {

        return new Group("myAutoScalingGroup", GroupArgs.builder()
//                .availabilityZones("us-west-2b")
                .maxSize(3)
                .minSize(1)
                .desiredCapacity(1)
                .vpcZoneIdentifiers(subnetIds)
                .launchTemplate(GroupLaunchTemplateArgs.builder()
                        .id(launchTemplate.id())
                        .version("$Latest")
                        .build())
                .defaultCooldown(60)
                .healthCheckGracePeriod(300)
                .targetGroupArns(targetGroup.arn().applyValue(List::of))
//                .tags(GroupTagArgs.builder()
//                        .key("Name")
//                        .value("asg_launch_config")
//                        .propagateAtLaunch(true)
//                        .build())
                .build());
    }

    public static Policy createAutoScalingUpPolicy(Group group) {
        return new Policy("scaleUpPolicy", PolicyArgs.builder()
                .policyType("SimpleScaling")
                .adjustmentType("ChangeInCapacity")
                .scalingAdjustment(1)
                .cooldown(60)
                .autoscalingGroupName(group.name())
                .build());
    }

    public static MetricAlarm createAutoScalingUpMetricAlarm(Policy policy, Group group) {
        return new MetricAlarm("cpuHighAlarm", MetricAlarmArgs.builder()
                .comparisonOperator("GreaterThanThreshold")
                .evaluationPeriods(1)
                .metricName("CPUUtilization")
                .namespace("AWS/EC2")
                .period(180)
                .statistic("Average")
                .threshold(5.0)
                .dimensions(group.name().applyValue(name -> Map.of("AutoScalingGroupName", name)))
                .alarmActions(policy.arn().applyValue(List::of))
                .build());
    }

    public static Policy createAutoScalingDownPolicy(Group group) {
        return new Policy("scaleDownPolicy", PolicyArgs.builder()
                .adjustmentType("ChangeInCapacity")
                .scalingAdjustment(-1)
                .cooldown(60)
                .autoscalingGroupName(group.name())
                .build());
    }

    public static MetricAlarm createAutoScalingDownMetricAlarm(Policy policy, Group group) {
        return new MetricAlarm("cpuLowAlarm", MetricAlarmArgs.builder()
                .comparisonOperator("LessThanThreshold")
                .evaluationPeriods(1)
                .metricName("CPUUtilization")
                .namespace("AWS/EC2")
                .period(180)
                .statistic("Average")
                .threshold(3.0)
                .dimensions(group.name().applyValue(name -> Map.of("AutoScalingGroupName", name)))
                .alarmActions(policy.arn().applyValue(List::of))
                .build());
    }
}
