package com.digcoo.fitech.stock;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.http.XueQiuHttpClient;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;

public class XueQiuHttpClientTest {


    @Test
    public void getPageTest() {
        XueQiuHttpClient xueQiuHttpClient = new XueQiuHttpClient(HttpClient.newHttpClient());
        List<Candlestick> candlesticks = xueQiuHttpClient.getHistoryCandles("sh688313", CandlestickPeriod.DAILY, 500);
    }

    @Test
    public void getCookieTest() throws IOException, InterruptedException {
        XueQiuHttpClient xueQiuHttpClient = new XueQiuHttpClient(HttpClient.newHttpClient());
        System.out.println(xueQiuHttpClient.getCookie());
    }


    @Test
    public void getCookie() {

        String url = "https://xueqiu.com";

        // 设置ChromeDriver路径
//        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");

        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        try {
            driver.get(url);

            // 获取所有Cookie
            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie cookie : cookies) {
                System.out.println("Cookie: " + cookie.getName() + "=" + cookie.getValue());
            }
        } finally {
            driver.quit();
        }
    }

}
