package com.inesanet.web.nfc;

/**
 * @Auther: liuweikai
 * @Date: 2019-12-31 21:31
 * @Description:
 */
public interface Error {
    String getCode();

    void setCode(String code);

    String getMessage();

    void setMessage(String message);
}
