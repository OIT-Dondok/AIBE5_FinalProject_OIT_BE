package com.oit.dondok.infra.image.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.oit.dondok.domain.mission.port.ImageMetadata;
import com.oit.dondok.global.exception.CustomException;
import com.oit.dondok.infra.image.exception.ImageErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ExtendWith(MockitoExtension.class)
class S3ImageMetadataAdapterTest {

  // SHA-256("") 표준 테스트 벡터.
  private static final String EMPTY_SHA256 =
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

  @Mock private S3Client s3Client;

  @InjectMocks private S3ImageMetadataAdapter adapter;

  @BeforeEach
  void setBucket() {
    ReflectionTestUtils.setField(adapter, "bucket", "dondok-test-bucket");
  }

  // 빈 바이트의 SHA-256은 표준 벡터와 일치하고, EXIF가 없으면 takenAt은 null이다.
  @Test
  void extractComputesSha256AndReturnsNullTakenAtWhenNoExif() {
    givenObjectBytes(new byte[0]);

    ImageMetadata metadata = adapter.extract("mission/1/1/empty");

    assertThat(metadata.sha256()).isEqualTo(EMPTY_SHA256);
    assertThat(metadata.takenAt()).isNull();
  }

  // EXIF가 없는 정상 jpeg는 takenAt이 null이고 해시는 64자 hex로 계산된다.
  @Test
  void extractReturnsNullTakenAtForImageWithoutExif() throws Exception {
    givenObjectBytes(jpegBytesWithoutExif());

    ImageMetadata metadata = adapter.extract("mission/1/1/plain");

    assertThat(metadata.takenAt()).isNull();
    assertThat(metadata.sha256()).matches("[0-9a-f]{64}");
  }

  // S3에 객체가 없으면 IMAGE_NOT_FOUND로 변환된다.
  @Test
  void extractThrowsImageNotFoundWhenObjectMissing() {
    given(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .willThrow(NoSuchKeyException.builder().build());

    assertThatThrownBy(() -> adapter.extract("mission/1/1/missing"))
        .isInstanceOf(CustomException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  private void givenObjectBytes(byte[] bytes) {
    given(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
        .willReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), bytes));
  }

  private static byte[] jpegBytesWithoutExif() throws IOException {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", os);
    return os.toByteArray();
  }
}
