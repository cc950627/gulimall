package com.atguigu.gulimall.thirdparyt.component;

import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.thirdparyt.properties.SmsComponentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

@Component
public class SmsComponent {

    @Autowired
    private SmsComponentProperties properties;

    public void sendSms(String phone, String code) {
        //String host = "https://smsmsgs.market.alicloudapi.com";  // 【1】请求地址 支持http 和 https 及 WEBSOCKET
        //String path = "/sms/";  // 【2】后缀
        //String appcode = "你自己的AppCode"; // 【3】开通服务后 买家中心-查看AppCode
        //String sign = "175622";   //  【4】请求参数，详见文档描述
        //String skin = "1";  //  【4】请求参数，详见文档描述
        String urlSend = properties.getHost() + properties.getPath() + "?code=" + code + "&phone=" + phone + "&sign=" + properties.getSign() + "&skin=" + properties.getSkin();   // 【5】拼接请求链接
        try {
            URL url = new URL(urlSend);
            HttpURLConnection httpURLCon = (HttpURLConnection) url.openConnection();
            httpURLCon.setRequestProperty("Authorization", "APPCODE " + properties.getAppcode());// 格式Authorization:APPCODE (中间是英文空格)
            int httpCode = httpURLCon.getResponseCode();
            if (httpCode == 200) {
                String json = read(httpURLCon.getInputStream());
                System.out.println("正常请求计费(其他均不计费)");
                System.out.println("获取返回的json:");
                System.out.print(json);
                return;
            }
            Map<String, List<String>> map = httpURLCon.getHeaderFields();
            String error = map.get("X-Ca-Error-Message").get(0);
            if (httpCode == 400 && error.equals("Invalid AppCode `not exists`")) {
                System.out.println("AppCode错误 ");
            } else if (httpCode == 400 && error.equals("Invalid Url")) {
                System.out.println("请求的 Method、Path 或者环境错误");
            } else if (httpCode == 400 && error.equals("Invalid Param Location")) {
                System.out.println("参数错误");
            } else if (httpCode == 403 && error.equals("Unauthorized")) {
                System.out.println("服务未被授权（或URL和Path不正确）");
            } else if (httpCode == 403 && error.equals("Quota Exhausted")) {
                System.out.println("套餐包次数用完 ");
            } else {
                System.out.println("参数名错误 或 其他错误");
                System.out.println(error);
            }
        } catch (MalformedURLException e) {
            System.out.println("URL格式错误");
        } catch (UnknownHostException e) {
            System.out.println("URL地址错误");
        } catch (Exception e) {
            // 打开注释查看详细报错异常信息
            // e.printStackTrace();
        }
        throw new BizException(BizExceptionEnum.TP_SEND_CODE_ERROR, String.format("params{phone: %s, code: %s}", phone, code));
    }
    /*
     * 读取返回结果
     */
    private static String read(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = new String(line.getBytes(), "utf-8");
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

}
