package devgraft.module.member.app;

import devgraft.module.member.domain.MemberCryptService;
import devgraft.module.member.domain.MemberRepository;
import devgraft.module.member.domain.Password;
import devgraft.support.exception.ValidationError;
import devgraft.support.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MembershipServiceTest {
    private MembershipService membershipService;
    private DecryptMembershipRequestProvider decryptMembershipRequestProvider;
    private MemberCryptService memberCryptService;
    private DecryptMembershipRequestValidator decryptMembershipRequestValidator;
    private MemberRepository memberRepository;
    private ProfileImageProvider profileImageProvider;
    @BeforeEach
    void setUp() {
        decryptMembershipRequestProvider = mock(DecryptMembershipRequestProvider.class);
        memberCryptService = mock(MemberCryptService.class);
        decryptMembershipRequestValidator = mock(DecryptMembershipRequestValidator.class);
        memberRepository = mock(MemberRepository.class);
        profileImageProvider = mock(ProfileImageProvider.class);

        given(decryptMembershipRequestProvider.from(any(), any())).willReturn(new DecryptMembershipRequest("", "", "", ""));
        given(memberRepository.existsById(any())).willReturn(true);
        given(memberCryptService.hashingPassword(any())).willReturn(Password.from(""));
        given(profileImageProvider.create()).willReturn("url");


        membershipService = new MembershipService(decryptMembershipRequestProvider, memberCryptService,
                decryptMembershipRequestValidator, memberRepository, profileImageProvider);
    }

    @DisplayName("회원가입 요청이 입력 조건과 맞지 않는 것이 있다면 에러")
    @Test
    void membershipRequestHasError() {
        final EncryptMembershipRequest givenRequest = new EncryptMembershipRequest("", "", "", "");
        given(decryptMembershipRequestValidator.validate(any())).willReturn(List.of(ValidationError.of("field", "message")));

        final ValidationException validationException = catchThrowableOfType(
                () -> membershipService.membership(givenRequest, null),
                ValidationException.class);

        assertThat(validationException).isNotNull();
        assertThat(validationException.getErrors()).isNotNull();
        assertThat(validationException.getErrors()).isNotEmpty();
        assertThat(validationException.getErrors().get(0).getField()).isEqualTo("field");
        assertThat(validationException.getErrors().get(0).getMessage()).isEqualTo("message");
    }

    @DisplayName("회원가입 요청의 아이디가 이미 존재할 경우 에러")
    @Test
    void existsMemberByIdHasError() {
        final EncryptMembershipRequest givenRequest = new EncryptMembershipRequest("", "", "", "");
        given(memberRepository.existsById(any())).willReturn(true);

        assertThrows(AlreadyExistsMemberIdException.class, () ->
                membershipService.membership(givenRequest, null));

        verify(memberRepository, times(1)).existsById(any());
    }

    @DisplayName("회원가입 저장 호출")
    @Test
    void membershipWasCallOfMemberRepository_save() {
        final EncryptMembershipRequest givenRequest = new EncryptMembershipRequest("", "", "", "");
        given(memberRepository.existsById(any())).willReturn(false);

        membershipService.membership(givenRequest, null);

        verify(memberRepository, times(1)).save(any());
    }
}