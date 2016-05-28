package io.github.eb4j.webbook;

import java.util.List;

/**
 * 候補クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Candidate {

    /** インデックス番号 */
    private int _index = 0;
    /** ラベル */
    private String _label = null;
    /** 外字エスケープ済みラベル */
    private String _escapedLabel = null;
    /** グループ候補 */
    private List<Candidate> _list = null;


    /**
     * コンストラクタ。
     *
     * @param index インデックス番号
     * @param label 候補ラベル
     * @param escapedLabel 外字エスケープ済みラベル
     */
    public Candidate(int index, String label, String escapedLabel) {
        super();
        _index = index;
        _label = label;
        _escapedLabel = escapedLabel;
    }


    /**
     * インデックス番号を返します。
     *
     * @return インデックス番号
     */
    public int getIndex() {
        return _index;
    }

    /**
     * 候補ラベルを返します。
     *
     * @return 候補ラベル
     */
    public String getLabel() {
        return _label;
    }

    /**
     * 外字エスケープ済み候補ラベルを返します。
     *
     * @return 候補ラベル
     */
    public String getEscapedLabel() {
        return _escapedLabel;
    }

    /**
     * 候補グループを設定します。
     *
     * @param list 候補のリスト
     */
    public void setCandidateList(List<Candidate> list) {
        _list = list;
    }

    /**
     * 候補グループを返します。
     *
     * @return 候補のリスト
     */
    public List<Candidate> getCandidateList() {
        return _list;
    }

    /**
     * グループ候補かどうかを返します。
     *
     * @return グループ候補の場合はtrue、そうでない場合はfalse
     */
    public boolean isGroup() {
        if (_list == null || _list.isEmpty()) {
            return false;
        }
        return true;
    }
}

// end of Candidate.java
