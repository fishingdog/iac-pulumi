package myproject;

import com.pulumi.aws.ec2.Instance;
import com.pulumi.aws.route53.Record;
import com.pulumi.aws.route53.RecordArgs;

import java.util.List;

public class RecordCreator {
    public static Record createRecord(Instance myinstance) {
        return new Record("www", RecordArgs.builder()
                .zoneId("Z08005953SMRXPDJ5ZFMP")
                .name("www.fishdog.me")
                .type("A")
                .ttl(60)
                .records(myinstance.publicIp().applyValue(List::of))
                .build());

    }
}
