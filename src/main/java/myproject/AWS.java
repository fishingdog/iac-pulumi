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
import com.pulumi.core.Output;
import myproject.AWSCreators.AutoScalingAndLoadBalancer.AutoScalingCreator;
import myproject.AWSCreators.AutoScalingAndLoadBalancer.LaunchTemplateCreator;
import myproject.AWSCreators.AutoScalingAndLoadBalancer.LoadBalancerCreator;
import myproject.AWSCreators.CloudWatch.CloudWatchCreator;
import myproject.AWSCreators.CloudWatch.RoleCreator;
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
        String vpcCidrBlockValue = System.getenv("VPC_CIDR_BLOCK");
        String vpcInstanceTenancyValue = System.getenv("VPC_INSTANCE_TENANCY");
        String vpcName = System.getenv("VPC_TAG_NAME");
        if (vpcCidrBlockValue == null || Objects.equals(vpcCidrBlockValue, "null")) {
            vpcCidrBlockValue = "10.1.0.0/16";
        }
        if (vpcInstanceTenancyValue == null || Objects.equals(vpcInstanceTenancyValue, "null")) {
            vpcInstanceTenancyValue = "default";
        }
        if (vpcName == null || Objects.equals(vpcName, "null")) {
            vpcName = "myVpc";
        }
        Vpc myvpc = VPCCreator.createVpc(vpcCidrBlockValue, vpcInstanceTenancyValue, vpcName);

        SecurityGroup lBSecurityGroup = SecurityGroupCreatorLoadBalancer.createLoadBalancerSecurityGroup(myvpc);
        SecurityGroup appSecurityGroup = SecurityGroupCreatorEC2.createApplicationSecurityGroup(myvpc, lBSecurityGroup);
        SecurityGroup dbSecurityGroup = SecurityGroupCreatorDB.createDatabaseSecurityGroup(myvpc, appSecurityGroup);

        //create & attaching internet gateway to created VPC
        String igTagNameValue = System.getenv("IG_TAG_NAME");
        if (igTagNameValue == null || Objects.equals(igTagNameValue, "null")) {igTagNameValue = "myGW";}
        InternetGateway igw = InternetGatewayCreator.createInternetGateway(igTagNameValue);

        InternetGatewayCreator.attachInternetGateway(myvpc, igw);

        // Create public & private routing tables
        String routeCidrBlockValue = System.getenv("RT_ROUTE_CIDR_BLOCK");
        String routePublicTableNameValue = System.getenv("RT_PUBLIC_ROUTE_TABLE_NAME");
        if (routeCidrBlockValue == null || Objects.equals(routeCidrBlockValue, "null")) {routeCidrBlockValue = "0.0.0.0/0";}
        if (routePublicTableNameValue == null || Objects.equals(routePublicTableNameValue, "null")) {routePublicTableNameValue = "default_pub_route_table";}
        RouteTable pubRT = RouteTableCreator.createRouteTable(myvpc, igw, routeCidrBlockValue, routePublicTableNameValue);

        String routePrivateTableNameValue = System.getenv("RT_PRIVATE_ROUTE_TABLE_NAME");
        if (routePrivateTableNameValue == null || Objects.equals(routePrivateTableNameValue, "null")) {routePrivateTableNameValue = "default_private_route_table";}
        RouteTable privRT = RouteTableCreator.createPrivateRouteTable(myvpc, routePrivateTableNameValue);

        // create 3 public subnets & 3 private subnets in the VPC created
        String subnetCiderListPub = System.getenv("PUBLIC_SUBNET_CIDER_LIST");
        String subnetTagNameListPub = System.getenv("PUBLIC_SUBNET_TAG_NAME_LIST");
        if (subnetCiderListPub == null || Objects.equals(subnetCiderListPub, "null")) {subnetCiderListPub = "10.1.0.0/24, 10.1.1.0/24, 10.1.2.0/24";}
        if (subnetTagNameListPub == null || Objects.equals(subnetTagNameListPub, "null")) {subnetTagNameListPub = "public_subnet_a, public_subnet_b, public_subnet_c"; }
        ArrayList<Subnet> pubSubnetList = SubnetCreator.createThreeSubnetWithRouteTable(myvpc, subnetCiderListPub, subnetTagNameListPub, pubRT, ctx, "public");
        Output<List<String>> pubSubnetIdList = SubnetCreator.getSubnetIdListFromSubnets(pubSubnetList);

        String subnetCiderListPriv = System.getenv("PRIV_SUBNET_CIDER_LIST");
        String subnetTagNameListPriv = System.getenv("PRIV_SUBNET_TAG_NAME_LIST");
        if (subnetCiderListPriv == null || Objects.equals(subnetCiderListPriv, "null")) {subnetCiderListPriv = "10.1.100.0/24, 10.1.101.0/24, 10.1.102.0/24";}
        if (subnetTagNameListPriv == null || Objects.equals(subnetTagNameListPriv, "null")) {subnetTagNameListPriv = "private_subnet_a, private_subnet_b, private_subnet_c"; }
        ArrayList<Subnet> privSubnetList = SubnetCreator.createThreeSubnetWithRouteTable(myvpc, subnetCiderListPriv, subnetTagNameListPriv, privRT, ctx, "private");

        // create parameter group, subnet group, spin up database
        ParameterGroup parameterGroup = RdsCreator.createRDSParameterGroup();
        Output<List<String>> privSubnetIdList = SubnetCreator.getSubnetIdListFromSubnets(privSubnetList);
        SubnetGroup mySubnetGroup = SubnetCreator.createSubnetGroupRDS(privSubnetIdList);

        com.pulumi.aws.rds.Instance rdsInstance = RdsCreator.createRDSInstance(parameterGroup, mySubnetGroup, dbSecurityGroup);

        // creating a role for cloudwatch agent
        Role cloudWatchRole = RoleCreator.createRole();

        // create logGroup & logStream
        CloudWatchCreator.logGroupCreator();

        // create Launch template
        String ami = System.getenv("AMI");
        if (ami == null || Objects.equals(ami, "null")) {ami = "ami-06e930d39870c0680";}
        String keyName = System.getenv("AWS_ACCESS_KEY_NAME");
        if (keyName == null || Objects.equals(keyName, "null")) {keyName = "testA5";}
        LaunchTemplate launchTemplate = LaunchTemplateCreator.createLaunchTemplate(ami, keyName, rdsInstance, cloudWatchRole, appSecurityGroup);

        TargetGroup targetGroup = LoadBalancerCreator.targetGroupCreator(myvpc);

        // Create Load Balancer
        LoadBalancer appLoadBalancer = LoadBalancerCreator.createApplicationLoadBalancer(pubSubnetIdList, lBSecurityGroup);

        // Create Listener for Load Balancer
        Listener listener = LoadBalancerCreator.listenerCreator(appLoadBalancer, targetGroup);

        // create auto-scaling group
        Group autoScalingGroup = AutoScalingCreator.createAutoScalingGroup(launchTemplate, targetGroup, pubSubnetIdList);

        // create Auto Scaling Policies and MetricAlarms
        Policy autoScalingUpPolicy = AutoScalingCreator.createAutoScalingUpPolicy(autoScalingGroup);
        MetricAlarm autoScalingUpMetricAlarm = AutoScalingCreator.createAutoScalingUpMetricAlarm(autoScalingUpPolicy, autoScalingGroup);

        Policy autoScalingDownPolicy = AutoScalingCreator.createAutoScalingDownPolicy(autoScalingGroup);
        MetricAlarm autoScalingDownMetricAlarm = AutoScalingCreator.createAutoScalingDownMetricAlarm(autoScalingDownPolicy, autoScalingGroup);

        // create "A record" and attach to the load balancer
        String domainName = System.getenv("MY_DOMAIN_NAME");
        if (domainName == null || Objects.equals(domainName, "null")) {domainName = "dev.fishdog.me";}
        Record myRecord = RecordCreator.createRecord(appLoadBalancer, domainName);
    }

}
