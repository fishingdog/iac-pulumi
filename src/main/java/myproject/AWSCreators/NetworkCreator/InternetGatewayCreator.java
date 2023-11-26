package myproject.AWSCreators.NetworkCreator;

import com.pulumi.aws.ec2.*;

import java.util.Map;

public class InternetGatewayCreator {
    public static InternetGateway createInternetGateway(String tagNameValue) {
        return new InternetGateway("gw", InternetGatewayArgs.builder()
                .tags(Map.of("Name", tagNameValue))
                .build());
    }

    public static void attachInternetGateway(Vpc myvpc, InternetGateway gw) {
        new InternetGatewayAttachment("InternetGatewayAttachment", InternetGatewayAttachmentArgs.builder()
                .internetGatewayId(gw.id())
                .vpcId(myvpc.id())
                .build());
    }
}
