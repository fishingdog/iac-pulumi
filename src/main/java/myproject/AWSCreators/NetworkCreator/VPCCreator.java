package myproject.AWSCreators.NetworkCreator;

import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.VpcArgs;

import java.util.Map;

public class VPCCreator {
    public static Vpc createVpc(String cidrBlockValue, String instanceTenancyValue, String tagNameValue) {
        return new Vpc("main", VpcArgs.builder()
                .cidrBlock(cidrBlockValue)
                .instanceTenancy(instanceTenancyValue)
                .tags(Map.of("Name", tagNameValue))
                .build());
    }
}
