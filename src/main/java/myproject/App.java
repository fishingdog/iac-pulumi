package myproject;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.aws.ec2.*;

import java.util.Map;


public class App {
    public static void main(String[] args) {
        Pulumi.run(Infrastructure::deploy);
        }

}
