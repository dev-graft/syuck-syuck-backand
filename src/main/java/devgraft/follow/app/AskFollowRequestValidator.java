package devgraft.follow.app;

import devgraft.support.exception.ValidationError;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class AskFollowRequestValidator {

    public List<ValidationError> validate(final AskFollowRequest request) {
        final List<ValidationError> errors = new ArrayList<>();

        if (!StringUtils.hasText(request.getFollowerLoginId())) {
            errors.add(ValidationError.of("followerLoginId", "AskFollowRequest.followerLoginId must not be null."));
        }

        return errors;
    }
}
