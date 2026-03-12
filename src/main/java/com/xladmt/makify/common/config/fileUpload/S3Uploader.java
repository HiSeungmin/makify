package com.xladmt.makify.common.config.fileUpload;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket.name}")
    private String bucket;

    /**
     * S3에 파일 업로드 후 Presigned URL 반환
     *
     * @param file   업로드할 파일
     * @param folder S3 저장 경로 (예: "verification/42/17")
     * @return Presigned URL (1시간 유효)
     */
    public String upload(MultipartFile file, String folder) throws IOException {
        String key = buildKey(folder, file.getOriginalFilename());
        putObject(file, key);
        log.info("[S3Uploader] 업로드 완료 - key: {}", key);
        return getPublicUrl(key);
    }

    /**
     * S3에 파일 업로드 후 key 반환 (롤백 처리용)
     *
     * @param file   업로드할 파일
     * @param folder S3 저장 경로
     * @return S3 key
     */
    public String uploadAndGetKey(MultipartFile file, String folder) throws IOException {
        String key = buildKey(folder, file.getOriginalFilename());
        putObject(file, key);
        log.info("[S3Uploader] 업로드 완료 - key: {}", key);
        return key;
    }

    private void putObject(MultipartFile file, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(
                new PutObjectRequest(bucket, key, file.getInputStream(), metadata)
        );
    }

    /**
     * S3 파일 삭제
     *
     * @param key 삭제할 파일의 S3 key
     */
    public void delete(String key) {
        amazonS3Client.deleteObject(bucket, key);
        log.info("[S3Uploader] 삭제 완료 - key: {}", key);
    }

    /**
     * 영구 퍼블릭 URL 반환
     *
     * @param key S3 key
     * @return 영구 접근 URL
     */
    public String getPublicUrl(String key) {
        return amazonS3Client.getUrl(bucket, key).toString();
    }

    /**
     * S3 Key 생성
     * 형식: {folder}/{uuid}_{originalFilename}
     */
    private String buildKey(String folder, String originalFilename) {
        return folder + "/" + UUID.randomUUID() + "_" + originalFilename;
    }
}
