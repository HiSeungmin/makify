package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Verifination_Method {
    CAMERA("카메라 허용"),
    ALBUM("앨범만 허용"),
    ALL("카메라, 앨범 허용");

    private final String description;
}
