package fuku.webbook.acl;

/**
 * アクセス制御エントリ基底クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public abstract class AbstractACLEntry implements ACLEntry {

    /** 許可リストか拒否リストかを示すフラグ */
    private boolean _allow = true;


    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     */
    protected AbstractACLEntry(boolean allow) {
        super();
        _allow = allow;
    }

    /**
     * 許可リストかどうかを返します。
     *
     * @return 許可リストの場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isAllowEntry() {
        return _allow;
    }
}

// end of AbstractACLEntry.java
