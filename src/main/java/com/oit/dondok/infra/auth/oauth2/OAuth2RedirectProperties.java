package com.oit.dondok.infra.auth.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2RedirectProperties(String successRedirectUri, String failureRedirectUri) {

  public OAuth2RedirectProperties {
    if (successRedirectUri == null || successRedirectUri.isBlank()) {
      throw new IllegalArgumentException("app.oauth2.success-redirect-uri must not be blank");
    }
    if (failureRedirectUri == null || failureRedirectUri.isBlank()) {
      throw new IllegalArgumentException("app.oauth2.failure-redirect-uri must not be blank");
    }
  }
}
