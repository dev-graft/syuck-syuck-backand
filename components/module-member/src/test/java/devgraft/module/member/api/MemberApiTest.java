package devgraft.module.member.api;

import devgraft.module.member.app.EncryptMembershipRequest;
import devgraft.module.member.app.GenerateCryptoKeyService;
import devgraft.module.member.app.MembershipService;
import devgraft.module.member.query.MemberData;
import devgraft.module.member.query.MemberDataDao;
import devgraft.support.crypto.RSA;
import devgraft.support.mapper.ObjectMapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.KeyPair;
import java.util.Objects;
import java.util.Optional;

import static devgraft.module.member.api.MemberApi.KEY_PAIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberApiTest extends ObjectMapperTest {
    private MockMvc mockMvc;
    private MembershipService membershipService;
    private GenerateCryptoKeyService generateCryptoKeyService;
    private MemberDataDao memberDataDao;

    @BeforeEach
    void setUp() {
        membershipService = mock(MembershipService.class);
        generateCryptoKeyService = mock(GenerateCryptoKeyService.class);
        memberDataDao = mock(MemberDataDao.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new MemberApi(membershipService, generateCryptoKeyService, memberDataDao))
                .build();
    }

    @DisplayName("회원가입 요청")
    @Test
    void membership() throws Exception {
        final KeyPair keyPair = RSA.generatedKeyPair();
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(KEY_PAIR, keyPair);
        final EncryptMembershipRequest givenRequest = new EncryptMembershipRequest(
                "loginId", "password", "nickname", "profileImage"
        );

        mockMvc.perform(post("/api/members/membership")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(getObjectMapper().writeValueAsString(givenRequest))
                        .session(mockHttpSession))
                .andExpect(status().isCreated());

        verify(membershipService).membership(refEq(givenRequest), refEq(keyPair));
    }

    @DisplayName("공개키 요청")
    @Test
    void getPubKey() throws Exception {
        final MockHttpSession mockHttpSession = new MockHttpSession();
        given(generateCryptoKeyService.process()).willReturn(RSA.generatedKeyPair());

        mockMvc.perform(get("/api/members/code")
                .session(mockHttpSession))
                .andExpect(status().isOk());

        assertThat(mockHttpSession.getAttribute(KEY_PAIR)).isNotNull();
    }

    @DisplayName("아이디 존재 여부 검증")
    @Test
    void existsLoginId() throws Exception {
        given(memberDataDao.findOne(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/members/check/{loginId}", "loginId"))
                .andExpect(status().isOk())
                .andExpect(result -> Objects.equals(result, false));
    }

//    @DisplayName("회원 조회 요청 / 회원이 존재하지 않을 경우 예외처리")
//    @Test
//    void getMemberProfile_NotFoundMemberHasError() throws Exception {
//        given(memberDataDao.findOne(any())).willReturn(Optional.empty());
//
//        mockMvc.perform(get("/api/members/profile/{loginId}", "loginId"))
//                .andExpect((result ->
//                        assertTrue(result.getResolvedException().getClass().isAssignableFrom(NotFoundMemberException.class))));
//    }

    @DisplayName("회원 조회 요청")
    @Test
    void getMemberProfile() throws Exception {
        final MemberData givenMemberData = MemberData.builder()
                .memberId("loginId")
                .nickname("nickname")
                .profileImage("profileImage")
                .stateMessage("stateMessage")
                .build();
        given(memberDataDao.findOne(any())).willReturn(Optional.of(givenMemberData));

        mockMvc.perform(get("/api/members/{loginId}", "loginId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId", equalTo(givenMemberData.getMemberId())))
                .andExpect(jsonPath("$.nickname", equalTo(givenMemberData.getNickname())))
                .andExpect(jsonPath("$.profileImage", equalTo(givenMemberData.getProfileImage())))
                .andExpect(jsonPath("$.stateMessage", equalTo(givenMemberData.getStateMessage())))
                .andDo(print());
    }
}