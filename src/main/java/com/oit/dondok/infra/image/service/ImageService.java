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

  public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
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

  public void reEncodeImage(String objectKey) {
    try {
      // InputStream을 BufferedImage로 변환
      BufferedImage image = ImageIO.read(downloadImage(objectKey));
      if (image == null) {
        throw new CustomException(ImageErrorCode.IMAGE_READ_FAILED);
      }

      // JPG로 재인코딩 (Exif 메타데이터 자동 제거)
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      boolean written = ImageIO.write(image, "jpg", os);
      if (!written) throw new CustomException(ImageErrorCode.IMAGE_ENCODE_FAILED);

      // 정제본을 같은 objectKey로 S3에 덮어쓰기
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(objectKey)
              .contentType("image/jpeg")
              .build(),
          RequestBody.fromBytes(os.toByteArray()));
    } catch (IOException e) {
      throw new CustomException(ImageErrorCode.IMAGE_ENCODE_FAILED);
    }
  }

  // S3에서 원본 이미지 스트림 다운로드
  private InputStream downloadImage(String objectKey) {
    return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build());
  }
}
