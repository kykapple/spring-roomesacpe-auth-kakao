package nextstep.auth;

import nextstep.auth.presentation.interceptor.AuthInterceptor;
import nextstep.auth.utils.JwtTokenProvider;
import nextstep.error.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @Test
    void 토큰이_존재하지_않을_경우_예외가_발생한다() {
        // given
        given(request.getHeader(HttpHeaders.AUTHORIZATION))
                .willReturn("");

        // when, then
        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, null))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void 토큰이_존재하지_않을_경우_예외가_발생한다2() {
        // given
        given(request.getHeader(HttpHeaders.AUTHORIZATION))
                .willReturn("Bearer ");

        // when, then
        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, null))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void 유효하지_않은_토큰일_경우_예외가_발생한다() {
        // given
        String invalidToken = "invalid-access-token";

        given(request.getHeader(HttpHeaders.AUTHORIZATION))
                .willReturn("Bearer " + invalidToken);
        given(jwtTokenProvider.validateToken(invalidToken))
                .willReturn(false);

        // when, then
        assertThatThrownBy(() -> authInterceptor.preHandle(request, response, null))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void 유효한_토큰일_경우_요청에_대한_로직이_수행된다() throws Exception {
        // given
        String token = "access-token";

        given(request.getHeader(HttpHeaders.AUTHORIZATION))
                .willReturn("Bearer " + token);
        given(jwtTokenProvider.validateToken(token))
                .willReturn(true);

        // when
        boolean result = authInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        verify(request, times(1)).setAttribute(eq("accessToken"), any(String.class));
    }

}
