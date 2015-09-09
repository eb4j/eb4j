package fuku.webbook;

/**
 * 表示設定コマンドクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class PreferenceForm {

    /** 書籍Bean */
    private WebBookBean _webbook = null;

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
    public PreferenceForm() {
        super();
    }


    /**
     * 書籍Beanを設定します。
     *
     * @param webbook 書籍Bean
     */
    public void setWebBookBean(WebBookBean webbook) {
        _webbook = webbook;
    }

    /**
     * 書籍Beanを設定します。
     *
     * @return 書籍Bean
     */
    public WebBookBean getWebBookBean() {
        return _webbook;
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

// end of PreferenceForm.java
