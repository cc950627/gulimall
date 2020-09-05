package com.atguigu.gulimall.thirdparyt.component;

import com.atguigu.gulimall.thirdparyt.properties.OAuth2ComponentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OAuth2Component {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OAuth2ComponentProperties properties;

    public String getWeiboAccessToken(String code) {
        String url = String.format("%s/oauth2/access_token?client_id=%s&client_secret=%s&grant_type=%s&redirect_uri=%s&code=%s",
                properties.getWeiboUri(),
                properties.getClientId(),
                properties.getClientSecret(),
                properties.getGrantType(),
                properties.getRedirectUri(),
                code);
        return restTemplate.postForObject(url, null, String.class);
        //JSONObject jsonObject = restTemplate.postForObject("https://api.weibo.com/oauth2/access_token?client_id=813131548&client_secret=f461989f2f7ee32f0b2da625bbe5a872&grant_type=authorization_code&redirect_uri=http://auth.gulimall.com/oauth2/weibo/getAccessToken&code=" + code, null, JSONObject.class);
    }


    public String getWeiboUserInfo(String accessToken, String uid) {
        String url = String.format("%s/2/users/show.json?access_token=%s&uid=%s",
                properties.getWeiboUri(),
                accessToken,
                uid);
        return restTemplate.getForObject(url, String.class);
    }
}
