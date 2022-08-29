package devgraft.member.domain;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface MemberRepository extends Repository<Member, Long> {
    boolean existsByLoggedId(String loginId);
    Optional<Member> findById(Long id);
    void save(Member member);
}
