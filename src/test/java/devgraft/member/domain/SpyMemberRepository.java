package devgraft.member.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpyMemberRepository implements MemberRepository {
    public Long nextIdx = 1L;
    public final Map<Long, Member> data = new HashMap<>();

    @Override
    public Optional<Member> findByIdx(Long idx) {
        return Optional.ofNullable(data.get(nextIdx));
    }

    @Override
    public void save(Member member) {
        data.put(nextIdx++, member);
    }
}
