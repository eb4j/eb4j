package io.github.eb4j.xml2eb.converter.wdic;

import java.util.ArrayList;
import java.util.List;

/**
 * テーブル行クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicTableRow {

    /** 要素リスト */
    List<WdicTableItem> _items = new ArrayList<WdicTableItem>();


    /**
     * コンストラクタ。
     *
     */
    public WdicTableRow() {
        super();
    }

    /**
     * 要素を追加します。
     *
     * @param item 要素
     */
    public void add(WdicTableItem item) {
        _items.add(item);
    }

    /**
     * 要素を追加します。
     *
     * @param index 追加位置
     * @param item 要素
     */
    public void add(int index, WdicTableItem item) {
        _items.add(index, item);
    }

    /**
     * 要素数を返します。
     *
     * @return 要素数
     */
    public int size() {
        return _items.size();
    }

    /**
     * 指定されたインデックスの要素を返します。
     *
     * @param index インデックス
     * @return 要素
     */
    public WdicTableItem get(int index) {
        return _items.get(index);
    }
}

// end of WdicTableRow.java
