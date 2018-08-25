package com.imooc.order.dataobject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class OrderMaster {
    @Id
    private String orderId; //订单id
    private String buyerName;   //买家名字
    private String buyerPhone;  //买家手机号
    private String buyerAddress;    //买家地址
    private String buyerOpenid; //买家微信Openid
    private BigDecimal orderAmount; //买家总金额
    private Integer orderStatus;    //订单状态，默认为新下单
    private Integer payStatus; //支付状态，默认未支付
    private Date createTime;    //创建时间
    private Date updateTime;    //更新时间
}
