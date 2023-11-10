package myproject;

import com.pulumi.aws.ec2.Instance;
import com.pulumi.aws.route53.Record;
import com.pulumi.aws.route53.RecordArgs;
import com.pulumi.aws.route53.Route53Functions;
import com.pulumi.aws.route53.inputs.GetZoneArgs;
import com.pulumi.aws.route53.outputs.GetZoneResult;
import com.pulumi.core.Output;

import java.util.List;

public class RecordCreator {
    public static Record createRecord(Instance myinstance, String domainName) {

        final var selected = Route53Functions.getZone(GetZoneArgs.builder()
                .name(domainName)
                .build());


        return new Record("www", RecordArgs.builder()
                .zoneId(selected.applyValue(GetZoneResult::zoneId))
                .name("www")
                .type("A")
                .ttl(60)
                .records(myinstance.publicIp().applyValue(List::of))
                .build());

    }
}
