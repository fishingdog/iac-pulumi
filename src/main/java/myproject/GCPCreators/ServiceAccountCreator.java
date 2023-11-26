package myproject.GCPCreators;

import com.pulumi.Context;
import com.pulumi.core.Output;
import com.pulumi.gcp.organizations.OrganizationsFunctions;
import com.pulumi.gcp.organizations.inputs.GetIAMPolicyArgs;
import com.pulumi.gcp.organizations.inputs.GetIAMPolicyBindingArgs;
import com.pulumi.gcp.organizations.outputs.GetIAMPolicyResult;
import com.pulumi.gcp.serviceaccount.*;

import java.util.List;

public class ServiceAccountCreator {

    public static Account createServiceAccount(Context ctx) {
        Account lambdaAccount = new Account("lambdaServiceAccount", AccountArgs.builder()
                .accountId("lambda")
                .displayName("Lambda Service Account")
                .build());

        attachPolicy(lambdaAccount);

        ctx.export("lambdaServiceAccountId", lambdaAccount.accountId());

        return lambdaAccount;
    }


    private static void attachPolicy(Account account) {
        final var admin = OrganizationsFunctions.getIAMPolicy(GetIAMPolicyArgs.builder()
                .bindings(GetIAMPolicyBindingArgs.builder()
                        .role("roles/storage.admin")
                        .members(account.email().applyValue(List::of))
                        .build())
                .build());

//        new IAMPolicy("admin-account-iam", IAMPolicyArgs.builder()
//                .serviceAccountId(account.name())
//                .policyData(admin.applyValue(GetIAMPolicyResult::policyData))
//                .build());
    }

    public static Key createAccessKey(Context ctx, Account myAccount) {
        Key lambdaKey = new Key("mykey", KeyArgs.builder()
                .serviceAccountId(myAccount.name())
                .publicKeyType("TYPE_X509_PEM_FILE")
                .build());

        ctx.export("lambdaKeyId", lambdaKey.id());

        return lambdaKey;

    }
}
