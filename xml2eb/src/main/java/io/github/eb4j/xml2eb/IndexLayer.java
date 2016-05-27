package fuku.xml2eb;

import java.io.File;
import java.util.ArrayList;

/**
 * インデックス階層クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class IndexLayer extends ArrayList<Index> {

    /** 階層レベル */
    private int _level = 0;
    /** インデックスブロック番号 */
    private long _block = 1;
    /** 固定単語長 */
    private int _wordLength = 0;
    /** ブロック内のバイト数 */
    private int _blockLength = 4;
    /** 出力ファイル */
    private File _file = null;


    /**
     * コンストラクタ。
     *
     * @param file ベースファイル
     * @param level 階層レベル
     */
    public IndexLayer(File file, int level) {
        super();
        _level = level;
        File dir = file.getParentFile();
        if (dir == null) {
            dir = new File(".");
        }
        String name = file.getName() + "." + level;
        _file = new File(dir, name);
    }


    /**
     * エントリ内容をクリアし、インデックスブロック番号を増加させます。
     *
     */
    @Override
    public void clear() {
        super.clear();
        _block++;
        _wordLength = 0;
        _blockLength = 4;
    }

    /**
     * 階層レベルを返します。
     *
     * @return 階層レベル
     */
    public int getLevel() {
        return _level;
    }

    /**
     * インデックスブロック番号を返します。
     *
     * @return インデックスブロック番号
     */
    public long getBlock() {
        return _block;
    }

    /**
     * 固定単語長を返します。
     *
     * @return 固定単語長
     */
    public int getWordLength() {
        return _wordLength;
    }

    /**
     * 固定単語長を設定します。
     *
     * @param len 固定単語長
     */
    public void setWordLength(int len) {
        _wordLength = len;
    }

    /**
     * ブロック内のバイト数を返します。
     *
     * @return バイト数
     */
    public int getBlockLength() {
        return _blockLength;
    }

    /**
     * 最後のインデックスを返します。
     *
     * @return 最後のインデックス
     */
    public Index getLastIndex() {
        int len = size();
        return get(len-1);
    }

    /**
     * インデックスを追加します。
     *
     * @param index インデックス
     * @return 追加に成功した場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean add(Index index) {
        _blockLength += index.getWord().getWordLength() + 13;
        return super.add(index);
    }

    /**
     * 出力ファイルを返します。
     *
     * @return 出力ファイル
     */
    public File getFile() {
        return _file;
    }
}

// end of IndexLayer.java
