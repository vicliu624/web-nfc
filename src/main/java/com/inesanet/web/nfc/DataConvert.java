package com.inesanet.web.nfc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @Auther: liuweikai
 * @Date: 2019-06-22 19:42
 * @Description:
 */
public class DataConvert {

    public static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    public static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
    private static final String hexDigits[] = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

    public static byte[] hexStringToBytes(String strData)
    {
        return hexStringToBytes(strData, strData.length());
    }

    public static byte[] hexStringToBytes(String strData, int iLen)
    {
        if(strData.length() < iLen)
            throw new IllegalArgumentException("strData Length too short");
        if(iLen % 2 != 0)
            throw new IllegalArgumentException("Length illegal");
        byte[] bData = new byte[iLen / 2];
        for (int i = 0; i < iLen / 2;i++)
        {
            int iTemp = Integer.parseInt(strData.substring(i * 2,i * 2 + 2), 16);
            bData[i] = (byte)(iTemp & 0x000000FF);
        }
        return bData;
    }

    public static byte[] utf8ToGBK(String utf8Str) throws UnsupportedEncodingException {
        String utf8 = new String(utf8Str.getBytes(StandardCharsets.UTF_8));
        System.out.println(utf8);
        String unicode = new String(utf8.getBytes(),StandardCharsets.UTF_8);
        System.out.println("gbk:" + byteArrayToHexString(unicode.getBytes("GBK")));
        return unicode.getBytes("GBK");
    }

    public static byte[] shortToBytes(short value) {
        byte[] bs = new byte[2];
        for (int i = 0; i < 2; i++) {
            bs[i] = (byte) ((value >> ((1 - i) * 8)) & 0xff);
        }
        return bs;
    }

    public static byte[] intToBytes(int value) {
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = (byte) ((value >> ((3 - i) * 8)) & 0xff);
        }
        return bs;
    }

    public static int bytesToInt(byte[] b) {
        int ret = 0;
        for (final byte a : b) {
            ret <<= 8;
            ret |= a & 0xFF;
        }
        return ret;
    }

    public static byte[] toLH(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }
}
