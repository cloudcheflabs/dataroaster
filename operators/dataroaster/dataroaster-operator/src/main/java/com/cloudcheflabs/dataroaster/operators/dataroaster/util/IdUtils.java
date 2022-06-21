package com.cloudcheflabs.dataroaster.operators.dataroaster.util;

import java.util.Base64;
import java.util.List;

public class IdUtils {

    /**
     * it is used to create new id with string list which consists of many column values which are unique.
     *
     * @param stringList
     * @return
     */
    public static String newId(List<String> stringList) {
        String bcrypted = BCryptUtils.encodeWithBCrypt(makeLine(stringList));
        String encodedString = Base64.getEncoder().encodeToString(bcrypted.getBytes());

        return encodedString;
    }

    private static String makeLine(List<String> stringList) {
        StringBuffer sb = new StringBuffer();
        int count = 0;
        int size = stringList.size();
        for(String str : stringList) {
            sb.append(str);
            count++;
            if(count < size) {
                sb.append(":");
            }
        }

        return sb.toString();
    }

    public static boolean isMatched(List<String> stringList, String base64EncodedId) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedId);
        String bcrypted = new String(decodedBytes);

        String original = makeLine(stringList);

        return BCryptUtils.isMatched(original, bcrypted);
    }
}
