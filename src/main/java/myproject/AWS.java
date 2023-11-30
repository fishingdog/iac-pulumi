package myproject;

import com.pulumi.Context;
import com.pulumi.aws.alb.Listener;
import com.pulumi.aws.alb.LoadBalancer;
import com.pulumi.aws.alb.TargetGroup;
import com.pulumi.aws.autoscaling.Group;
import com.pulumi.aws.autoscaling.Policy;
import com.pulumi.aws.cloudwatch.MetricAlarm;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.rds.ParameterGroup;
import com.pulumi.aws.rds.SubnetGroup;
import com.pulumi.aws.route53.Record;
import com.pulumi.aws.sns.Topic;
import com.pulumi.core.Output;
import myproject.AWSCreators.AutoScalingAndLoadBalancer.AutoScalingCreator;
import myproject.AWSCreators.AutoScalingAndLoadBalancer.LaunchTemplateCreator;
import myproject.AWSCreators.AutoScalingAndLoadBalancer.LoadBalancerCreator;
import myproject.AWSCreators.CloudWatch.CloudWatchCreator;
import myproject.AWSCreators.CloudWatch.RoleCreator;
import myproject.AWSCreators.Instance.CreateEC2Instance;
import myproject.AWSCreators.Instance.RdsCreator;
import myproject.AWSCreators.NetworkCreator.*;
import myproject.AWSCreators.SecurityGroup.SecurityGroupCreatorDB;
import myproject.AWSCreators.SecurityGroup.SecurityGroupCreatorEC2;
import myproject.AWSCreators.SecurityGroup.SecurityGroupCreatorLoadBalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AWS {

    public static void runAWS(Context ctx) {


//        //test
//        String ami = System.getenv("AMI");
//        if (ami == null || Objects.equals(ami, "null")) {ami = "ami-0e735e95644dc74e2";}
//        String keyName = System.getenv("AWS_ACCESS_KEY_NAME");
//        if (keyName == null || Objects.equals(keyName, "null")) {keyName = "testA5";}
//        Instance myInstance = CreateEC2Instance.createEC2Instance(appSecurityGroup, ami, pubSubnetList.get(0), keyName, rdsInstance, cloudWatchRole);


    }

}
