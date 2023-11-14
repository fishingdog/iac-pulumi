import com.pulumi.aws.alb.*;
import com.pulumi.aws.alb.inputs.ListenerDefaultActionArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.core.Output;
import com.pulumi.aws.autoscaling.Group;

import java.lang.annotation.Target;
import java.util.List;

public class LoadBalancerCreator {
    public static LoadBalancer createApplicationLoadBalancer(Output<List<String>> privSubnetIdList) {
        return new LoadBalancer("myAlb", LoadBalancerArgs.builder()
                .loadBalancerType("application")
                .subnets(privSubnetIdList)
                .build());
    }

    public static TargetGroup targetGroupCreator(Vpc myvpc) {
        return new TargetGroup("myTg", TargetGroupArgs.builder()
                .port(8080)
                .protocol("HTTP")
                .vpcId(myvpc.id())
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
