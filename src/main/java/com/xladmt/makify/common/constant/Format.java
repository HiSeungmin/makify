package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Format {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    BMP("bmp"),
    WEBP("webp"),
    SVG("svg"),
    MP4("mp4"),
    MOV("mov"),
    AVI("avi"),
    MP3("mp3"),
    WAV("wav"),
    PDF("pdf");

    private final String extension;
}
