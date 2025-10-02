package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Getter
@Builder
@DynamoDbBean
@EqualsAndHashCode
public class StatusList {
    @NonNull Integer idx;
    @NonNull String uri;

    public StatusList() {
        // Empty constructor needed for dynamoDb deserialization
    }

    public StatusList(@NotNull Integer idx, @NotNull String uri) {
        this.idx = idx;
        this.uri = uri;
    }
}
