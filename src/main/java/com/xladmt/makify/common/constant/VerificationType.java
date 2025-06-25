package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationType {
    CAMERA("카메라만 허용(앨범 불가)"),
    ALBUM("앨범만 허용(카메라 불가)"),
    ALL("카메라, 앨범 모두 허용");

    private final String description;
}
