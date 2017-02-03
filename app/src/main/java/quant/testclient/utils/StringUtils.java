package quant.testclient.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ldfs on 15/6/2.
 */
public class StringUtils {
    private final static String TAG=StringUtils.class.getSimpleName();
    /**
     * 验证数据是否是英文
     *
     * @param str
     * @return
     */
    public static boolean validateEnglish(String str) {
        return matcher("^[a-zA-Z]+$", str);
    }

    /**
     * 验证数据是否是 ip 地址
     *
     * @param str
     * @return
     */
    public static boolean validateAddress(String str) {
        return matcher("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})", str);
    }

    /**
     * 验证数据是否是英文
     *
     * @param str
     * @return
     */
    public static boolean validatePassW(String str) {
        return matcher("[\\da-zA-Z]{6,20}", str);
    }
    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

    // 完整的判断中文汉字和符号
    public static int hasChineseLength(String strName) {
        int size=0;
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                size++;
            }
        }
        return size;
    }

    /**
     * 对表情符，特殊字符进行过滤
     * @param str
     * @return
     */
    public static String emojiFilter(String str) {
        // 只允许字母和数字和中文//[\\pP‘’“”
        String regEx = "^[A-Za-z\\d\\u4E00-\\u9FA5\\p{P}‘’“”]+$";
        Pattern p = Pattern.compile(regEx);
        StringBuilder sb = new StringBuilder(str);

        for (int len = str.length(), i = len - 1; i>= 0; --i) {

            if (!Pattern.matches(regEx, String.valueOf(str.charAt(i)))) {
                sb.deleteCharAt(i);
            }
        }

        return sb.toString();
    }

    /**
     * 获取url地址中的主域名
     * @param url
     * @return
     */
    public static String getServerUrl(String url) {
        String source="";
        try {
            Pattern p = Pattern.compile("[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(url);
            if(matcher.find()) {
                source = matcher.group();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return source;
    }

    public static boolean isEmpty(String...arrays){
        boolean result=true;
        if(null!=arrays){
            for(int i=0;i<arrays.length;i++){
                if(!(result&=!TextUtils.isEmpty(arrays[i]))) break;
            }
        }
        return result;
    }



    /**
     * @param regex 正则表达式字符串
     * @param str   要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则‰返回 false;
     */
    public static boolean matcher(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean matcherMobileNumber(CharSequence value){
        boolean result=false;
        if(!TextUtils.isEmpty(value)){
            Matcher matcher = Pattern.compile("[1][3,4,5,7,8][0-9]{9}").matcher(value);
            result=matcher.matches();
        }
        return result;
    }



    public static String getEllipsizeValue(String value, int startCount, int endCount){
        StringBuilder builder=new StringBuilder(value.substring(0,startCount));
        for(int i=0;i<value.length()-startCount-endCount;builder.append("*"),i++);
        builder.append(value.substring(value.length()-endCount,value.length()));
        return builder.toString();
    }

    /**
     * 获得字符串中数值
     *
     * @param value
     * @return
     */
    public static int string2Int(String value) {
        int number = -1;
        if (!TextUtils.isEmpty(value)) {
            try {
                number = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                number = -1;
            }
        }
        return number;
    }

    /**
     * 获得字符串中数值
     *
     * @param value
     * @return
     */
    public static float string2Float(String value) {
        float number = -1;
        if (!TextUtils.isEmpty(value)) {
            try {
                number = Float.valueOf(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                number = -1;
            }
        }
        return number;
    }

}
