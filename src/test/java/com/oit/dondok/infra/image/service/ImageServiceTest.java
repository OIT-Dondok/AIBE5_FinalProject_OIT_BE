package com.oit.dondok.infra.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.oit.dondok.global.exception.CustomException;
import com.oit.dondok.infra.image.dto.PresignedUrlRequest;
import com.oit.dondok.infra.image.dto.PresignedUrlResponse;
import com.oit.dondok.infra.image.exception.ImageErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

  @Mock private S3Presigner s3Presigner;

  @Mock private S3Client s3Client;

  @InjectMocks private ImageService imageService;

  @BeforeEach
  void setBucket() {
    ReflectionTestUtils.setField(imageService, "bucket", "dondok-test-bucket");
  }

  @Test
  void generatePresignedUrlReturnsServerGeneratedMissionKey() throws Exception {
    PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
    given(presigned.url()).willReturn(new URL("https://s3.example.com/upload"));
    given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presigned);

    PresignedUrlResponse response =
        imageService.generatePresignedUrl(new PresignedUrlRequest(42L, 101L));

    assertThat(response.uploadUrl()).isEqualTo("https://s3.example.com/upload");
    // key는 클라이언트가 아니라 서버가 mission/{crewId}/{crewParticipantId}/{uuid} 형식으로 생성한다.
    assertThat(response.s3Key()).matches("mission/42/101/[0-9a-fA-F-]{36}");
  }

  @Test
  void reEncodeImageReuploadsReEncodedImage() throws Exception {
    given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(s3Stream(sampleImageBytes()));

    imageService.reEncodeImage("mission/42/101/abc");

    // 재인코딩된 이미지가 같은 key로 다시 업로드된다.
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void reEncodeImageThrowsImageNotFoundWhenObjectMissing() {
    given(s3Client.getObject(any(GetObjectRequest.class)))
        .willThrow(NoSuchKeyException.builder().build());

    assertThatThrownBy(() -> imageService.reEncodeImage("mission/42/101/missing"))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  @Test
  void reEncodeImageThrowsImageReadFailedWhenObjectIsNotImage() {
    given(s3Client.getObject(any(GetObjectRequest.class)))
        .willReturn(s3Stream("not-an-image".getBytes(StandardCharsets.UTF_8)));

    assertThatThrownBy(() -> imageService.reEncodeImage("mission/42/101/broken"))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_READ_FAILED);
  }

  private static ResponseInputStream<GetObjectResponse> s3Stream(byte[] bytes) {
    return new ResponseInputStream<>(
        GetObjectResponse.builder().build(),
        AbortableInputStream.create(new ByteArrayInputStream(bytes)));
  }

  private static byte[] sampleImageBytes() throws IOException {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(image, "png", os);
    return os.toByteArray();
  }
}
