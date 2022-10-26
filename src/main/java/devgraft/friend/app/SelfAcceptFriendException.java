package devgraft.friend.app;

import devgraft.support.exception.AbstractRequestException;
import devgraft.support.exception.StatusConstant;

public class SelfAcceptFriendException  extends AbstractRequestException {
    public SelfAcceptFriendException() {
        super("친구 요청자가 승인할 수 없습니다.", StatusConstant.BAD_REQUEST);
    }
}
