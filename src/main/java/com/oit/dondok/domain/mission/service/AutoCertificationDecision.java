package com.oit.dondok.domain.mission.service;

public record AutoCertificationDecision(boolean approved) {

  public static AutoCertificationDecision approve() {
    return new AutoCertificationDecision(true);
  }

  public static AutoCertificationDecision reject() {
    return new AutoCertificationDecision(false);
  }
}
