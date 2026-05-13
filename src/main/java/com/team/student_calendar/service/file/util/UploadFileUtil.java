package com.team.student_calendar.service.file.util;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class UploadFileUtil {

    private final Tika tika = new Tika();
    private final S3Properties s3Properties;



//    /**
//     * . 뒤에 있는 파일 확장자 가져오기
//     * */
//    public String getFileExtension(String originalFilename) {
//        if (!StringUtils.hasText(originalFilename)) {
//            return "";
//        }
//        String base = StringUtils.getFilename(originalFilename);
//        if (!StringUtils.hasText(base)) {
//            return "";
//        }
//        int dot = base.lastIndexOf('.');
//        if (dot < 0 || dot == base.length() - 1) {
//            return "";
//        }
//        return base.substring(dot).toLowerCase();
//    }

    /**
     * 파일의 MIME type 가져오기
     * @param file 파일
     * @return MIME type
     */
    public String checkFileTypeValidation(MultipartFile file) {

        /* mime 가져오기*/
        String mimeType = detectMimeType(file);

        /* 지원하는 확장자인지 체크*/
        if (mimeType.isBlank() || !s3Properties.getAllowedContentTypesSet().contains(mimeType.toLowerCase())) {
            throw new BaseException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        return mimeType;
    }


    /**
     * S3에 삽입하기 위한 메타데이터 만들기
     * @param fileSize 파일 길이
     * @param mimeType MIME type
     * @return objectMetadata
     */
    public ObjectMetadata makeMetaData(Long fileSize, String mimeType) {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        metadata.setContentType(mimeType);

        return metadata;
    }




    /**
     * Tika로 MIME 타입 판별<br/>
     * Tika는 파일 시작 부분의 byte magic pattern을 읽어서 MIME 타입을 확인한다
     * */
    private String detectMimeType(MultipartFile file) {

        String name = file.getOriginalFilename();

        try (InputStream in = file.getInputStream()) {
            return tika.detect(in, name).toLowerCase();
        } catch (IOException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }





}
