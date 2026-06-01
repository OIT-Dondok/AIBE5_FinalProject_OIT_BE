package com.oit.dondok.infra.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.oit.dondok.infra.image.dto.PresignedUrlRequest;
import com.oit.dondok.infra.image.dto.PresignedUrlResponse;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
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
}
