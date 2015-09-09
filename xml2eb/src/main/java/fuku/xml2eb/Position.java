package fuku.xml2eb;

import java.io.File;

import fuku.eb4j.util.HexUtil;

/**
 * 位置クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Position {

    /** ファイル */
    private File _file = null;
    /** 位置 */
    private long _pos = 0L;


    /**
     * コンストラクタ。
     *
     * @param file ファイル
     * @param pos 位置
     */
    public Position(File file, long pos) {
        super();
        _file = file;
        _pos = pos;
    }


    /**
     * ファイルを返します。
     *
     * @return ファイル
     */
    public File getFile() {
        return _file;
    }

    /**
     * 位置を返します。
     *
     * @return 位置
     */
    public long getPosition() {
        return _pos;
    }

    /**
     * 位置を返します。
     *
     * @param offset オフセットブロック数
     * @return 位置
     */
    public long getPosition(long offset) {
        return _pos + ((offset - 1) * 2048);
    }

    /**
     * ブロック位置を返します。
     *
     * @return ブロック位置
     */
    public long getBlock() {
        return _pos / 2048 + 1;
    }

    /**
     * ブロック内のオフセット位置を返します。
     *
     * @return オフセット位置
     */
    public int getOffset() {
        return (int)(_pos % 2048);
    }

    /**
     * 文字列表現を返します。
     *
     * @return 文字列
     */
    public String toString() {
        return _file.getName() + ":0x" + HexUtil.toHexString(_pos);
    }
}

// end of Position.java
