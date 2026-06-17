package com.oit.dondok.infra.auth.oauth2;

import com.oit.dondok.domain.auth.exception.AuthErrorCode;
import com.oit.dondok.global.exception.CustomException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2RedirectProperties(String successRedirectUri, String failureRedirectUri) {

  public OAuth2RedirectProperties {
    if (successRedirectUri == null || successRedirectUri.isBlank()) {
      throw new CustomException(AuthErrorCode.OAUTH_SUCCESS_REDIRECT_URI_INVALID);
    }
    if (failureRedirectUri == null || failureRedirectUri.isBlank()) {
      throw new CustomException(AuthErrorCode.OAUTH_FAILURE_REDIRECT_URI_INVALID);
    }
  }
}
