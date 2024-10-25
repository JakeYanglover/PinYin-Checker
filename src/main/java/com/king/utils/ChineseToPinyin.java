package com.king.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

public class ChineseToPinyin {

    private HanyuPinyinOutputFormat format;

    public ChineseToPinyin() {
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 无音调
        format.setVCharType(HanyuPinyinVCharType.WITH_V); // ü -> v
    }

    /**
     * 将给定的中文字符串转换为拼音
     *
     * @param chinese 中文字符串
     * @return 转换后的拼音字符串
     */
    public String convertToPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        try {
            for (int i = 0; i < chinese.length(); i++) {
                char ch = chinese.charAt(i);
                if (Character.toString(ch).matches("[\\u4E00-\\u9FA5]+")) {
                    // 汉字部分转换为拼音
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    if (pinyinArray != null) {
                        pinyin.append(pinyinArray[0]); // 取第一个拼音
                    }
                } else {
                    // 非汉字部分直接添加
                    pinyin.append(ch);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pinyin.toString();
    }
}
