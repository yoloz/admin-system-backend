package indi.yolo.admin.system.commons.utils;

import cn.hutool.crypto.SecureUtil;
import net.dreamlu.mica.core.utils.HexUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class UtilsTest {

    @Test
    void printRipeMD160() {
        System.out.println(Utils.ripeMD160("test"));
    }

    @Test
    void printMD5() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update("test".getBytes(StandardCharsets.UTF_8));
            byte[] result = md.digest();
            System.out.println(HexUtil.encodeToString(result, true));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    void encrypt(){
        byte[] keys = "0123456789ABHAEQ".getBytes(StandardCharsets.UTF_8);
        System.out.println(SecureUtil.aes(keys).encryptHex("test"));
    }

}