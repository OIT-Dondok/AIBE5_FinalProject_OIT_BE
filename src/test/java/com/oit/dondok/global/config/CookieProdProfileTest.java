package com.oit.dondok.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // JUnit 5로 변경
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("prod")
class CookieProdProfileTest {

  @Autowired private CookieProperties cookieProperties;

  @Test
  @DisplayName("운영 환경 설정: SameSite=None, Secure=true 여야 한다")
  void prodConfigCheck() {
    assertThat(cookieProperties.sameSite()).isEqualTo("None");
    assertThat(cookieProperties.secure()).isTrue();
  }
}
