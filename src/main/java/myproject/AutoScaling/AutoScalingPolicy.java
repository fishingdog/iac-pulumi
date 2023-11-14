package myproject.AutoScaling;

import com.pulumi.aws.autoscaling.Policy;
import com.pulumi.aws.autoscaling.PolicyArgs;
import com.pulumi.aws.autoscaling.inputs.PolicyTargetTrackingConfigurationArgs;
import com.pulumi.aws.autoscaling.inputs.PolicyTargetTrackingConfigurationPredefinedMetricSpecificationArgs;

public class AutoScalingPolicy {

    public static Policy createAutoScalingPolicy() {
        return new Policy("example", PolicyArgs.builder()
                .targetTrackingConfiguration(PolicyTargetTrackingConfigurationArgs.builder()
                        .predefinedMetricSpecification(PolicyTargetTrackingConfigurationPredefinedMetricSpecificationArgs.builder()
                                .predefinedMetricType("ASGAverageCPUUtilization")
                                .build())
                        .targetValue(3.0)
                        .build())
                .build());

    }
}
