package fuku.xml2eb;

import java.io.UnsupportedEncodingException;

/**
 * 検索語クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Word implements Comparable<Word> {

    /** 検索語 */
    private byte[] _word = null;
    /** 参照タグ名称 */
    private String _tag = null;


    /**
     * コンストラクタ。
     *
     * @param word 検索語
     * @param tag 参照タグ名称
     */
    public Word(byte[] word, String tag) {
        super();
        int len = word.length;
        _word = new byte[len];
        System.arraycopy(word, 0, _word, 0, len);
        _tag = tag;
    }


    /**
     * 検索語を返します。
     *
     * @return 検索語
     */
    public byte[] getWord() {
        int len = _word.length;
        byte[] b = new byte[len];
        System.arraycopy(_word, 0, b, 0, len);
        return b;
    }

    /**
     * 検索語のバイト数を返します。
     *
     * @return 検索語のバイト数
     */
    public int getWordLength() {
        return _word.length;
    }

    /**
     * 検索語の参照タグ名称を返します。
     *
     * @return 参照タグ名称
     */
    public String getReferenceTag() {
        return _tag;
    }

    /**
     * オブジェクトのハッシュコード値を返します。
     *
     * @return ハッシュコード値
     */
    @Override
    public int hashCode() {
        int code = 0;
        int n = _word.length;
        for (int i=0; i<n; i++) {
            code = code * 31 + _word[n];
        }
        code += _tag.hashCode();
        return code;
    }

    /**
     * このオブジェクトとほかのオブジェクトが等しいかどうかを返します。
     *
     * @param obj 比較対象オブジェクト
     * @return 等しい場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            if (compareTo((Word)obj) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * この検索語と指定された検索語の順序を比較します。
     *
     * @param word 比較対象の検索語
     * @return この検索語が指定された検索語より小さい場合は負の整数、
     *         等しい場合はゼロ、大きい場合は正の整数
     */
    @Override
    public int compareTo(Word word) {
        byte[] b = word.getWord();
        int len1 = _word.length;
        int len2 = b.length;
        int len = Math.min(len1, len2);
        int c1, c2, comp;
        for (int i=0; i<len; i++) {
            c1 = _word[i] & 0xff;
            c2 = b[i] & 0xff;
            comp = c1 - c2;
            if (comp != 0) {
                return comp;
            }
        }
        comp = len1 - len2;
        if (comp != 0) {
            return comp;
        }
        return _tag.compareTo(word.getReferenceTag());
    }

    /**
     * 検索語の文字列表現を返します。
     *
     * @return 文字列
     */
    @Override
    public String toString() {
        String str;
        try {
            str = new String(_word, "x-JIS0208");
        } catch (UnsupportedEncodingException e) {
            str = "???";
        }
        str += " [" + _tag + "]";
        return str;
    }
}

// end of Word.java
