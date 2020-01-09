package MD5test;

import cn.kim.util.AESUtil;
import org.junit.Test;

import java.security.InvalidKeyException;

/**
 * Created by 余庚鑫 on 2019/12/27
 */
public class AESTest {

    @Test
    public void AESTest() {
        try {
            String e = AESUtil.encode("52259024812376064","49cd2059-b7e7-435b-9b4a-b6bc8d115981");
            System.out.println(e);
            String d = AESUtil.dncode(e,"49cd2059-b7e7-435b-9b4a-b6bc8d115981");
            System.out.println(d);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
