package com.origeek.imagePicker.util;

import java.io.InputStream;

public class WebpUtil {

    public static boolean isWebpAnimated(InputStream in) {
        boolean result = false;
        try {
            long l01 = in.skip(12);
            byte[] buf = new byte[4];
            int i = in.read(buf);
            if ("VP8X".equals(new String(buf, 0, i))) {
                long l02 = in.skip(12);
                result = (in.read(buf) == 4 && (buf[3] & 0x00000002) != 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
