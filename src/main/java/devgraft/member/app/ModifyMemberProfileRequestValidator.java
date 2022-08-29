package devgraft.member.app;

import devgraft.support.exception.ValidationError;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static devgraft.member.app.MemberPatterns.NICKNAME_ID_PATTERN;

@Component
public class ModifyMemberProfileRequestValidator {
    public List<ValidationError> validate(final ModifyMemberProfileRequest request) {
        final List<ValidationError> errors = new ArrayList<>();
        if (StringUtils.hasText(request.getNickname()) && !NICKNAME_ID_PATTERN.matcher(request.getNickname()).matches()) {
            errors.add(ValidationError.of("nickname", "ModifyMemberProfileRequest.nickname pattern don't match."));
        }
        return errors;
    }
}
