package com.imooc.order.dto;

import com.imooc.order.dataobject.OrderDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO:数据传输对象
 */
@Data
public class OrderDTO {
    private String orderId; //订单id
    private String buyerName;   //买家名字
    private String buyerPhone;  //买家手机号
    private String buyerAddress;    //买家地址
    private String buyerOpenid; //买家微信Openid
    private BigDecimal orderAmount; //买家总金额
    private Integer orderStatus;    //订单状态，默认为新下单
    private Integer payStatus; //支付状态，默认未支付

    private List<OrderDetail> orderDetailList;
}
