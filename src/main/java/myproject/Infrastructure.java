package myproject;

import com.pulumi.Context;
import com.pulumi.aws.AwsFunctions;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;
import com.pulumi.aws.inputs.GetAvailabilityZonesArgs;
import com.pulumi.aws.inputs.GetAvailabilityZonesPlainArgs;
import com.pulumi.aws.outputs.GetAvailabilityZonesResult;

import java.lang.reflect.Array;
import java.security.SecureRandom;
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

        String igTagNameValue = System.getenv("IG_TAG_NAME");
        if (igTagNameValue == null || Objects.equals(igTagNameValue, "null")) {igTagNameValue = "myGW";}
        InternetGateway igw = createInternetGateway(igTagNameValue);

        attachInternetGateway(myvpc, igw);

        String routeCidrBlockValue = System.getenv("RT_ROUTE_CIDR_BLOCK");
        String routePublicTableNameValue = System.getenv("RT_PUBLIC_ROUTE_TABLE_NAME");
        if (routeCidrBlockValue == null || Objects.equals(routeCidrBlockValue, "null")) {routeCidrBlockValue = "0.0.0.0/0";}
        if (routePublicTableNameValue == null || Objects.equals(routePublicTableNameValue, "null")) {routePublicTableNameValue = "default_pub_route_table";}
        RouteTable pubRT = createRouteTable(myvpc, igw, routeCidrBlockValue, routePublicTableNameValue);

        String routePrivateTableNameValue = System.getenv("RT_PRIVATE_ROUTE_TABLE_NAME");
        if (routePrivateTableNameValue == null || Objects.equals(routePrivateTableNameValue, "null")) {routePrivateTableNameValue = "default_private_route_table";}
        RouteTable privRT = createPrivateRouteTable(myvpc, routePrivateTableNameValue);

        String subnetCiderListPub = System.getenv("PUBLIC_SUBNET_CIDER_LIST");
        String subnetTagNameListPub = System.getenv("PUBLIC_SUBNET_TAG_NAME_LIST");
        if (subnetCiderListPub == null || Objects.equals(subnetCiderListPub, "null")) {subnetCiderListPub = "10.1.0.0/24, 10.1.1.0/24, 10.1.2.0/24";}
        if (subnetTagNameListPub == null || Objects.equals(subnetTagNameListPub, "null")) {subnetTagNameListPub = "public_subnet_a, public_subnet_b, public_subnet_c"; }
        createThreeSubnetWithRouteTable(myvpc, subnetCiderListPub, subnetTagNameListPub, pubRT, ctx);

        String subnetCiderListPriv = System.getenv("PRIV_SUBNET_CIDER_LIST");
        String subnetTagNameListPriv = System.getenv("PRIV_SUBNET_TAG_NAME_LIST");
        if (subnetCiderListPriv == null || Objects.equals(subnetCiderListPriv, "null")) {subnetCiderListPriv = "10.1.100.0/24, 10.1.101.0/24, 10.1.102.0/24";}
        if (subnetTagNameListPriv == null || Objects.equals(subnetTagNameListPriv, "null")) {subnetTagNameListPriv = "private_subnet_a, private_subnet_b, private_subnet_c"; }
        createThreeSubnetWithRouteTable(myvpc, subnetCiderListPriv, subnetTagNameListPriv, privRT, ctx);



//        String subnetCiderPrefixPub = System.getenv("PUBLIC_SUBNET_CIDER_PREFIX");
//        String subnetCiderStartingIndexPub = System.getenv("PUBLIC_SUBNET_CIDER_START");
//        String subnetTagNamePrefixPub = System.getenv("PUBLIC_SUBNET_TAG_NAME_PREFIX");
//        String numOfSubnetsPub = System.getenv("NUM_OF_PUBLIC_SUBNETS");
//        if (subnetCiderPrefixPub == null || Objects.equals(subnetCiderPrefixPub, "null")) {subnetCiderPrefixPub = "10.1.";}
//        if (subnetCiderStartingIndexPub == null || Objects.equals(subnetCiderStartingIndexPub, "null")) {subnetCiderStartingIndexPub = "0"; }
//        if (subnetTagNamePrefixPub == null || Objects.equals(subnetTagNamePrefixPub, "null")) {subnetTagNamePrefixPub = "public_subnet"; }
//        if (numOfSubnetsPub == null || Objects.equals(numOfSubnetsPub, "null")) {numOfSubnetsPub = "3"; }
//        createMultipleSubnetWithRouteTable(myvpc, subnetCiderPrefixPub, subnetCiderStartingIndexPub, subnetTagNamePrefixPub, numOfSubnetsPub, pubRT);
//
//        String subnetCiderPrefixPriv = System.getenv("PRIV_SUBNET_CIDER_PREFIX");
//        String subnetCiderStartingIndexPriv = System.getenv("PRIV_SUBNET_CIDER_START");
//        String subnetTagNamePrefixPriv = System.getenv("PRIV_SUBNET_TAG_NAME_PREFIX");
//        String numOfSubnetsPriv = System.getenv("NUM_OF_PUBLIC_SUBNETS_PRIV");
//        if (subnetCiderPrefixPriv == null || Objects.equals(subnetCiderPrefixPriv, "null")) {subnetCiderPrefixPriv = "10.1.";}
//        if (subnetCiderStartingIndexPriv == null || Objects.equals(subnetCiderStartingIndexPriv, "null")) {subnetCiderStartingIndexPriv = "100"; }
//        if (subnetTagNamePrefixPriv == null || Objects.equals(subnetTagNamePrefixPriv, "null")) {subnetTagNamePrefixPriv = "private_subnet"; }
//        if (numOfSubnetsPriv == null || Objects.equals(numOfSubnetsPriv, "null")) {numOfSubnetsPriv = "3"; }
//        createMultipleSubnetWithRouteTable(myvpc, subnetCiderPrefixPriv, subnetCiderStartingIndexPriv, subnetTagNamePrefixPriv, numOfSubnetsPriv, privRT);

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


    public static Subnet createSubnet(Vpc myvpc, String subnetCiderBlockValue, String myZone, String subnetTagNameValue) {
        return new Subnet("main", SubnetArgs.builder()
                .vpcId(myvpc.id())
                .cidrBlock(subnetCiderBlockValue != null ? subnetCiderBlockValue : "10.1.1.0/24")
                .tags(Map.of("Name", subnetTagNameValue != null ? subnetTagNameValue : "mysubnet"))
                .availabilityZone(myZone != null ? myZone : "us-west-2a")
                .build()
            );
    }

    public static void createThreeSubnetWithRouteTable(Vpc myvpc, String subnetCiderList, String subnetTagNameList, RouteTable rt, Context ctx) {

        String[] ciderList = subnetCiderList.split(",\\s*");
        ArrayList<String> listOfCider = new ArrayList<>(Arrays.asList(ciderList));

        String[] nameList = subnetTagNameList.split(",\\s*");
        ArrayList<String> listOfName = new ArrayList<>(Arrays.asList(nameList));

        int numOfSubnets = listOfCider.size();

        try {
            final var available = AwsFunctions.getAvailabilityZonesPlain(GetAvailabilityZonesPlainArgs.builder()
                    .state("available")
                    .build());

            final GetAvailabilityZonesResult getAvailabilityZonesResult = available.get();

            final List<String> zoneNameList = getAvailabilityZonesResult.names();

            if (numOfSubnets > zoneNameList.size()) {
                numOfSubnets = zoneNameList.size();
            }

            for (int i = 0; i < numOfSubnets; i++) {

                String subnetCiderBlockValue = listOfCider.get(i);
                String subnetTagNameValue = listOfName.get(i);
                final int zoneIndex = i;
                Subnet mysubnet = new Subnet(subnetTagNameValue, SubnetArgs.builder()
                        .tags(Map.of("Name", subnetTagNameValue))
                        .vpcId(myvpc.id())
                        .cidrBlock(subnetCiderBlockValue)
                        .availabilityZone(zoneNameList.get(i))
                        .build());
                associateRouteTableToSubnet(mysubnet, rt);

            }
        } catch (Exception e) {
            ctx.log().error(e.getMessage());
        }
    }


    public static void createMultipleSubnetWithRouteTable(Vpc myvpc, String subnetCiderBlockPrefix, String subnetCiderStartingIndex, String subnetTagNamePrefix, String numOfSubnets, RouteTable rt) {

        int startIndex = 0;
        startIndex = Integer.parseInt(subnetCiderStartingIndex);
        int numOfSubnet = 0;
        numOfSubnet = Integer.parseInt(numOfSubnets);

        final var available = AwsFunctions.getAvailabilityZones(GetAvailabilityZonesArgs.builder()
                .state("available")
                .build());

        for (int i = 0; i < numOfSubnet; i++) {
            final int curIndex = startIndex + i;
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
