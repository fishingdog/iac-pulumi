package myproject.GCPCreators;

import com.pulumi.Context;
import com.pulumi.core.Output;
import com.pulumi.gcp.organizations.OrganizationsFunctions;
import com.pulumi.gcp.organizations.inputs.GetIAMPolicyArgs;
import com.pulumi.gcp.organizations.inputs.GetIAMPolicyBindingArgs;
import com.pulumi.gcp.organizations.outputs.GetIAMPolicyResult;
import com.pulumi.gcp.serviceaccount.*;
import com.pulumi.gcp.storage.BucketIAMMember;
import com.pulumi.gcp.storage.BucketIAMMemberArgs;
import myproject.AWSCreators.AWSSecrets.SecretCreator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class ServiceAccountCreator {

    public static Account createServiceAccount(Context ctx, String GCPBucketName) {
        Account lambdaAccount = new Account("lambdaServiceAccount", AccountArgs.builder()
                .accountId("lambda")
                .displayName("Lambda Service Account")
                .build());

        bindStorageObjectUserRole(lambdaAccount, GCPBucketName);

        ctx.export("lambdaServiceAccountId", lambdaAccount.accountId());

        return lambdaAccount;
    }


//    private static void attachPolicy(Account account) {
//        final var admin = OrganizationsFunctions.getIAMPolicy(GetIAMPolicyArgs.builder()
//                .bindings(GetIAMPolicyBindingArgs.builder()
//                        .role("roles/storage.admin")
//                        .members(account.email().applyValue(List::of))
//                        .build())
//                .build());
//
////        new IAMPolicy("admin-account-iam", IAMPolicyArgs.builder()
////                .serviceAccountId(account.name())
////                .policyData(admin.applyValue(GetIAMPolicyResult::policyData))
////                .build());
//    }

    public static Output<String> createAccessKey(Context ctx, Account myAccount) {
        Key lambdaKey = new Key("mykey", KeyArgs.builder()
                .serviceAccountId(myAccount.name())
                .publicKeyType("TYPE_X509_PEM_FILE")
                .build());

        ctx.export("lambdaKeyId", lambdaKey.id());

        final Output<String> stringOutput = lambdaKey.privateKey();
        Output<String> out = stringOutput.applyValue(encodedPrivateKey -> {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedPrivateKey);

            String decodedPrivateKey = new String(decodedBytes, StandardCharsets.UTF_8);


            String noNewLinesPrivateKey = decodedPrivateKey.replace("\n", "").replace("\r", "");
            return noNewLinesPrivateKey;
        });
        ctx.export("serviceAccountPrivateKey1", Output.of(out));

        return out;
    }

    private static void bindStorageObjectUserRole(Account serviceAccount, String GCPBucketName) {
        BucketIAMMember bucketIAMBinding = new BucketIAMMember("bucketIAM", BucketIAMMemberArgs.builder()
                .bucket(GCPBucketName)
                .role("roles/storage.objectAdmin")
                .member(serviceAccount.email().applyValue(email -> "serviceAccount:" + email))
                .build());
    }
}
