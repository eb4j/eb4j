package fuku.eb4j.io;

import java.io.File;

/**
 * ファイル情報クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class FileInfo {

    /** ファイル */
    private File _file = null;
    /** フォーマット形式 */
    private int _format = EBFile.FORMAT_PLAIN;

    /** ファイルサイズ */
    private long _fileSize = 0L;
    /** 実ファイルサイズ */
    private long _realFileSize = 0L;
    /** スライスサイズ */
    private int _sliceSize = BookInputStream.PAGE_SIZE;

    /** 圧縮レベル (for EBZIP) */
    private int _zipLevel = 0;
    /** インデックスサイズ (for EBZIP) */
    private int _zipIndexSize = 0;
    /** CRC (for EBZIP) */
    private long _zipCrc = 0L;

    /** インデックステーブルの位置 (for EPWING) */
    private long _epwingIndexPos = 0L;
    /** インデックステーブルのサイズ (for EPWING) */
    private long _epwingIndexSize = 0L;
    /** 頻度テーブルの位置 (for EPWING) */
    private long _epwingFreqPos = 0L;
    /** 頻度テーブルのサイズ (for EPWING) */
    private long _epwingFreqSize = 0L;
    /** ハフマンツリーのルートノード (for EPWING) */
    private HuffmanNode _epwingRootNode = null;

    /** インデックス開始位置 (for S-EBXA) */
    private long _sebxaIndexPos = 0L;
    /** 圧縮本文開始位置 (for S-EBXA) */
    private long _sebxaBasePos = 0L;
    /** 本文開始位置 (for S-EBXA) */
    private long _sebxaStartPos = 0L;
    /** 本文終了位置 (for S-EBXA) */
    private long _sebxaEndPos = 0L;


    /**
     * コンストラクタ。
     *
     */
    protected FileInfo() {
        super();
    }


    /**
     * ファイルのパス名を返します。
     *
     * @return ファイルのパス名
     */
    protected String getPath() {
        return _file.getPath();
    }

    /**
     * ファイルを返します。
     *
     * @return ファイル
     */
    protected File getFile() {
        return _file;
    }

    /**
     * ファイルを設定します。
     *
     * @param file ファイル
     */
    protected void setFile(File file) {
        _file = file;
    }

    /**
     * フォーマット形式を返します。
     *
     * @return フォーマット形式
     * @see EBFile#FORMAT_PLAIN
     * @see EBFile#FORMAT_EBZIP
     * @see EBFile#FORMAT_EPWING
     * @see EBFile#FORMAT_EPWING6
     */
    protected int getFormat() {
        return _format;
    }

    /**
     * フォーマット形式を設定します。
     *
     * @param format フォーマット形式
     * @see EBFile#FORMAT_PLAIN
     * @see EBFile#FORMAT_EBZIP
     * @see EBFile#FORMAT_EPWING
     * @see EBFile#FORMAT_EPWING6
     */
    protected void setFormat(int format) {
        _format = format;
    }

    /**
     * ファイルサイズを返します。
     *
     * @return ファイルサイズ
     */
    protected long getFileSize() {
        return _fileSize;
    }

    /**
     * ファイルサイズを設定します。
     *
     * @param size フィイルサイズ
     */
    protected void setFileSize(long size) {
        _fileSize = size;
    }

    /**
     * 実ファイルサイズを返します。
     *
     * @return 実ファイルサイズ
     */
    protected long getRealFileSize() {
        return _realFileSize;
    }

    /**
     * 実ファイルサイズを設定します。
     *
     * @param size 実ファイルサイズ
     */
    protected void setRealFileSize(long size) {
        _realFileSize = size;
    }

    /**
     * スライスサイズを返します。
     *
     * @return スライスサイズ
     */
    protected int getSliceSize() {
        return _sliceSize;
    }

    /**
     * スライスサイズを設定します。
     *
     * @param size スライスサイズ
     */
    protected void setSliceSize(int size) {
        _sliceSize = size;
    }

    /**
     * EBZIPの圧縮レベルを返します。
     *
     * @return 圧縮レベル
     */
    protected int getZipLevel() {
        return _zipLevel;
    }

    /**
     * EBZIPの圧縮レベルを設定します。
     *
     * @param level 圧縮レベル
     */
    protected void setZipLevel(int level) {
        _zipLevel = level;
    }

    /**
     * EBZIPのインデックスサイズを返します。
     *
     * @return インデックスサイズ
     */
    protected int getZipIndexSize() {
        return _zipIndexSize;
    }

    /**
     * EBZIPのインデックスサイズを設定します。
     *
     * @param size インデックスサイズ
     */
    protected void setZipIndexSize(int size) {
        _zipIndexSize = size;
    }

    /**
     * EBZIPのCRCを返します。
     *
     * @return CRC
     */
    protected long getZipCRC() {
        return _zipCrc;
    }

    /**
     * EBZIPのCRCを設定します。
     *
     * @param crc CRC
     */
    protected void setZipCRC(long crc) {
        _zipCrc = crc;
    }

    /**
     * EPWINGのインデックステーブル位置を返します。
     *
     * @return インデックステーブル位置
     */
    protected long getEpwingIndexPosition() {
        return _epwingIndexPos;
    }

    /**
     * EPWINGのインデックステーブル位置を設定します。
     *
     * @param pos インデックステーブル位置
     */
    protected void setEpwingIndexPosition(long pos) {
        _epwingIndexPos = pos;
    }

    /**
     * EPWINGのインデックステーブルサイズを設定します。
     *
     * @return インデックステーブルサイズ
     */
    protected long getEpwingIndexSize() {
        return _epwingIndexSize;
    }

    /**
     * EPWINGのインデックステーブルサイズを設定します。
     *
     * @param size インデックステーブルサイズ
     */
    protected void setEpwingIndexSize(long size) {
        _epwingIndexSize = size;
    }

    /**
     * EPWINGの頻度テーブル位置を返します。
     *
     * @return 頻度テーブル位置
     */
    protected long getEpwingFrequencyPosition() {
        return _epwingFreqPos;
    }

    /**
     * EPWINGの頻度テーブル位置を設定します。
     *
     * @param pos 頻度テーブル位置
     */
    protected void setEpwingFrequencyPosition(long pos) {
        _epwingFreqPos = pos;
    }

    /**
     * EPWINGの頻度テーブルサイズを返します。
     *
     * @return 頻度テーブルサイズ
     */
    protected long getEpwingFrequencySize() {
        return _epwingFreqSize;
    }

    /**
     * EPWINGの頻度テーブルサイズを設定します。
     *
     * @param size 頻度テーブルサイズ
     */
    protected void setEpwingFrequencySize(long size) {
        _epwingFreqSize = size;
    }

    /**
     * EPWINGのハフマンツリールートノードを返します。
     *
     * @return ハフマンツリールートノード
     */
    protected HuffmanNode getEpwingRootNode() {
        return _epwingRootNode;
    }

    /**
     * EPWINGのハフマンツリールートノードを設定します。
     *
     * @param node ハフマンツリールートノード
     */
    protected void setEpwingRootNode(HuffmanNode node) {
        _epwingRootNode = node;
    }

    /**
     * S-EBXAのインデックス開始位置を返します。
     *
     * @return インデックス開始位置
     */
    protected long getSebxaIndexPosition() {
        return _sebxaIndexPos;
    }

    /**
     * S-EBXAのインデックス開始位置を設定します。
     *
     * @param pos インデックス開始位置
     */
    protected void setSebxaIndexPosition(long pos) {
        _sebxaIndexPos = pos;
    }

    /**
     * S-EBXAの圧縮本文開始位置を返します。
     *
     * @return 圧縮本文開始位置
     */
    protected long getSebxaBasePosition() {
        return _sebxaBasePos;
    }

    /**
     * S-EBXAの圧縮本文開始位置を設定します。
     *
     * @param pos 圧縮本文開始位置
     */
    protected void setSebxaBasePosition(long pos) {
        _sebxaBasePos = pos;
    }

    /**
     * S-EBXAの本文開始位置を返します。
     *
     * @return 本文開始位置
     */
    protected long getSebxaStartPosition() {
        return _sebxaStartPos;
    }

    /**
     * S-EBXAの本文開始位置を設定します。
     *
     * @param pos 本文開始位置
     */
    protected void setSebxaStartPosition(long pos) {
        _sebxaStartPos = pos;
    }

    /**
     * S-EBXAの本文終了位置を返します。
     *
     * @return 本文終了位置
     */
    protected long getSebxaEndPosition() {
        return _sebxaEndPos;
    }

    /**
     * S-EBXAの本文終了位置を設定します。
     *
     * @param pos 本文終了位置
     */
    protected void setSebxaEndPosition(long pos) {
        _sebxaEndPos = pos;
    }
}

// end of FileInfo.java
