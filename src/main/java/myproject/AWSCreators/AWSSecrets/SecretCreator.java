package myproject.AWSCreators.AWSSecrets;

import com.pulumi.Context;
import com.pulumi.aws.secretsmanager.Secret;
import com.pulumi.aws.secretsmanager.SecretArgs;
import com.pulumi.aws.secretsmanager.SecretVersion;
import com.pulumi.aws.secretsmanager.SecretVersionArgs;
import com.pulumi.core.Output;

import java.util.Map;

public class SecretCreator {

    public static Secret creatSecret(Context ctx, String name, Output<String> secretString) {
        var secret = new Secret(name, SecretArgs.builder()
                .name(name)
                .forceOverwriteReplicaSecret(true)
                .build());

        var secretVersion = new SecretVersion("mySecretVersion", SecretVersionArgs.builder()
                .secretId(secret.id())
                .secretString(secretString) // Your secret value
                .build());
        // Export the ARN of the secret
        ctx.export("SecretName", secret.name());
        return secret;
    }

}
