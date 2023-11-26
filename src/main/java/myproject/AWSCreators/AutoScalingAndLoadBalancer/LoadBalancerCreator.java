package myproject.AWSCreators.AutoScalingAndLoadBalancer;

import com.pulumi.aws.alb.*;
import com.pulumi.aws.alb.inputs.ListenerDefaultActionArgs;
import com.pulumi.aws.alb.inputs.TargetGroupHealthCheckArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.core.Output;

import java.util.List;

public class LoadBalancerCreator {
    public static LoadBalancer createApplicationLoadBalancer(Output<List<String>> pubSubnetIdList, SecurityGroup lbSecurityGroup) {
        return new LoadBalancer("myAlb", LoadBalancerArgs.builder()
                .loadBalancerType("application")
                .subnets(pubSubnetIdList)
                .securityGroups(lbSecurityGroup.id().applyValue(List::of))
                .build());
    }

    public static TargetGroup targetGroupCreator(Vpc myvpc) {
        return new TargetGroup("myTg", TargetGroupArgs.builder()
                .port(8080)
                .protocol("HTTP")
                .vpcId(myvpc.id())
                .healthCheck(TargetGroupHealthCheckArgs.builder()
                        .enabled(true)
                        .interval(30)
                        .unhealthyThreshold(5)
                        .timeout(10)
                        .healthyThreshold(2)
                        .matcher("200")
                        .path("/healthz")
                        .protocol("HTTP")
                        .build())
                .build());
    }

    public static Listener listenerCreator(LoadBalancer appLoadBalancer, TargetGroup targetGroup) {
        return new Listener("myListener", ListenerArgs.builder()
                .loadBalancerArn(appLoadBalancer.arn())
                .port(80)
                .protocol("HTTP")
                .defaultActions(ListenerDefaultActionArgs.builder()
                        .type("forward")
                        .targetGroupArn(targetGroup.arn())
                        .build())
                .build());
    }

}
