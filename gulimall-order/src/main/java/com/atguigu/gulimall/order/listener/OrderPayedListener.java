package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.properties.AlipayProperties;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderPayedListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayProperties alipayProperties;

    @PostMapping("/payed/notify")
    public String alipayedHandle(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> params = parameterMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));
        if (AlipaySignature.rsaCheckV2(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType())) {
            return orderService.handlePayResult(payAsyncVo);
        }
        return "error";

    }
}
