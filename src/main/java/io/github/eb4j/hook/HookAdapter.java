package io.github.eb4j.hook;

/**
 * エスケープシーケンス加工の抽象アダプタクラス。
 *
 * @author Hisaya FUKUMOTO
 * @param <T> フックから取得されるオブジェクト
 */
public abstract class HookAdapter<T> implements Hook<T> {

    /**
     * コンストラクタ。
     *
     */
    public HookAdapter() {
        super();
    }


    /**
     * すべての入力をクリアし、初期化します。
     *
     */
    @Override
    public void clear() {
    }

    /**
     * フックによって加工されたオブジェクトを返します。
     *
     * @return 常にnull
     */
    @Override
    public T getObject() {
        return null;
    }

    /**
     * 次の入力が可能かどうかを返します。
     *
     * @return 常にfalse
     */
    @Override
    public boolean isMoreInput() {
        return false;
    }

    /**
     * 文字を追加します。
     *
     * @param ch 文字
     */
    @Override
    public void append(final char ch) {
        append(Character.toString(ch));
    }

    /**
     * 文字列を追加します。
     *
     * @param str 文字列
     */
    @Override
    public void append(final String str) {
    }

    /**
     * 外字を追加します。
     *
     * @param code 外字の文字コード
     */
    @Override
    public void append(final int code) {
    }

    /**
     * 半角表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginNarrow() {
    }

    /**
     * 半角表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endNarrow() {
    }

    /**
     * ユニコードの開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginUnicode() {
    }

    /**
     * ユニコードの終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endUnicode() {
    }

    /**
     * 下付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginSubscript() {
    }

    /**
     * 下付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSubscript() {
    }

    /**
     * 上付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginSuperscript() {
    }

    /**
     * 上付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSuperscript() {
    }

    /**
     * 行頭の字下げ指定を表すエスケープシーケンスに対するフックです。
     *
     * @param indent 字下げ量
     */
    @Override
    public void setIndent(final int indent) {
    }

    /**
     * 改行を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void newLine() {
    }

    /**
     * 改行禁止の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginNoNewLine() {
    }

    /**
     * 改行禁止の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endNoNewLine() {
    }

    /**
     * 強調表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginEmphasis() {
    }

    /**
     * 強調表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endEmphasis() {
    }

    /**
     * 文字修飾の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param type 文字修飾種別
     * @see #BOLD
     * @see #ITALIC
     */
    @Override
    public void beginDecoration(final int type) {
    }

    /**
     * 文字修飾の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endDecoration() {
    }

    /**
     * 複合検索の候補となる語の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginCandidate() {
    }

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<BR>
     * 候補となる語はさらに細かい選択肢に分かれていることを示します。
     *
     * @param pos 次の階層の候補一覧データの位置
     */
    @Override
    public void endCandidateGroup(final long pos) {
    }

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<BR>
     * 候補となる語が実際に検索の入力語として使えるものであることを示します。
     *
     */
    @Override
    public void endCandidateLeaf() {
    }

    /**
     * 別位置のテキストデータの参照開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginReference() {
    }

    /**
     * 別位置のテキストデータの参照終了を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    @Override
    public void endReference(final long pos) {
    }

    /**
     * キーワード表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginKeyword() {
    }

    /**
     * キーワード表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endKeyword() {
    }

    /**
     * モノクロ画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param width 画像の幅
     * @param height 画像の高さ
     */
    @Override
    public void beginMonoGraphic(final int width, final int height) {
    }

    /**
     * モノクロ画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 画像データの位置
     */
    @Override
    public void endMonoGraphic(final long pos) {
    }

    /**
     * インラインカラー画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 画像形式
     * @param pos 画像データの位置
     * @see #DIB
     * @see #JPEG
     */
    @Override
    public void beginInlineColorGraphic(final int format, final long pos) {
    }

    /**
     * インラインカラー画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endInlineColorGraphic() {
    }

    /**
     * カラー画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 画像形式
     * @param pos 画像データの位置
     * @see #DIB
     * @see #JPEG
     */
    @Override
    public void beginColorGraphic(final int format, final long pos) {
    }

    /**
     * カラー画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endColorGraphic() {
    }

    /**
     * 音声の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 音声形式
     * @param start 音声データの開始位置
     * @param end 音声データの終了位置
     * @see #WAVE
     * @see #MIDI
     */
    @Override
    public void beginSound(final int format, final long start, final long end) {
    }

    /**
     * 音声の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSound() {
    }

    /**
     * 動画の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 動画形式
     * @param width 動画の幅
     * @param height 動画の高さ
     * @param filename 動画ファイル名
     */
    @Override
    public void beginMovie(final int format, final int width, final int height,
                           final String filename) {
    }

    /**
     * 動画の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endMovie() {
    }

    /**
     * カラー画像データ群の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    @Override
    public void beginGraphicReference(final long pos) {
    }

    /**
     * カラー画像データ群の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endGraphicReference() {
    }

    /**
     * カラー画像データ群の参照を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    @Override
    public void setGraphicReference(final long pos) {
    }

    /**
     * カラー画像データ群の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginImagePage() {
    }

    /**
     * カラー画像データ群の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endImagePage() {
    }

    /**
     * クリック領域の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param x x座標
     * @param y y座標
     * @param w 幅
     * @param h 高さ
     * @param pos 参照先の位置
     */
    @Override
    public void beginClickableArea(final int x, final int y, final int w, final int h,
                                   final long pos) {
    }

    /**
     * クリック領域の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endClickableArea() {
    }

    /**
     * EBXA-C外字の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginEBXACGaiji() {
    }

    /**
     * EBXA-C外字の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endEBXACGaiji() {
    }
}

// end of HookAdapter.java
