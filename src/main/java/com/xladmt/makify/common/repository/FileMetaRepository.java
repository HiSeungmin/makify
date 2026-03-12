package com.xladmt.makify.common.repository;

import com.xladmt.makify.common.constant.ImageType;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMetaRepository extends JpaRepository<FileMeta, Long> {

    List<FileMeta> findByReferenceIdAndTypeAndIsDeleted(Long referenceId, ImageType type, YN isDeleted);
}
