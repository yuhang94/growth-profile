package io.growth.platform.profile.domain.template;

import io.growth.platform.profile.api.dto.EventTemplateDTO;
import io.growth.platform.profile.api.enums.EventType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BuiltInTemplateProvider {

    private final Map<EventType, List<EventTemplateDTO>> templates = new EnumMap<>(EventType.class);

    @PostConstruct
    public void init() {
        templates.put(EventType.ORDER, List.of(orderTemplate()));
        templates.put(EventType.PAGE_VIEW, List.of(pageViewTemplate()));
        templates.put(EventType.LOGIN, List.of(loginTemplate()));
        templates.put(EventType.SEARCH, List.of(searchTemplate()));
        templates.put(EventType.CLICK, List.of(clickTemplate()));
        templates.put(EventType.SHARE, List.of(shareTemplate()));
    }

    public List<EventTemplateDTO> getTemplates(EventType eventType) {
        return templates.getOrDefault(eventType, Collections.emptyList());
    }

    private EventTemplateDTO orderTemplate() {
        return new EventTemplateDTO(null, EventType.ORDER, "标准订单消息",
                "包含用户、商品、支付信息的订单事件",
                """
                {
                  "userId": 10001,
                  "shopCode": "SHOP_001",
                  "orderProductDataList": [
                    {
                      "productId": 20001,
                      "productName": "示例商品",
                      "quantity": 2
                    }
                  ],
                  "orderPayData": {
                    "totalPrice": 9900
                  },
                  "orderTime": "2024-01-15 10:30:00"
                }""");
    }

    private EventTemplateDTO pageViewTemplate() {
        return new EventTemplateDTO(null, EventType.PAGE_VIEW, "页面浏览",
                "用户浏览页面行为事件",
                """
                {
                  "userId": 10001,
                  "pageUrl": "/product/detail/20001",
                  "pageTitle": "商品详情页",
                  "referrer": "/search?q=手机",
                  "duration": 15000,
                  "eventTime": "2024-01-15 10:30:00"
                }""");
    }

    private EventTemplateDTO loginTemplate() {
        return new EventTemplateDTO(null, EventType.LOGIN, "用户登录",
                "用户登录行为事件",
                """
                {
                  "userId": 10001,
                  "loginMethod": "PASSWORD",
                  "deviceType": "MOBILE",
                  "ip": "192.168.1.1",
                  "eventTime": "2024-01-15 10:30:00"
                }""");
    }

    private EventTemplateDTO searchTemplate() {
        return new EventTemplateDTO(null, EventType.SEARCH, "搜索行为",
                "用户搜索行为事件",
                """
                {
                  "userId": 10001,
                  "keyword": "手机壳",
                  "resultCount": 42,
                  "category": "配件",
                  "eventTime": "2024-01-15 10:30:00"
                }""");
    }

    private EventTemplateDTO clickTemplate() {
        return new EventTemplateDTO(null, EventType.CLICK, "元素点击",
                "用户点击页面元素行为事件",
                """
                {
                  "userId": 10001,
                  "elementId": "btn_add_cart",
                  "elementType": "BUTTON",
                  "pageUrl": "/product/detail/20001",
                  "eventTime": "2024-01-15 10:30:00"
                }""");
    }

    private EventTemplateDTO shareTemplate() {
        return new EventTemplateDTO(null, EventType.SHARE, "内容分享",
                "用户分享内容行为事件",
                """
                {
                  "userId": 10001,
                  "shareChannel": "WECHAT",
                  "contentType": "PRODUCT",
                  "contentId": "20001",
                  "eventTime": "2024-01-15 10:30:00"
                }""");
    }
}
