package fuku.eb4j.io;

import java.util.Collections;
import java.util.List;

/**
 * ハフマンノードクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class HuffmanNode implements Comparable<HuffmanNode> {

    /** EOF葉ノード */
    protected static final int LEAF_EOF = 0;
    /** 8bit葉ノード */
    protected static final int LEAF_8 = 1;
    /** 16bit葉ノード */
    protected static final int LEAF_16 = 2;
    /** 32bit葉ノード */
    protected static final int LEAF_32 = 3;

    /** 葉ノードの種類 */
    private int _leafType = -1;
    /** 値 */
    private long _value = -1L;
    /** 出現頻度値 */
    private int _frequency = 0;
    /** 左子ノード */
    private HuffmanNode _left = null;
    /** 右子ノード */
    private HuffmanNode _right = null;


    /**
     * コンストラクタ。 (葉ノード用)
     *
     * @param value 値
     * @param frequency 出現頻度値
     * @param leafType 葉ノードの種類
     */
    protected HuffmanNode(long value, int frequency, int leafType) {
        _value = value;
        _frequency = frequency;
        _leafType = leafType;
    }

    /**
     * コンストラクタ。 (枝ノード用)
     *
     * @param left 左子ノード
     * @param right 右子ノード
     */
    protected HuffmanNode(HuffmanNode left, HuffmanNode right) {
        _left = left;
        _right = right;
        _frequency = _left.getFrequency() + _right.getFrequency();
    }


    /**
     * 葉ノードの種類を返します。
     *
     * @return 葉ノードの種類
     */
    protected int getLeafType() {
        return _leafType;
    }

    /**
     * 値を返します。
     *
     * @return 値
     */
    protected long getValue() {
        return _value;
    }

    /**
     * 出現頻度値を返します。
     *
     * @return 出現頻度値
     */
    protected int getFrequency() {
        return _frequency;
    }

    /**
     * 左子ノードを返します。
     *
     * @return 左子ノード
     */
    protected HuffmanNode getLeft() {
        return _left;
    }

    /**
     * 右子ノードを返します。
     *
     * @return 右子ノード
     */
    protected HuffmanNode getRight() {
        return _right;
    }

    /**
     * 葉ノードか枝ノードかを判別します。
     *
     * @return 葉ノードであればtrue、枝ノードであればfalse
     */
    protected boolean isLeaf() {
        if (_right == null && _left == null) {
            return true;
        }
        return false;
    }

    /**
     * オブジェクトのハッシュコード値を返します。
     *
     * @return ハッシュコード値
     */
    public int hashCode() {
        return (int)(_value + _frequency);
    }

    /**
     * このオブジェクトとほかのオブジェクトが等しいかどうかを返します。
     *
     * @param obj 比較対象オブジェクト
     * @return 等しい場合はtrue、そうでない場合はfalse
     */
    public boolean equals(Object obj) {
        if (obj instanceof HuffmanNode) {
            HuffmanNode node = (HuffmanNode)obj;
            if (node.getLeafType() == getLeafType()
                && node.getValue() == getValue()
                && node.getFrequency() == getFrequency()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 2つのノードの出現頻度値を比較します。
     *
     * @param node 比較対象のノード
     * @return このノードの頻度値が引数ノードの頻度値と等しい場合0、
     *         このノードの頻度値が引数ノードの頻度値より小さい場合は0より小さい値、
     *         このノードの頻度値が引数ノードの頻度値より大きい場合は0より大きい値
     */
    @Override
    public int compareTo(HuffmanNode node) {
        int ret = getFrequency() - node.getFrequency();
        return ret;
    }

    /**
     * ハフマンツリーを作成します。
     *
     * @param list HuffmanNodeのリスト
     * @return ルートノード
     */
    protected static HuffmanNode makeTree(List<HuffmanNode> list) {
        HuffmanNode node1, node2, tmp;

        // ソート (選択ソート)
        int size = list.size();
        for (int i=0; i<size-1; i++) {
            node1 = list.get(i);
            int n = i;
            for (int j=i+1; j<size; j++) {
                tmp = list.get(j);
                if (node1.compareTo(tmp) < 0) {
                    node1 = tmp;
                    n = j;
                }
            }
            if (i != n) {
                Collections.swap(list, i, n);
            }
        }

        // ハフマンツリーの作成
        while (list.size() > 1) {
            // 頻度値が最も小さいノードの検索
            size = list.size();
            node1 = list.get(0);
            int n = 0;
            for (int i=1; i<size; i++) {
                tmp = list.get(i);
                if (node1.compareTo(tmp) >= 0) {
                    node1 = tmp;
                    n = i;
                }
            }
            list.remove(n);

            // 頻度値が次に小さいノードの検索
            size = list.size();
            node2 = list.get(0);
            n = 0;
            for (int i=1; i<size; i++) {
                tmp = list.get(i);
                if (node2.compareTo(tmp) >= 0) {
                    node2 = tmp;
                    n = i;
                }
            }
            list.remove(n);

            // 枝ノードの作成
            list.add(new HuffmanNode(node1, node2));
        }
        return list.get(0);
    }
}

// end of HuffmanNode.java
