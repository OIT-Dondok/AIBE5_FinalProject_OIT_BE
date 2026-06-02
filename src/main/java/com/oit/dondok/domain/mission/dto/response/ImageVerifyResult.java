package com.oit.dondok.domain.mission.dto.response;

import com.oit.dondok.domain.mission.entity.ExifRisk;
import java.time.LocalDateTime;

// 서버가 원본 이미지에서 추출/판정한 risk signal 묶음
public record ImageVerifyResult(
    LocalDateTime takenAt, String imageHash, ExifRisk exifRisk, boolean duplicate) {}
