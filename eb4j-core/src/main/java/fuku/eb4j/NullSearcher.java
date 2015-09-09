package fuku.eb4j;

/**
 * NULL検索クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class NullSearcher implements Searcher {

    /**
     * コンストラクタ。
     *
     */
    protected NullSearcher() {
        super();
    }

    /**
     * 常にnullを返します。
     *
     * @return 常にnull
     * @exception EBException 検索中にエラーが発生した場合
     */
    @Override
    public Result getNextResult() throws EBException {
        return null;
    }
}

// end of NullSearcher.java
