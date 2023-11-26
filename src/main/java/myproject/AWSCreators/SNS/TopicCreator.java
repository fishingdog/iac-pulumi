package myproject.AWSCreators.SNS;

import com.pulumi.Context;
import com.pulumi.aws.sns.Topic;
import com.pulumi.aws.sns.TopicArgs;

public class TopicCreator {
    public static Topic createTopic(Context ctx) {
        return new Topic("userUpdates", TopicArgs.builder()
                .build());

    }

}
