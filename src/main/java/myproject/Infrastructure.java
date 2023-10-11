package myproject;

import com.pulumi.Context;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;

import java.util.Map;

public class Infrastructure {
    public static void deploy(Context ctx) {
        Vpc myvpc = createVpc();
        InternetGateway igw = createInternetGateway();
        attachInternetGateway(myvpc, igw);
        Subnet mysubnet = createSubnet(myvpc);
        RouteTable pubRT = createRouteTable(myvpc, igw);
        RouteTable privRT = createPrivateRouteTable(myvpc);
        associateRouteTableToSubnet(mysubnet, pubRT);

    }

    private static Vpc createVpc() {
        String cidrBlockValue = System.getenv("CIDR_BLOCK");
        String instanceTenancyValue = System.getenv("INSTANCE_TENANCY");
        String tagNameValue = System.getenv("TAG_NAME");

        return new Vpc("main", VpcArgs.builder()
                .cidrBlock(cidrBlockValue != null ? cidrBlockValue : "10.1.0.0/16")
                .instanceTenancy(instanceTenancyValue != null ? instanceTenancyValue : "default")
                .tags(Map.of("Name", tagNameValue != null ? tagNameValue : "defaultVpc"))
                .build());
    }

    private static InternetGateway createInternetGateway() {
        String tagNameValue = System.getenv("TAG_NAME");
        return new InternetGateway("gw", InternetGatewayArgs.builder()
                .tags(Map.of("Name", tagNameValue != null ? tagNameValue : "defaultGW"))
                .build());
    }

    private static void attachInternetGateway(Vpc myvpc, InternetGateway gw) {
        new InternetGatewayAttachment("InternetGatewayAttachment", InternetGatewayAttachmentArgs.builder()
                .internetGatewayId(gw.id())
                .vpcId(myvpc.id())
                .build());
    }

    public static Subnet createSubnet(Vpc myvpc) {
        String subnetCiderBlockValue = System.getenv("SUBNET_CIDR_BLOCK");
        String subnetTagNameValue = System.getenv("SUBNET_TAG_NAME");

        return new Subnet("main", SubnetArgs.builder()
                .vpcId(myvpc.id())
                .cidrBlock(subnetCiderBlockValue != null ? subnetCiderBlockValue : "10.1.1.0/24")
                .tags(Map.of("Name", subnetTagNameValue != null ? subnetTagNameValue : "mysubnet"))
                .build()
            );
    }

    private static RouteTable createRouteTable(Vpc myvpc, InternetGateway igw) {
        String routeCidrBlockValue = System.getenv("ROUTE_CIDR_BLOCK");
        String routeTableNameValue = System.getenv("PUBLIC_ROUTE_TABLE_NAME");

        return new RouteTable("testRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .routes(RouteTableRouteArgs.builder()
                        .cidrBlock(routeCidrBlockValue != null ? routeCidrBlockValue : "0.0.0.0/0")
                        .gatewayId(igw.id())
                        .build())
                .tags(Map.of("Name", routeTableNameValue != null ? routeTableNameValue : "default_pub_route_table"))
                .build());
    }

    private static RouteTable createPrivateRouteTable(Vpc myvpc) {
        String privateRouteTableNameValue = System.getenv("PRIVATE_ROUTE_TABLE_NAME");

        return new RouteTable("privateRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .tags(Map.of("Name", privateRouteTableNameValue != null ? privateRouteTableNameValue : "default_private_route_table"))
                .build());
    }

    private static RouteTableAssociation associateRouteTableToSubnet(Subnet mysubnet, RouteTable pubRT) {
        String associationName = System.getenv("ROUTE_TABLE_ASSOCIATION_NAME");

        return new RouteTableAssociation(
                associationName != null ? associationName : "routeTableAssociation",
                RouteTableAssociationArgs.builder()
                        .subnetId(mysubnet.id())
                        .routeTableId(pubRT.id())
                        .build()
        );
    }

}
