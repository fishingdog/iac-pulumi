package myproject;

import com.pulumi.Context;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.rds.ParameterGroup;
import com.pulumi.aws.rds.SubnetGroup;
import com.pulumi.aws.route53.Record;
import com.pulumi.core.Output;

import java.util.*;


public class Infrastructure {
    public static void deploy(Context ctx) {

        String vpcCidrBlockValue = System.getenv("VPC_CIDR_BLOCK");
        String vpcInstanceTenancyValue = System.getenv("VPC_INSTANCE_TENANCY");
        String vpcName = System.getenv("VPC_TAG_NAME");
        if (vpcCidrBlockValue == null || Objects.equals(vpcCidrBlockValue, "null")) {vpcCidrBlockValue = "10.1.0.0/16";}
        if (vpcInstanceTenancyValue == null || Objects.equals(vpcInstanceTenancyValue, "null")) {vpcInstanceTenancyValue = "default";}
        if (vpcName == null || Objects.equals(vpcName, "null")) {vpcName = "myVpc";}
        Vpc myvpc = createVpc(vpcCidrBlockValue, vpcInstanceTenancyValue, vpcName);

        SecurityGroup appSecurityGroup = SecurityGroupCreatorEC2.createApplicationSecurityGroup(myvpc);
        SecurityGroup dbSecurityGroup = SecurityGroupCreatorDB.createDatabaseSecurityGroup(myvpc, appSecurityGroup);

        //create & attaching internet gateway to created VPC
        String igTagNameValue = System.getenv("IG_TAG_NAME");
        if (igTagNameValue == null || Objects.equals(igTagNameValue, "null")) {igTagNameValue = "myGW";}
        InternetGateway igw = createInternetGateway(igTagNameValue);

        attachInternetGateway(myvpc, igw);

        // Create public & private routing tables
        String routeCidrBlockValue = System.getenv("RT_ROUTE_CIDR_BLOCK");
        String routePublicTableNameValue = System.getenv("RT_PUBLIC_ROUTE_TABLE_NAME");
        if (routeCidrBlockValue == null || Objects.equals(routeCidrBlockValue, "null")) {routeCidrBlockValue = "0.0.0.0/0";}
        if (routePublicTableNameValue == null || Objects.equals(routePublicTableNameValue, "null")) {routePublicTableNameValue = "default_pub_route_table";}
        RouteTable pubRT = createRouteTable(myvpc, igw, routeCidrBlockValue, routePublicTableNameValue);

        String routePrivateTableNameValue = System.getenv("RT_PRIVATE_ROUTE_TABLE_NAME");
        if (routePrivateTableNameValue == null || Objects.equals(routePrivateTableNameValue, "null")) {routePrivateTableNameValue = "default_private_route_table";}
        RouteTable privRT = createPrivateRouteTable(myvpc, routePrivateTableNameValue);

        // create 3 public subnets & 3 private subnets in the VPC created
        String subnetCiderListPub = System.getenv("PUBLIC_SUBNET_CIDER_LIST");
        String subnetTagNameListPub = System.getenv("PUBLIC_SUBNET_TAG_NAME_LIST");
        if (subnetCiderListPub == null || Objects.equals(subnetCiderListPub, "null")) {subnetCiderListPub = "10.1.0.0/24, 10.1.1.0/24, 10.1.2.0/24";}
        if (subnetTagNameListPub == null || Objects.equals(subnetTagNameListPub, "null")) {subnetTagNameListPub = "public_subnet_a, public_subnet_b, public_subnet_c"; }
        ArrayList<Subnet> pubSubnetList = SubnetCreator.createThreeSubnetWithRouteTable(myvpc, subnetCiderListPub, subnetTagNameListPub, pubRT, ctx);

        String subnetCiderListPriv = System.getenv("PRIV_SUBNET_CIDER_LIST");
        String subnetTagNameListPriv = System.getenv("PRIV_SUBNET_TAG_NAME_LIST");
        if (subnetCiderListPriv == null || Objects.equals(subnetCiderListPriv, "null")) {subnetCiderListPriv = "10.1.100.0/24, 10.1.101.0/24, 10.1.102.0/24";}
        if (subnetTagNameListPriv == null || Objects.equals(subnetTagNameListPriv, "null")) {subnetTagNameListPriv = "private_subnet_a, private_subnet_b, private_subnet_c"; }
        ArrayList<Subnet> privSubnetList = SubnetCreator.createThreeSubnetWithRouteTable(myvpc, subnetCiderListPriv, subnetTagNameListPriv, privRT, ctx);

        // create parameter group, subnet group, spin up database
        ParameterGroup parameterGroup = RdsCreator.createRDSParameterGroup();
        Output<List<String>> privSubnetIdList = SubnetCreator.getSubnetIdListFromSubnets(privSubnetList);
        SubnetGroup mySubnetGroup = SubnetCreator.createSubnetGroupRDS(privSubnetIdList);

        com.pulumi.aws.rds.Instance rdsInstance = RdsCreator.createRDSInstance(parameterGroup, mySubnetGroup, dbSecurityGroup);

        // creating a role for cloudwatch agent
        Role cloudWatchRole = RoleCreator.createRole();

        // fetch ami id from environment variable and pull up instance from this AMI
        String ami = System.getenv("AMI");
        if (ami == null || Objects.equals(ami, "null")) {ami = "ami-06e930d39870c0680";}
        String keyName = System.getenv("AWS_ACCESS_KEY_NAME");
        if (keyName == null || Objects.equals(keyName, "null")) {keyName = "testA5";}
        Instance myInstance = CreateEC2Instance.createEC2Instance(appSecurityGroup, ami, pubSubnetList.get(0), keyName, rdsInstance, cloudWatchRole);

        // create "A record" and attach to the ec2 instance
        Record myRecord = RecordCreator.createRecord(myInstance);
    }

    private static Vpc createVpc(String cidrBlockValue, String instanceTenancyValue, String tagNameValue) {
        return new Vpc("main", VpcArgs.builder()
                .cidrBlock(cidrBlockValue)
                .instanceTenancy(instanceTenancyValue)
                .tags(Map.of("Name", tagNameValue))
                .build());
    }


    private static InternetGateway createInternetGateway(String tagNameValue) {
        return new InternetGateway("gw", InternetGatewayArgs.builder()
                .tags(Map.of("Name", tagNameValue))
                .build());
    }

    private static void attachInternetGateway(Vpc myvpc, InternetGateway gw) {
        new InternetGatewayAttachment("InternetGatewayAttachment", InternetGatewayAttachmentArgs.builder()
                .internetGatewayId(gw.id())
                .vpcId(myvpc.id())
                .build());
    }






    private static RouteTable createRouteTable(Vpc myvpc, InternetGateway igw, String routeCidrBlockValue, String routeTableNameValue) {

        return new RouteTable("testRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .routes(RouteTableRouteArgs.builder()
                        .cidrBlock(routeCidrBlockValue)
                        .gatewayId(igw.id())
                        .build())
                .tags(Map.of("Name", routeTableNameValue))
                .build());
    }


    private static RouteTable createPrivateRouteTable(Vpc myvpc, String routePrivateTableNameValue) {

        return new RouteTable("privateRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .tags(Map.of("Name", routePrivateTableNameValue))
                .build());
    }

}
