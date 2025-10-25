package com.digcoo.fitech.backtest.controller;

import com.digcoo.fitech.backtest.param.req.BackTestReqParam;
import com.digcoo.fitech.backtest.param.res.BackTestResult;
import com.digcoo.fitech.backtest.service.BackTestService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backtest")
public class BackTestController {

    @Resource
    private BackTestService backTestService;

    @GetMapping("/execute")
    public ResponseEntity<BackTestResult> doExecute(BackTestReqParam param) {
        return ResponseEntity.ok(backTestService.runBackTest(param));
    }

}
