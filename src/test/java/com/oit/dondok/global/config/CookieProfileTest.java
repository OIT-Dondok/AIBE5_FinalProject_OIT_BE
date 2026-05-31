package com.oit.dondok.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class CookieProfileTest {

  // 테스트를 위한 가벼운 설정 클래스
  @Configuration
  @EnableConfigurationProperties(CookieProperties.class)
  static class TestConfig {}

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

  @Test
  @DisplayName("로컬 설정: SameSite=Lax, Secure=false 확인")
  void localConfigCheck() {
    contextRunner
        .withPropertyValues(
            "spring.profiles.active=local", "app.cookie.secure=false", "app.cookie.same-site=Lax")
        .run(
            context -> {
              CookieProperties props = context.getBean(CookieProperties.class);
              assertThat(props.sameSite()).isEqualTo("Lax");
              assertThat(props.secure()).isFalse();
            });
  }

  @Test
  @DisplayName("운영 설정: SameSite=None, Secure=true 확인")
  void prodConfigCheck() {
    contextRunner
        .withPropertyValues(
            "spring.profiles.active=prod", "app.cookie.secure=true", "app.cookie.same-site=None")
        .run(
            context -> {
              CookieProperties props = context.getBean(CookieProperties.class);
              assertThat(props.sameSite()).isEqualTo("None");
              assertThat(props.secure()).isTrue();
            });
  }
}
