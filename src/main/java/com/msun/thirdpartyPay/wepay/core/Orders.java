package com.msun.thirdpartyPay.wepay.core;

import static com.msun.thirdpartyPay.support.util.Preconditions.checkNotNullAndEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.msun.thirdpartyPay.support.json.Jsons;
import com.msun.thirdpartyPay.wepay.model.common.Coupon;
import com.msun.thirdpartyPay.wepay.model.enums.WepayField;
import com.msun.thirdpartyPay.wepay.model.order.WePayOrder;
import com.msun.thirdpartyPay.wepay.util.RandomStrs;

/**
 * 订单组件
 * 
 * @author zxc Apr 13, 2017 6:42:03 PM
 */
public final class Orders extends Component {

    /**
     * 查询订单
     */
    private static final String ORDER_QUERY = "https://api.mch.weixin.qq.com/pay/orderquery";

    /**
     * 关闭订单
     */
    private static final String ORDER_CLOSE = "https://api.mch.weixin.qq.com/pay/closeorder";

    Orders(Wepay wepay) {
        super(wepay);
    }

    /**
     * 根据微信订单号查询订单
     * 
     * @param transactionId 微信订单号
     * @return PayOrder对象，或抛WepayException
     */
    public WePayOrder queryByTransactionId(String transactionId) {
        checkNotNullAndEmpty(transactionId, "transactionId");
        Map<String, String> queryParams = new TreeMap<>();
        put(queryParams, WepayField.TRANSACTION_ID, transactionId);
        return doQueryOrder(queryParams);
    }

    /**
     * 根据商户订单号查询订单
     * 
     * @param outTradeNo 商户订单号
     * @return PayOrder对象，或抛WepayException
     */
    public WePayOrder queryByOutTradeNo(String outTradeNo) {
        checkNotNullAndEmpty(outTradeNo, "outTradeNo");
        Map<String, String> queryParams = new TreeMap<>();
        put(queryParams, WepayField.OUT_TRADE_NO, outTradeNo);
        return doQueryOrder(queryParams);
    }

    private WePayOrder doQueryOrder(Map<String, String> queryParams) {
        buildQueryParams(queryParams);
        Map<String, Object> orderData = doPost(ORDER_QUERY, queryParams);
        WePayOrder order = Jsons.DEFAULT.fromJson(Jsons.DEFAULT.toJson(orderData), WePayOrder.class);
        setCoupons(order, orderData);
        return order;
    }

    private void setCoupons(WePayOrder order, Map<String, Object> orderData) {
        if (order != null && order.getCouponCount() != null && order.getCouponCount() > 0) {
            List<Coupon> coupons = new ArrayList<>();
            Coupon coupon;
            for (int couponIndex = 0; couponIndex < order.getCouponCount(); couponIndex++) {
                coupon = Coupon.newCoupon((String) orderData.get(WepayField.COUPON_BATCH_ID + "_" + couponIndex), (String) orderData.get(WepayField.COUPON_ID + "_" + couponIndex),
                                          Integer.parseInt((String) orderData.get(WepayField.COUPON_FEE + "_" + couponIndex)));
                coupons.add(coupon);
            }
            order.setCoupons(coupons);
        }
    }

    /**
     * 关闭订单
     * 
     * @param outTradeNo 商户订单号
     * @return 关闭成功返回true，或抛WepayException
     */
    public Boolean closeOrder(String outTradeNo) {
        checkNotNullAndEmpty(outTradeNo, "outTradeNo");
        Map<String, String> closeParams = new TreeMap<>();
        put(closeParams, WepayField.OUT_TRADE_NO, outTradeNo);
        buildCloseParams(closeParams);
        return doPost(ORDER_CLOSE, closeParams) != null;
    }

    /**
     * 构建关闭订单参数
     * 
     * @param closeParams 关闭参数
     */
    private void buildCloseParams(Map<String, String> closeParams) {
        buildConfigParams(closeParams);
        put(closeParams, WepayField.NONCE_STR, RandomStrs.generate(16));
        buildSignParams(closeParams);
    }

    /**
     * 构建查询订单参数
     * 
     * @param queryParams 查询参数
     */
    private void buildQueryParams(Map<String, String> queryParams) {
        buildConfigParams(queryParams);
        put(queryParams, WepayField.NONCE_STR, RandomStrs.generate(16));
        buildSignParams(queryParams);
    }
}
