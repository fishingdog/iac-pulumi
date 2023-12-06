package myproject.AWSCreators.AutoScalingAndLoadBalancer;

import com.pulumi.aws.acm.AcmFunctions;
import com.pulumi.aws.acm.Certificate;
import com.pulumi.aws.acm.inputs.CertificateState;
import com.pulumi.aws.acm.inputs.GetCertificateArgs;
import com.pulumi.aws.acm.outputs.GetCertificateResult;
import com.pulumi.aws.alb.*;
import com.pulumi.aws.alb.inputs.ListenerDefaultActionArgs;
import com.pulumi.aws.alb.inputs.TargetGroupHealthCheckArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.core.Output;
import com.pulumi.resources.CustomResourceOptions;

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



    public static Listener httpsListenerCreator(LoadBalancer appLoadBalancer, TargetGroup targetGroup, String domainName) {

        var certificate = AcmFunctions.getCertificate(GetCertificateArgs.builder()
                .domain(domainName)
                .build());

        return new Listener("myHttpsListener", ListenerArgs.builder()
                .loadBalancerArn(appLoadBalancer.arn())
                .port(443) // Listening on port 443 for HTTPS
                .protocol("HTTPS")
                .sslPolicy("ELBSecurityPolicy-2016-08") // specify a security policy
                .certificateArn(certificate.applyValue(GetCertificateResult::arn)) // ARN of the SSL certificate from ACM
                .defaultActions(ListenerDefaultActionArgs.builder()
                        .type("forward")
                        .targetGroupArn(targetGroup.arn())
                        .build())
                .build());
    }
}
