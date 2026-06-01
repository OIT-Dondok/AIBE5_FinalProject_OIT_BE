package com.oit.dondok.infra.image.service;

import com.oit.dondok.global.exception.CustomException;
import com.oit.dondok.infra.image.dto.PresignedUrlRequest;
import com.oit.dondok.infra.image.dto.PresignedUrlResponse;
import com.oit.dondok.infra.image.exception.ImageErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class ImageService {
  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  @Value("${app.aws.s3.bucket}")
  private String bucket;

  public PresignedUrlResponse generatePresignedUrl(UUID memberUuid, PresignedUrlRequest request) {
    // presigned URL 발급은 특정 S3 namespace에 대한 업로드 권한 위임이므로, 발급 전에 요청자가
    // 해당 crew/participant namespace에 업로드할 자격이 있는지 반드시 검증한다.
    verifyUploadPermission(memberUuid, request);

    // S3에 저장될 object key 생성. 클라이언트가 key를 지정하지 못하도록 서버가 생성한다.
    // key 형식: mission/{crewId}/{crewParticipantId}/{uuid}
    String objectKey =
        String.format(
            "mission/%d/%d/%s", request.crewId(), request.crewParticipantId(), UUID.randomUUID());

    // S3에 PUT 요청을 허용하는 서명된 URL 생성 요청
    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10)) // URL 유효시간 10분, 만료 후 업로드불가
            .putObjectRequest(r -> r.bucket(bucket).key(objectKey).contentType("image/jpeg"))
            .build();

    // S3가 서명된 URL 반환
    String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

    return PresignedUrlResponse.of(presignedUrl, objectKey);
  }

  // TODO(보안): 업로드 권한(소유권) 검증 미구현. CrewParticipantRepository가 이 브랜치에 머지되면 채운다.
  // - crewParticipantRepository.findById(request.crewParticipantId())로 참여자 조회
  // - participant.getCrew().getId() == request.crewId() 인지 확인
  // - participant.getMember().getUuid() == memberUuid 인지 확인 (요청자 소유 검증)
  // 위반 시 권한 예외(예: PARTICIPANT_NOT_FOUND)로 차단한다.
  // 현재는 crew 도메인 의존성이 없어 통과시키며, 머지 전 반드시 구현해야 하는 IDOR 위험 지점이다.
  private void verifyUploadPermission(UUID memberUuid, PresignedUrlRequest request) {
    // 의도적으로 미구현 상태로 둔다. 위 TODO 참고.
  }

  public void reEncodeImage(String objectKey) {
    // try-with-resources로 S3 InputStream과 출력 스트림을 닫는다.
    try (InputStream inputStream = downloadImage(objectKey);
        ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      // InputStream을 BufferedImage로 변환
      BufferedImage image = ImageIO.read(inputStream);
      if (image == null) {
        throw new CustomException(ImageErrorCode.IMAGE_READ_FAILED);
      }

      // JPG로 재인코딩 (Exif 메타데이터 자동 제거)
      if (!ImageIO.write(image, "jpg", os)) {
        throw new CustomException(ImageErrorCode.IMAGE_ENCODE_FAILED);
      }

      // 정제본을 같은 objectKey로 S3에 덮어쓰기
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(objectKey)
              .contentType("image/jpeg")
              .build(),
          RequestBody.fromBytes(os.toByteArray()));
    } catch (NoSuchKeyException e) {
      // S3에 원본 객체가 없는 경우(404/NoSuchKey)
      throw new CustomException(ImageErrorCode.IMAGE_NOT_FOUND);
    } catch (IOException e) {
      throw new CustomException(ImageErrorCode.IMAGE_ENCODE_FAILED);
    }
  }

  // S3에서 원본 이미지 스트림 다운로드
  private InputStream downloadImage(String objectKey) {
    return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build());
  }
}
