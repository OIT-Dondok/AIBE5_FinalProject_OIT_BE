package com.oit.dondok.global.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class CookieLocalProfileTest {

  @Autowired private CookieProperties cookieProperties;

  @Test
  @DisplayName("로컬 환경 설정: SameSite=Lax, Secure=false 여야 한다")
  void localConfigCheck() {
    assertThat(cookieProperties.sameSite()).isEqualTo("Lax");
    assertThat(cookieProperties.secure()).isFalse();
  }
}
