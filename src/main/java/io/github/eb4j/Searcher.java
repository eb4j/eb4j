package io.github.eb4j;

/**
 * 検索インタフェース。
 *
 * @author Hisaya FUKUMOTO
 */
public interface Searcher {

    /**
     * Returns a next search result.
     *
     * @return a search result or null when no value to return.
     * @exception EBException if error happened.
     */
    Result getNextResult() throws EBException;
}

// end of Searcher.java
