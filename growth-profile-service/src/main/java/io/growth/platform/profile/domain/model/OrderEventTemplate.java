package io.growth.platform.profile.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class OrderEventTemplate {

    private Long userId;

    private String shopCode;

    private List<OrderProductData> orderProductDataList;

    private OrderPayData orderPayData;


    @Data
    static class OrderProductData {

        private Long productId;

        private String productName;

        private Integer quantity;
    }

    @Data
    static class OrderPayData {

        private Long totalPrice;
    }

}
