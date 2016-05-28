package io.github.eb4j.hook;

/**
 * エスケープシーケンス処理インタフェース。
 *
 * @author Hisaya FUKUMOTO
 * @param <T> フックから取得されるオブジェクト
 */
public interface Hook<T> {

    /** 画像形式 (DIB) */
    int DIB = 0;
    /** 画像形式 (JPEG) */
    int JPEG = 1;

    /** 音声形式 (WAVE) */
    int WAVE = 1;
    /** 音声形式 (MIDI) */
    int MIDI = 2;

    /** 動画形式 (MPEG) */
    int MPEG = 1;

    /** 文字修飾種別 (太字) */
    int BOLD = 0x03;
    /** 文字修飾種別 (斜体) */
    int ITALIC = 0x01;


    /**
     * すべての入力をクリアし、初期化します。
     *
     */
    void clear();

    /**
     * フックによって加工されたオブジェクトを返します。
     *
     * @return オブジェクト
     */
    T getObject();

    /**
     * 次の入力が可能かどうかを返します。
     *
     * @return まだ入力を受けつける場合はtrue、そうでない場合はfalse
     */
    boolean isMoreInput();

    /**
     * 文字を追加します。
     *
     * @param ch 文字
     */
    void append(char ch);

    /**
     * 文字列を追加します。
     *
     * @param str 文字列
     */
    void append(String str);

    /**
     * 外字を追加します。
     *
     * @param code 外字の文字コード
     */
    void append(int code);

    /**
     * 半角表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginNarrow();

    /**
     * 半角表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endNarrow();

    /**
     * ユニコードの開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginUnicode();

    /**
     * ユニコードの終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endUnicode();

    /**
     * 下付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginSubscript();

    /**
     * 下付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endSubscript();

    /**
     * 上付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginSuperscript();

    /**
     * 上付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endSuperscript();

    /**
     * 行頭の字下げ指定を表すエスケープシーケンスに対するフックです。
     *
     * @param indent 字下げ量
     */
    void setIndent(int indent);

    /**
     * 改行を表すエスケープシーケンスに対するフックです。
     *
     */
    void newLine();

    /**
     * 改行禁止の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginNoNewLine();

    /**
     * 改行禁止の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endNoNewLine();

    /**
     * 強調表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginEmphasis();

    /**
     * 強調表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endEmphasis();

    /**
     * 文字修飾の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param type 文字修飾種別
     * @see #BOLD
     * @see #ITALIC
     */
    void beginDecoration(int type);

    /**
     * 文字修飾の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endDecoration();

    /**
     * 複合検索の候補となる語の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginCandidate();

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<BR>
     * 候補となる語はさらに細かい選択肢に分かれていることを示します。
     *
     * @param pos 次の階層の候補一覧データの位置
     */
    void endCandidateGroup(long pos);

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<BR>
     * 候補となる語が実際に検索の入力語として使えるものであることを示します。
     *
     */
    void endCandidateLeaf();

    /**
     * 別位置のテキストデータの参照開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginReference();

    /**
     * 別位置のテキストデータの参照終了を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    void endReference(long pos);

    /**
     * キーワード表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginKeyword();

    /**
     * キーワード表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endKeyword();

    /**
     * モノクロ画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param width 画像の幅
     * @param height 画像の高さ
     */
    void beginMonoGraphic(int width, int height);

    /**
     * モノクロ画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 画像データの位置
     */
    void endMonoGraphic(long pos);

    /**
     * インラインカラー画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 画像形式
     * @param pos 画像データの位置
     * @see #DIB
     * @see #JPEG
     */
    void beginInlineColorGraphic(int format, long pos);

    /**
     * インラインカラー画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endInlineColorGraphic();

    /**
     * カラー画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 画像形式
     * @param pos 画像データの位置
     * @see #DIB
     * @see #JPEG
     */
    void beginColorGraphic(int format, long pos);

    /**
     * カラー画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endColorGraphic();

    /**
     * 音声の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 音声形式
     * @param start 音声データの開始位置
     * @param end 音声データの終了位置
     * @see #WAVE
     * @see #MIDI
     */
    void beginSound(int format, long start, long end);

    /**
     * 音声の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endSound();

    /**
     * 動画の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 動画形式
     * @param width 動画の幅
     * @param height 動画の高さ
     * @param filename 動画ファイル名
     */
    void beginMovie(int format, int width, int height, String filename);

    /**
     * 動画の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endMovie();

    /**
     * カラー画像データ群の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    void beginGraphicReference(long pos);

    /**
     * カラー画像データ群の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endGraphicReference();

    /**
     * カラー画像データ群の参照を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    void setGraphicReference(long pos);

    /**
     * カラー画像データ群の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginImagePage();

    /**
     * カラー画像データ群の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endImagePage();

    /**
     * クリック領域の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param x x座標
     * @param y y座標
     * @param w 幅
     * @param h 高さ
     * @param pos 参照先の位置
     */
    void beginClickableArea(int x, int y, int w, int h, long pos);

    /**
     * クリック領域の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endClickableArea();

    /**
     * EBXA-C外字の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    void beginEBXACGaiji();

    /**
     * EBXA-C外字の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    void endEBXACGaiji();
}

// end of Hook.java
