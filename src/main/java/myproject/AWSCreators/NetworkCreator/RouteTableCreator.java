package myproject.AWSCreators.NetworkCreator;

import com.pulumi.aws.ec2.InternetGateway;
import com.pulumi.aws.ec2.RouteTable;
import com.pulumi.aws.ec2.RouteTableArgs;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;

import java.util.Map;

public class RouteTableCreator {
    public static RouteTable createRouteTable(Vpc myvpc, InternetGateway igw, String routeCidrBlockValue, String routeTableNameValue) {

        return new RouteTable("testRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .routes(RouteTableRouteArgs.builder()
                        .cidrBlock(routeCidrBlockValue)
                        .gatewayId(igw.id())
                        .build())
                .tags(Map.of("Name", routeTableNameValue))
                .build());
    }


    public static RouteTable createPrivateRouteTable(Vpc myvpc, String routePrivateTableNameValue) {

        return new RouteTable("privateRouteTable", RouteTableArgs.builder()
                .vpcId(myvpc.id())
                .tags(Map.of("Name", routePrivateTableNameValue))
                .build());
    }
}
