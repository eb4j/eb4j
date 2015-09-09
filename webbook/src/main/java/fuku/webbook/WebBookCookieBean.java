package fuku.webbook;

import javax.servlet.http.Cookie;

import fuku.eb4j.util.HexUtil;
import static fuku.webbook.WebBookConstants.COOKIE_WEBBOOK;

/**
 * WebBookクッキー管理Beanクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WebBookCookieBean {

    private static final int METHOD = 0;
    private static final int MAXIMUM = 1;
    private static final int INLINE_IMAGE = 2;
    private static final int INLINE_OBJECT = 3;
    private static final int CANDIDATE_SELECTOR = 4;
    private static final int _MAX_ID = 5;

    /** 検索方法 */
    private int _method = -1;
    /** 表示件数 */
    private int _max = -1;
    /** 画像のインライン表示 */
    private boolean _inlineImage = false;
    /** 音声/動画のインライン表示 */
    private boolean _inlineObject = false;
    /** 候補セレクタの表示 */
    private boolean _candidate = false;


    /**
     * コンストラクタ。
     *
     */
    public WebBookCookieBean() {
        super();
    }


    /**
     * クッキーを設定します。
     *
     * @param cookie クッキー
     */
    public void setCookie(Cookie cookie) {
        if (cookie == null || !cookie.getName().equals(COOKIE_WEBBOOK)) {
            return;
        }
        String value = cookie.getValue();
        if (value.length() == 4) {
            // 下位互換
            _setField(METHOD, value.substring(0, 1));
            _setField(MAXIMUM, value.substring(1, 2) + "0");
            _setField(INLINE_IMAGE, value.substring(2, 3));
            _setField(INLINE_OBJECT, value.substring(3, 4));
        } else {
            int len = value.length();
            int idx = 0;
            try {
                while (idx < len) {
                    String str = value.substring(idx, idx+2);
                    int field = Integer.parseInt(str, 16);
                    idx += 2;
                    str = value.substring(idx, idx+2);
                    int n = Integer.parseInt(str, 16);
                    idx += 2;
                    str = value.substring(idx, idx+n);
                    _setField(field, str);
                    idx += n;
                }
            } catch (RuntimeException e) {
            }
        }
    }

    /**
     * クッキーを返します。
     *
     * @return クッキー
     */
    public Cookie getCookie() {
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<_MAX_ID; i++) {
            buf.append(HexUtil.toHexString(i, 2));
            String value = _getField(i);
            int len = value.length();
            if (len > 0xff) {
                value = value.substring(0, 0xff);
                len = 0xff;
            }
            buf.append(HexUtil.toHexString(len, 2));
            buf.append(value);
        }
        Cookie cookie = new Cookie(COOKIE_WEBBOOK, buf.toString());
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setSecure(false);
        return cookie;
    }

    /**
     * フィールドの値を設定します。
     *
     * @param field フィールドID
     * @param valeu 値
     */
    private void _setField(int field, String value) {
        switch (field) {
            case METHOD:
                try {
                    _method = Integer.parseInt(value);
                } catch (RuntimeException e) {
                }
                break;
            case MAXIMUM:
                try {
                    _max = Integer.parseInt(value);
                } catch (RuntimeException e) {
                }
                break;
            case INLINE_IMAGE:
                try {
                    int n = Integer.parseInt(value);
                    if (n > 0) {
                        _inlineImage = true;
                    } else {
                        _inlineImage = false;
                    }
                } catch (RuntimeException e) {
                }
                break;
            case INLINE_OBJECT:
                try {
                    int n = Integer.parseInt(value);
                    if (n > 0) {
                        _inlineObject = true;
                    } else {
                        _inlineObject = false;
                    }
                } catch (RuntimeException e) {
                }
                break;
            case CANDIDATE_SELECTOR:
                try {
                    int n = Integer.parseInt(value);
                    if (n > 0) {
                        _candidate = true;
                    } else {
                        _candidate = false;
                    }
                } catch (RuntimeException e) {
                }
                break;
            default:
                break;
        }
    }

    /**
     * フィールドの値を返します。
     *
     * @param field フィールドID
     * @return 値
     */
    private String _getField(int field) {
        String value = null;
        switch (field) {
            case METHOD:
                value = Integer.toString(_method);
                break;
            case MAXIMUM:
                value = Integer.toString(_max);
                break;
            case INLINE_IMAGE:
                if (_inlineImage) {
                    value = "1";
                } else {
                    value = "0";
                }
                break;
            case INLINE_OBJECT:
                if (_inlineObject) {
                    value = "1";
                } else {
                    value = "0";
                }
                break;
            case CANDIDATE_SELECTOR:
                if (_candidate) {
                    value = "1";
                } else {
                    value = "0";
                }
                break;
            default:
                break;
        }
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * 検索方法を設定します。
     *
     * @param method 検索方法
     */
    public void setMethod(int method) {
        _method = method;
    }

    /**
     * 検索方法を返します。
     *
     * @return 検索方法
     */
    public int getMethod() {
        return _method;
    }

    /**
     * 最大表示件数を設定します。
     *
     * @param max 最大表示件数
     */
    public void setMaximum(int max) {
        _max = max;
    }

    /**
     * 最大表示件数を返します。
     *
     * @return 最大表示件数
     */
    public int getMaximum() {
        return _max;
    }

    /**
     * 画像のインライン表示を設定します。
     *
     * @param inline 画像のインライン表示
     */
    public void setInlineImage(boolean inline) {
        _inlineImage = inline;
    }

    /**
     * 画像のインライン表示を返します。
     *
     * @return 画像のインライン表示
     */
    public boolean isInlineImage() {
        return _inlineImage;
    }

    /**
     * 音声/動画のインライン表示を設定します。
     *
     * @param inline 音声/動画のインライン表示
     */
    public void setInlineObject(boolean inline) {
        _inlineObject = inline;
    }

    /**
     * 音声/動画のインライン表示を返します。
     *
     * @return 音声/動画のインライン表示
     */
    public boolean isInlineObject() {
        return _inlineObject;
    }

    /**
     * 候補セレクタの表示を設定します。
     *
     * @param candidate 候補セレクタの表示
     */
    public void setCandidateSelector(boolean candidate) {
        _candidate = candidate;
    }

    /**
     * 候補セレクタの表示を返します。
     *
     * @return 候補セレクタの表示
     */
    public boolean isCandidateSelector() {
        return _candidate;
    }
}

// end of WebBookCookieBean.java
