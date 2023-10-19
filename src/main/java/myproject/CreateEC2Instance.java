package myproject;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.ec2.inputs.InstanceRootBlockDeviceArgs;
import com.pulumi.core.Output;
import com.pulumi.aws.ec2.inputs.GetAmiArgs;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateEC2Instance {

    public static Instance createEC2Instance(SecurityGroup sg, String ami, Subnet mysubnet) {
        List<String> sglist = new ArrayList<>();
        return new Instance("MyEc2Instance", InstanceArgs.builder()
                .ami(ami)
                .instanceType("t2.micro")
                .subnetId(mysubnet.id())
                .rootBlockDevice(InstanceRootBlockDeviceArgs.builder()
                        .volumeType("gp2")
                        .volumeSize(25)
                        .deleteOnTermination(true)
                        .build())
                .tags(Map.of("Name", "MyEC2Instance"))
                .build());
    }
}
