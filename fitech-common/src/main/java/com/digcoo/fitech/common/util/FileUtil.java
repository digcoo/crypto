package com.digcoo.fitech.common.util;

import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.model.CandlestickSnapshot;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@Slf4j
public final class FileUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(50_000_000) // 50MB
                .build();
        objectMapper.getFactory().setStreamReadConstraints(constraints);
    }

    public static void saveSnapshot(List<CandlestickSnapshot> snapshotData) throws IOException {
        long startTime = System.currentTimeMillis();
        File snapshotFile = new File(GlobalConstants.SNAPSHOT_FILE);
        if (!snapshotFile.getParentFile().exists()) {
            snapshotFile.getParentFile().mkdirs();
        }
        objectMapper.writeValue(snapshotFile, snapshotData);
        log.info("saveSnapshot cost: {}ms", System.currentTimeMillis() - startTime);
    }

    public static List<CandlestickSnapshot> loadSnapshot() throws IOException {
        long startTime = System.currentTimeMillis();
        File snapshotFile = new File(GlobalConstants.SNAPSHOT_FILE);
        if (!snapshotFile.exists()) {
            log.warn("snapshot not exist: " + snapshotFile.getAbsolutePath());
            return Collections.emptyList();
        }

        // 从JSON文件读取数据列表
        List<CandlestickSnapshot> snapshotData =  objectMapper.readValue(
                snapshotFile,
                objectMapper.getTypeFactory().constructCollectionType(List.class, CandlestickSnapshot.class)
        );

        log.info("loadSnapshot cost: {}ms", System.currentTimeMillis() - startTime);

        return snapshotData;

    }


    public static void saveSnapshotGz(List<CandlestickSnapshot> snapshotData) throws IOException {
        long startTime = System.currentTimeMillis();
        try (FileOutputStream fos = new FileOutputStream(GlobalConstants.SNAPSHOT_GZ_FILE);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
                objectMapper.writeValue(gzos, snapshotData);
            log.info("saveSnapshotGz cost: {}ms", System.currentTimeMillis() - startTime);
        }
    }

    public static List<CandlestickSnapshot> loadSnapshotGz() throws IOException {
        long startTime = System.currentTimeMillis();
        try (FileInputStream fis = new FileInputStream(GlobalConstants.SNAPSHOT_GZ_FILE);
             GZIPInputStream gzis = new GZIPInputStream(fis)) {
            List<CandlestickSnapshot> snapshotData = objectMapper.readValue(
                    gzis,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CandlestickSnapshot.class)
            );

            log.info("loadSnapshotGz cost: {}ms", System.currentTimeMillis() - startTime);
            return snapshotData;
        }

    }

}
