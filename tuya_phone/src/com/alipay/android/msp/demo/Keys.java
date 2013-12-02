/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 */

package com.alipay.android.msp.demo;

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class Keys {

    // 合作商户ID，用签约支付宝账号登录www.alipay.com后，在商家服务页面中获取。
	public static final String DEFAULT_PARTNER = "2088011967169802";

    // 商户收款的支付宝账号
	public static final String DEFAULT_SELLER = "2088011967169802";

    // 商户（RSA）私钥
	public static final String PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAIi1aupPp8s0dEu8/W31rAtshcyg467Yutm0YnviBIsdR0ZO4CooymDNHAzYbSFQuaaUXaFQ91+NN9llRCpbrzVDPJA/vOagWg5cZoyeN5WLQUKd3ZcecYedLWev+P5avspds2/kxDZPf4QteGHLUdzIRSI6DaCnlpKUKwn1h3MJAgMBAAECgYBG7fG5/ngtm1qXjUfkTGnmR3koUXWwWyyfFdNfOUZoM1mupe/yj9Vo4iKim4wLlAmu3x2xPAQNJIUMm1kBrS467BiJJrdTUM1gFd2fwZ/D51zNb3PlcuKPlG0aa27BVqikAuLBst5p6KDC8dXUvnbWkwd1Rm3z11xJRMkmZ7/vDQJBAN+7s5tYBRzcgvRgAfyONXCCXJiFwRN7nFGws730c/yL1u2MN8J+rYFCpSNcdT9vtVB41/Dk2nm6lpLcYqJtFfsCQQCcbL5DA38PKoLGzqDcrPCnzIrJHFJ0/tRkfCWeLw6bd9dpgc7sJYgHo7HPT64GhbdGi3FacY1wrQ2PxUSAxv/LAkEAtbAzchKvzDlS62tQEa3WvfBPi9kK62x1jG1n+fmbe1qAhtZYrcy7z/20J5w85vArmRcZndnrhfE9uDPasbbOowJBAJboDbFm2RhPgbS8ONJMAStQl/+m1CHMiiia4Eu3yBbSSb2ennqRRqdgE7UVgKUIMSTcd7GyZKp0OOYfgJPf53sCQQDTtWJeo3uHaGeT8XlwqyrG0btX4v5SQcWvZze61S1LlrodumIsUgtLkFvyRHQm6RNndXHucEnJszlwOWJD3hwE";
    // 支付宝（RSA）公钥
	public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

}
