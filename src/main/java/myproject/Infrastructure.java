package myproject;

import com.pulumi.Context;
import com.pulumi.aws.AwsFunctions;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;
import com.pulumi.aws.inputs.GetAvailabilityZonesArgs;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;


public class Infrastructure {
    public static void deploy(Context ctx) {
        String vpcCidrBlockValue = System.getenv("VPC_CIDR_BLOCK");
        String vpcInstanceTenancyValue = System.getenv("VPC_INSTANCE_TENANCY");
        String vpcName = System.getenv("VPC_TAG_NAME");
        Vpc myvpc = createVpc(vpcCidrBlockValue, vpcInstanceTenancyValue, vpcName);

        String igTagNameValue = System.getenv("VPC_TAG_NAME");
        InternetGateway igw = createInternetGateway(igTagNameValue);

        attachInternetGateway(myvpc, igw);

        String routeCidrBlockValue = System.getenv("RT_ROUTE_CIDR_BLOCK");
        String routePublicTableNameValue = System.getenv("RT_PUBLIC_ROUTE_TABLE_NAME");
        RouteTable pubRT = createRouteTable(myvpc, igw, routeCidrBlockValue, routePublicTableNameValue);

        String routePrivateTableNameValue = System.getenv("RT_PRIVATE_ROUTE_TABLE_NAME");
        RouteTable privRT = createPrivateRouteTable(myvpc, routePrivateTableNameValue);

//        String subnetCiderPrefix = System.getenv("PUBLIC_SUBNET_CIDER_PREFIX");
//        String subnetCiderStartingIndex = System.getenv("PUBLIC_SUBNET_CIDER_START");
//        String subnetTagNamePrefix = System.getenv("PUBLIC_SUBNET_TAG_NAME_PREFIX");
//        String numOfSubnets = System.getenv("NUM_OF_PUBLIC_SUBNETS");
        String subnetCiderPrefix = "10.1.";
        String subnetCiderStartingIndex = "0";
        String subnetTagNamePrefix = "public_subnet";
        String numOfSubnets = "3";
        createMultipleSubnetWithRouteTable(myvpc, subnetCiderPrefix, subnetCiderStartingIndex, subnetTagNamePrefix, numOfSubnets, pubRT);

        subnetCiderStartingIndex = "100";
        subnetTagNamePrefix = "private_Subnet";
        createMultipleSubnetWithRouteTable(myvpc, subnetCiderPrefix, subnetCiderStartingIndex, subnetTagNamePrefix, numOfSubnets, privRT);

    }

    private static Vpc createVpc(String cidrBlockValue, String instanceTenancyValue, String tagNameValue) {
        return new Vpc("main", VpcArgs.builder()
                .cidrBlock(cidrBlockValue != null ? cidrBlockValue : "10.1.0.0/16")
                .instanceTenancy(instanceTenancyValue != null ? instanceTenancyValue : "default")
                .tags(Map.of("Name", tagNameValue != null ? tagNameValue : "defaultVpc"))
                .build());
    }


    private static InternetGateway createInternetGateway(String tagNameValue) {
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


    public static Subnet createSubnet(Vpc myvpc, String subnetCiderBlockValue, String myZone, String subnetTagNameValue) {
        return new Subnet("main", SubnetArgs.builder()
                .vpcId(myvpc.id())
                .cidrBlock(subnetCiderBlockValue != null ? subnetCiderBlockValue : "10.1.1.0/24")
                .tags(Map.of("Name", subnetTagNameValue != null ? subnetTagNameValue : "mysubnet"))
                .availabilityZone(myZone != null ? myZone : "us-west-2a")
                .build()
            );
    }


    public static void createMultipleSubnetWithRouteTable(Vpc myvpc, String subnetCiderBlockPrefix, String subnetCiderStartingIndex, String subnetTagNamePrefix, String numOfSubnets, RouteTable rt) {

        if (subnetCiderBlockPrefix == null) { subnetCiderBlockPrefix = "10.1."; }
        int startIndex = 0;
        if (subnetCiderStartingIndex != null) {startIndex = Integer.parseInt(subnetCiderStartingIndex);}
        if (subnetTagNamePrefix == null) { subnetTagNamePrefix = "mysubnet"; }
        int numOfSubnet = 0;
        if (numOfSubnets == null) {
            numOfSubnet = 3;
        } else { numOfSubnet = Integer.parseInt(numOfSubnets); }

        final var available = AwsFunctions.getAvailabilityZones(GetAvailabilityZonesArgs.builder()
                .state("available")
                .build());

        for (int i = 0; i < numOfSubnet; i++) {
            int curIndex = startIndex + i;
            String subnetCiderBlockValue = subnetCiderBlockPrefix + curIndex + ".0/24";
            String subnetTagNameValue = subnetTagNamePrefix + i;
            final int zoneIndex = i;
            Subnet mysubnet = new Subnet(subnetTagNameValue, SubnetArgs.builder()
                    .tags(Map.of("Name", subnetTagNameValue))
                    .vpcId(myvpc.id())
                    .cidrBlock(subnetCiderBlockValue)
                    .availabilityZone(available.applyValue(getAvailabilityZonesResult -> getAvailabilityZonesResult.names().get(zoneIndex)))
                    .build());
            associateRouteTableToSubnet(mysubnet, rt);

        }
    }

    private static RouteTable createRouteTable(Vpc myvpc, InternetGateway igw, String routeCidrBlockValue, String routeTableNameValue) {

        return new RouteTable("testRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .routes(RouteTableRouteArgs.builder()
                        .cidrBlock(routeCidrBlockValue != null ? routeCidrBlockValue : "0.0.0.0/0")
                        .gatewayId(igw.id())
                        .build())
                .tags(Map.of("Name", routeTableNameValue != null ? routeTableNameValue : "default_pub_route_table"))
                .build());
    }


    private static RouteTable createPrivateRouteTable(Vpc myvpc, String routePrivateTableNameValue) {

        return new RouteTable("privateRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .tags(Map.of("Name", routePrivateTableNameValue != null ? routePrivateTableNameValue : "default_private_route_table"))
                .build());
    }

    private static void associateRouteTableToSubnet(Subnet mysubnet, RouteTable rt) {
        String randomName = generateRandomString(10);
        new RouteTableAssociation(randomName, RouteTableAssociationArgs.builder()
                    .subnetId(mysubnet.id())
                    .routeTableId(rt.id())
                    .build()
        );
    }


    public static String generateRandomString(int length) {
        final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return sb.toString();
    }

}
