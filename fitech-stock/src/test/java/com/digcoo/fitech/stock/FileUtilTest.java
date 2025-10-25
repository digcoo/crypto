package com.digcoo.fitech.stock;

import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.CandlestickSnapshot;
import com.digcoo.fitech.common.util.FileUtil;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FileUtilTest {


    @Test
    public void saveSnapshot() throws IOException {
        List<CandlestickSnapshot> snapshotList = new ArrayList<>();
        List<Candlestick> candlesticks = Lists.newArrayList(
                Candlestick.builder()
                        .symbol("sh688382")
                        .timestamp(System.currentTimeMillis())
                        .open(new BigDecimal(0.1))
                        .build()
        );
        snapshotList.add(CandlestickSnapshot.builder()
                        .symbol("sh688382")
                        .candlesticksMap(ImmutableMap.of("sh688382:day", candlesticks))
                .build());

        FileUtil.saveSnapshot(snapshotList);
    }

    @Test
    public void loadSnapshot() throws IOException {
        List<CandlestickSnapshot> snapshots = FileUtil.loadSnapshot();
        System.out.println(snapshots.size());
    }




    @Test
    public void saveSnapshotGz() throws IOException {
        List<CandlestickSnapshot> snapshotList = new ArrayList<>();
        List<Candlestick> candlesticks = Lists.newArrayList(
                Candlestick.builder()
                        .symbol("sh688382")
                        .timestamp(System.currentTimeMillis())
                        .open(new BigDecimal(0.1))
                        .build()
        );
        snapshotList.add(CandlestickSnapshot.builder()
                .symbol("sh688382")
                .candlesticksMap(ImmutableMap.of("sh688382:day", candlesticks))
                .build());

        FileUtil.saveSnapshotGz(snapshotList);
    }



    @Test
    public void loadSnapshotGz() throws IOException {
        List<CandlestickSnapshot> snapshots = FileUtil.loadSnapshotGz();
        System.out.println(snapshots.size());
    }
}
