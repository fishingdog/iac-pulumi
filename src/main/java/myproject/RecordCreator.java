//package myproject;
//
//import com.pulumi.Context;
//import com.pulumi.aws.route53.Record;
//import com.pulumi.aws.route53.RecordArgs;
//
//public class RecordCreator {
//    public static Record stack(Context ctx) {
//        var www = new Record("www", RecordArgs.builder()
//                .zoneId(aws_route53_zone.primary().zone_id())
//                .name("www.fishdog.me")
//                .type("A")
//                .ttl(60)
//                .records(aws_eip.lb().public_ip())
//                .build());
//}
