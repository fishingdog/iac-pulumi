package myproject;

import com.pulumi.Context;

public class Infrastructure {
    public static void deploy(Context ctx) {

//        AWS.runAWS(ctx);

        GCP.runGCP(ctx);

    }

}
