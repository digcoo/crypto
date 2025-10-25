package com.digcoo.fitech.backtest.service;

import com.digcoo.fitech.backtest.param.req.BackTestReqParam;
import com.digcoo.fitech.backtest.param.res.BackTestResult;

public interface BackTestService {

    BackTestResult runBackTest(BackTestReqParam param);
}
