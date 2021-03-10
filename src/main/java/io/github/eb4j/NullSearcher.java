package io.github.eb4j;

/**
 * NULL searcher class.
 *
 * @author Hisaya FUKUMOTO
 */
public class NullSearcher implements Searcher {

    /**
     * Constructor.
     *
     */
    protected NullSearcher() {
        super();
    }

    /**
     * Always return null.
     *
     * @return null
     * @exception EBException if error is happened when searching.
     */
    @Override
    public Result getNextResult() throws EBException {
        return null;
    }
}

// end of NullSearcher.java
