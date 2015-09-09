package fuku.webbook;

/**
 * 単語検索コマンドクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class SearchForm {

    /** 書籍Bean */
    private WebBookBean _webbook = null;

    /** 検索語 */
    private String _word = null;
    /** 検索対象 */
    private int _target = -1;
    /** 検索方法 */
    private int _method = -1;
    /** 表示件数 */
    private int _max = -1;


    /**
     * コンストラクタ。
     *
     */
    public SearchForm() {
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
     * 検索語を設定します。
     *
     * @param word 検索語
     */
    public void setWord(String word) {
        _word = word;
    }

    /**
     * 検索語を返します。
     *
     * @return 検索語
     */
    public String getWord() {
        return _word;
    }

    /**
     * 検索対象を設定します。
     *
     * @param target 検索対象
     */
    public void setTarget(int target) {
        _target = target;
    }

    /**
     * 検索対象を返します。
     *
     * @return 検索対象
     */
    public int getTarget() {
        return _target;
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
}

// end of SearchForm.java
