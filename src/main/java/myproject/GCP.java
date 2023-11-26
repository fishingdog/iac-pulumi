package myproject;

import com.pulumi.Context;
import com.pulumi.gcp.serviceaccount.Account;
import com.pulumi.gcp.serviceaccount.Key;
import myproject.GCPCreators.ServiceAccountCreator;

public class GCP {
    public static void runGCP(Context ctx) {

       Account lambdaServiceAccount = ServiceAccountCreator.createServiceAccount(ctx);

       Key lambdaKey = ServiceAccountCreator.createAccessKey(ctx, lambdaServiceAccount);

    }
}
