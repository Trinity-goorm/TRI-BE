package com.trinity.ctc.domain.user.validator;

import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.status.UserStatus;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {
    public void validateUserStatus(User user) {
        if(user.getStatus() != UserStatus.TEMPORARILY_UNAVAILABLE) throw new CustomException(UserErrorCode.NOT_TEMPORAL_USER);
    }

    public void validateCategorySelection(int selectCount) {
        if (selectCount != 3) throw new CustomException(UserErrorCode.INVALID_CATEGORY_COUNT);
    }
}