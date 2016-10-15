package io.github.eb4j;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.util.CompareUtil;
import io.github.eb4j.util.HexUtil;

/**
 * Search class for searching with a single word.
 *
 * @author Hisaya FUKUMOTO
 */
public class SingleWordSearcher implements Searcher {

    /** 前方一致検索を示す定数 */
    protected static final int WORD = 0;
    /** 後方一致検索を示す定数 */
    protected static final int ENDWORD = 1;
    /** 完全一致検索を示す定数 */
    protected static final int EXACTWORD = 2;
    /** 条件検索を示す定数 */
    protected static final int KEYWORD = 3;
    /** クロス検索を示す定数 */
    protected static final int CROSS = 4;
    /** 複合検索を示す定数 */
    protected static final int MULTI = 5;

    /** 最大インデックス深さ */
    private static final int MAX_INDEX_DEPTH = 6;

    /** 項目の配置スタイル */
    private static final int VARIABLE = 0;
    /** 項目の配置スタイル */
    private static final int FIXED = 1;

    /** ログ */
    private Logger _logger = null;

    /** 副本 */
    private SubBook _sub = null;
    /** インデックススタイル */
    private IndexStyle _style = null;
    /** 現在の検索種別 */
    private int _type = 0;

    /** 検索語 */
    private byte[] _word = null;
    /** 検索キー */
    private byte[] _canonical = null;
    /** 検索するファイル */
    private EBFile _file = null;

    /** キャッシュ */
    private byte[] _cache = new byte[BookInputStream.PAGE_SIZE];
    /** キャシュのページ位置 */
    private long _cachePage = 0L;
    /** キャシュのオフセット位置 */
    private int _off = 0;

    /** データのページ位置 */
    private long _page = 0L;
    /** データのページID */
    private int _pageID = 0;
    /** エントリのサイズ */
    private int _entryLength = 0;
    /** エントリの配置方法 */
    private int _entryArrangement = 0;
    /** エントリの数 */
    private int _entryCount = 0;
    /** エントリのインデックス */
    private int _entryIndex = 0;
    /** グループエントリ内であることを示すフラグ */
    private boolean _inGroupEntry = false;
    /** 比較結果 */
    private int _comparison = -1;

    /** キーワード検索用見出し位置 */
    private long _keywordHeading = 0L;


    /**
     * Build searcher object.
     *
     * @param sub subbook.
     * @param style index style.
     * @param type search type.
     * @see SingleWordSearcher#WORD
     * @see SingleWordSearcher#ENDWORD
     * @see SingleWordSearcher#EXACTWORD
     * @see SingleWordSearcher#KEYWORD
     * @see SingleWordSearcher#CROSS
     * @see SingleWordSearcher#MULTI
     */
    protected SingleWordSearcher(final SubBook sub, final IndexStyle style, final int type) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _sub = sub;
        _file = sub.getTextFile();
        _style = style;
        _type = type;
    }


    /**
     * Set a word to search.
     *
     * @param word a search word.
     */
    private void _setWord(final byte[] word) {
        int len = word.length;
        _word = new byte[len];
        System.arraycopy(word, 0, _word, 0, len);
        _canonical = new byte[len];
        System.arraycopy(word, 0, _canonical, 0, len);

        if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
            _style.fixWordLatin(_canonical);
        } else {
            _style.fixWord(_canonical);
        }

        if (_style.getIndexID() != 0x70 && _style.getIndexID() != 0x90) {
            System.arraycopy(_canonical, 0, _word, 0, len);
        }

        // 後方検索の場合、反転する
        if (_type == ENDWORD) {
            if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                ByteUtil.reverseWordLatin(_word);
                ByteUtil.reverseWordLatin(_canonical);
            } else {
                ByteUtil.reverseWord(_word);
                ByteUtil.reverseWord(_canonical);
            }
        }
        try {
            _logger.debug("search word: '" + new String(_word, "x-JIS0208") + "'");
            _logger.debug("search canonical word: '" + new String(_canonical, "x-JIS0208") + "'");
        } catch (UnsupportedEncodingException e) {
        }
    }

    /**
     * キーとパターンを比較します。
     *
     * @param key キー
     * @param pattern パターン
     * @return キーがパターンと同じ場合:0、
     *         キーがパターンより大きい場合:1以上、
     *         キーがパターンより小さい場合:-1以下
     */
    private int _comparePre(final byte[] key, final byte[] pattern) {
        int comp = 0;
        switch (_type) {
            case EXACTWORD:
                if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                    comp = CompareUtil.compareToLatin(key, pattern, true);
                } else {
                    comp = CompareUtil.compareToJISX0208(key, pattern, true);
                }
                break;
            case MULTI:
                if (_style.getCandidatePage() == 0) {
                    comp = CompareUtil.compareToByte(key, pattern, true);
                } else {
                    if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                        comp = CompareUtil.compareToLatin(key, pattern, true);
                    } else {
                        comp = CompareUtil.compareToJISX0208(key, pattern, true);
                    }
                }
                break;
            case WORD:
            case ENDWORD:
            case KEYWORD:
            case CROSS:
            default:
                comp = CompareUtil.compareToByte(key, pattern, true);
                break;
        }
        try {
            _logger.debug("compare key word: (" + comp + ") '"
                          + new String(key, "x-JIS0208") + "' '"
                          + new String(pattern, "x-JIS0208") + "'");
        } catch (UnsupportedEncodingException e) {
        }
        return comp;
    }

    /**
     * キーとパターンを比較します。
     *
     * @param key キー
     * @param pattern パターン
     * @return キーがパターンと同じ場合:0、
     *         キーがパターンより大きい場合:1以上、
     *         キーがパターンより小さい場合:-1以下
     */
    private int _compareSingle(final byte[] key, final byte[] pattern) {
        int comp = 0;
        switch (_type) {
            case ENDWORD:
                if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                    comp = CompareUtil.compareToByte(key, pattern, false);
                } else {
                    IndexStyle style = _sub.getEndwordIndexStyle(SubBook.KANA);
                    if (style != null && _style.getStartPage() == style.getStartPage()) {
                        comp = CompareUtil.compareToKanaSingle(key, pattern, false);
                    } else {
                        comp = CompareUtil.compareToByte(key, pattern, false);
                    }
                }
                break;
            case EXACTWORD:
                if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                    comp = CompareUtil.compareToLatin(key, pattern, false);
                } else {
                    IndexStyle style = _sub.getWordIndexStyle(SubBook.KANA);
                    if (style != null && _style.getStartPage() == style.getStartPage()) {
                        comp = CompareUtil.compareToKanaSingle(key, pattern, true);
                    } else {
                        comp = CompareUtil.compareToJISX0208(key, pattern, false);
                    }
                }
                break;
            case KEYWORD:
            case CROSS:
                comp = CompareUtil.compareToByte(key, pattern, false);
                break;
            case MULTI:
                if (_style.getCandidatePage() == 0) {
                    comp = CompareUtil.compareToByte(key, pattern, false);
                } else {
                    if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                        comp = CompareUtil.compareToLatin(key, pattern, false);
                    } else {
                        comp = CompareUtil.compareToJISX0208(key, pattern, false);
                    }
                }
                break;
            case WORD:
            default:
                if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                    comp = CompareUtil.compareToByte(key, pattern, false);
                } else {
                    IndexStyle style = _sub.getWordIndexStyle(SubBook.KANA);
                    if (style != null && _style.getStartPage() == style.getStartPage()) {
                        comp = CompareUtil.compareToKanaSingle(key, pattern, false);
                    } else {
                        comp = CompareUtil.compareToByte(key, pattern, false);
                    }
                }
                break;
        }
        try {
            _logger.debug("compare key word: (" + comp + ") '"
                          + new String(key, "x-JIS0208") + "' '"
                          + new String(pattern, "x-JIS0208") + "'");
        } catch (UnsupportedEncodingException e) {
        }
        return comp;
    }

    /**
     * キーとパターンを比較します。
     *
     * @param key キー
     * @param pattern パターン
     * @return キーがパターンと同じ場合:0、
     *         キーがパターンより大きい場合:1以上、
     *         キーがパターンより小さい場合:-1以下
     */
    private int _compareGroup(final byte[] key, final byte[] pattern) {
        int comp = 0;
        switch (_type) {
            case EXACTWORD:
                if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                    comp = CompareUtil.compareToLatin(key, pattern, false);
                } else {
                    comp = CompareUtil.compareToKanaGroup(key, pattern, true);
                }
                break;
            case MULTI:
                if (_style.getCandidatePage() == 0) {
                    if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                        comp = CompareUtil.compareToByte(key, pattern, false);
                    } else {
                        comp = CompareUtil.compareToKanaGroup(key, pattern, false);
                    }
                } else {
                    if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                        comp = CompareUtil.compareToLatin(key, pattern, false);
                    } else {
                        comp = CompareUtil.compareToKanaGroup(key, pattern, true);
                    }
                }
                break;
            case WORD:
            case ENDWORD:
            case KEYWORD:
            case CROSS:
            default:
                if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                    comp = CompareUtil.compareToByte(key, pattern, false);
                } else {
                    comp = CompareUtil.compareToKanaGroup(key, pattern, false);
                }
                break;
        }
        try {
            _logger.debug("compare key word: (" + comp + ") '"
                          + new String(key, "x-JIS0208") + "' '"
                          + new String(pattern, "x-JIS0208") + "'");
        } catch (UnsupportedEncodingException e) {
        }
        return comp;
    }

    /**
     * 検索を行います。
     *
     * @param word 検索語
     * @exception EBException 前処理中にエラーが発生した場合
     */
    protected void search(final byte[] word) throws EBException {
        _setWord(word);
        _page = _style.getStartPage();

        // pre-search
        BookInputStream bis = _file.getInputStream();
        try {
            long nextPage = _page;
            int depth;
            for (depth=0; depth<MAX_INDEX_DEPTH; depth++) {
                // データをキャッシュへ読み込む
                bis.seek(_page, 0);
                bis.readFully(_cache, 0, _cache.length);
                _cachePage = _page;

                _pageID = _cache[0] & 0xff;
                _entryLength = _cache[1] & 0xff;
                if (_entryLength == 0) {
                    _entryArrangement = VARIABLE;
                } else {
                    _entryArrangement = FIXED;
                }
                _entryCount = ByteUtil.getInt2(_cache, 2);
                _off = 4;

                _logger.debug("page=0x" + HexUtil.toHexString(_page)
                              + ", ID=0x" + HexUtil.toHexString(_pageID));
                // リーフインデックスに達っしたらループ終了
                if (_isLeafLayer(_pageID)) {
                    break;
                }

                // 次のレベルのインデックスを取得する
                byte[] b = new byte[_entryLength];
                for (_entryIndex=0; _entryIndex<_entryCount; _entryIndex++) {
                    if (_off + _entryLength + 4 > BookInputStream.PAGE_SIZE) {
                        throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                    }
                    System.arraycopy(_cache, _off, b, 0, b.length);
                    _off += _entryLength;
                    if (_comparePre(_canonical, b) <= 0) {
                        nextPage = ByteUtil.getLong4(_cache, _off);
                        break;
                    }
                    _off += 4;
                }
                if (_entryIndex >= _entryCount || nextPage == _page) {
                    _comparison = -1;
                    return;
                }
                _page = nextPage;
            }

            // インデックス深さのチェック
            if (depth == MAX_INDEX_DEPTH) {
                throw new EBException(EBException.UNEXP_FILE, _file.getPath());
            }
        } finally {
            bis.close();
        }
        _entryIndex = 0;
        _comparison = 1;
        _inGroupEntry = false;
    }

    /**
     * 次の検索結果を返します。
     *
     * @return 検索結果 (次の検索結果がない場合null)
     * @exception EBException 検索中にエラーが発生した場合
     */
    @Override
    public Result getNextResult() throws EBException {
        if (_comparison < 0) {
            return null;
        }

        while (true) {
            refreshCache();

            if (!_isLeafLayer(_pageID)) {
                // リーフインデックスでなければ例外
                throw new EBException(EBException.UNEXP_FILE, _file.getPath());
            }

            if (!_hasGroupEntry(_pageID)) {
                Result result;
                while (_entryIndex < _entryCount) {
                    result = getNonGroupEntry();
                    if (result != null) {
                        return result;
                    }
                    if (_comparison < 0) {
                        return null;
                    }
                }
            } else {
                Result result;
                while (_entryIndex < _entryCount) {
                    result = getGroupedEntry();
                    if (result != null) {
                        return result;
                    }
                    if (_comparison < 0) {
                        return null;
                    }
                }
            }

            // 次ページが存在すれば続行、存在しなければ終了
            if (_isLayerEnd(_pageID)) {
                _comparison = -1;
                break;
            }
            _page++;
            _entryIndex = 0;
        }
        return null;
    }

    /**
     * 指定されたページが最下層かどうかを判別します。
     *
     * @param id ページID
     * @return 最下層である場合はtrue、そうでない場合はfalse
     */
    private boolean _isLeafLayer(final int id) {
        if ((id & 0x80) == 0x80) {
            return true;
        }
        return false;
    }

    // /**
    //  * 指定されたページが階層開始ページかどうかを判別します。
    //  *
    //  * @param id ページID
    //  * @return 階層開始ページである場合はtrue、そうでない場合はfalse
    //  */
    // private boolean _isLayerStart(int id) {
    //     if ((id & 0x40) == 0x40) {
    //         return true;
    //     }
    //     return false;
    // }

    /**
     * 指定されたページが階層終了ページかどうかを判別します。
     *
     * @param id ページID
     * @return 階層終了ページである場合はtrue、そうでない場合はfalse
     */
    private boolean _isLayerEnd(final int id) {
        if ((id & 0x20) == 0x20) {
            return true;
        }
        return false;
    }

    /**
     * 指定されたページがグループエントリを含んでいるかどうか判別します。
     *
     * @param id ページID
     * @return グループエントリを含んでいる場合はtrue、そうでない場合はfalse
     */
    private boolean _hasGroupEntry(final int id) {
        if ((id & 0x10) == 0x10) {
            return true;
        }
        return false;
    }

    // キャッシュとデータのページが異なれば読み込む
    private void refreshCache() throws EBException {
        if (_cachePage != _page) {
            BookInputStream bis = _file.getInputStream();
            try {
                bis.seek(_page, 0);
                bis.readFully(_cache, 0, _cache.length);
            } finally {
                bis.close();
            }
            _cachePage = _page;

            if (_entryIndex == 0) {
                _pageID = _cache[0] & 0xff;
                _entryLength = _cache[1] & 0xff;
                if (_entryLength == 0) {
                    _entryArrangement = VARIABLE;
                } else {
                    _entryArrangement = FIXED;
                }
                _entryCount = ByteUtil.getInt2(_cache, 2);
                _entryIndex = 0;
                _off = 4;
                _logger.debug("page=0x" + HexUtil.toHexString(_page)
                              + ", ID=0x" + HexUtil.toHexString(_pageID));
            }
        }
    }

    // グループエントリなし
    private Result getNonGroupEntry() throws EBException {
        if (_entryArrangement == VARIABLE) {
            if (_off + 1 > BookInputStream.PAGE_SIZE) {
                throw new EBException(EBException.UNEXP_FILE, _file.getPath());
            }
            _entryLength = _cache[_off] & 0xff;
            _off++;
        }

        if (_off + _entryLength + 12 > BookInputStream.PAGE_SIZE) {
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }

        byte[] b = new byte[_entryLength];
        System.arraycopy(_cache, _off, b, 0, b.length);
        _off += _entryLength;

        _comparison = _compareSingle(_word, b);
        Result result = null;
        if (_comparison == 0) {
            // 本文/見出し位置の取得
            long tPage = ByteUtil.getLong4(_cache, _off);
            int tOff = ByteUtil.getInt2(_cache, _off+4);
            long hPage = ByteUtil.getLong4(_cache, _off+6);
            int hOff = ByteUtil.getInt2(_cache, _off+10);
            result = new Result(_sub, hPage, hOff, tPage, tOff);
        }

        _entryIndex++;
        _off += 12;

        return result;
    }

    // グループエントリあり
    private Result getGroupedEntry() throws EBException {
        if (_off + 2 > BookInputStream.PAGE_SIZE) {
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }
        int groupID = _cache[_off] & 0xff;
        Result result = null;
        if (groupID == 0x00) {
            // シングルエントリ
            _entryLength = _cache[_off+1] & 0xff;
            if (_off + _entryLength + 14 > BookInputStream.PAGE_SIZE) {
                throw new EBException(EBException.UNEXP_FILE, _file.getPath());
            }

            byte[] b = new byte[_entryLength];
            System.arraycopy(_cache, _off+2, b, 0, b.length);
            _off += _entryLength + 2;

            _comparison = _compareSingle(_canonical, b);
            if (_comparison == 0) {
                // 本文/見出し位置の取得
                long tPage = ByteUtil.getLong4(_cache, _off);
                int tOff = ByteUtil.getInt2(_cache, _off+4);
                long hPage = ByteUtil.getLong4(_cache, _off+6);
                int hOff = ByteUtil.getInt2(_cache, _off+10);
                result = new Result(_sub, hPage, hOff, tPage, tOff);
            }
            _off += 12;
            _inGroupEntry = false;
        } else if (groupID == 0x80) {
            // グループエントリの開始
            _entryLength = _cache[_off+1] & 0xff;
            byte[] b = new byte[_entryLength];
            if (_type == KEYWORD || _type == CROSS) {
                if (_off + _entryLength + 12 > BookInputStream.PAGE_SIZE) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }

                System.arraycopy(_cache, _off+6, b, 0, b.length);
                _off += _entryLength + 6;
                _comparison = _compareSingle(_word, b);
                long hPage = ByteUtil.getLong4(_cache, _off);
                int hOff = ByteUtil.getInt2(_cache, _off+4);
                _keywordHeading =
                    BookInputStream.getPosition(hPage, hOff);
                _off += 6;
            } else if (_type == MULTI) {
                if (_off + _entryLength + 6 > BookInputStream.PAGE_SIZE) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }

                System.arraycopy(_cache, _off+6, b, 0, b.length);
                _comparison = _compareSingle(_word, b);
                _off += _entryLength + 6;
            } else {
                if (_off + _entryLength + 4 > BookInputStream.PAGE_SIZE) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }

                System.arraycopy(_cache, _off+4, b, 0, b.length);
                _comparison = _compareSingle(_canonical, b);
                _off += _entryLength + 4;
            }
            _inGroupEntry = true;
        } else if (groupID == 0xc0) {
            // グループエントリの要素
            if (_type == KEYWORD || _type == CROSS) {
                if (_off + 7 > BookInputStream.PAGE_SIZE) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }

                if (_comparison == 0 && _inGroupEntry) {
                    // 本文/見出し位置の取得
                    long tPage = ByteUtil.getLong4(_cache, _off+1);
                    int tOff = ByteUtil.getInt2(_cache, _off+5);
                    result = new Result(_sub, _keywordHeading, tPage, tOff);
                    _keywordHeading =
                        _sub.getNextHeadingPosition(_keywordHeading);
                }
                _off += 7;
            } else if (_type == MULTI) {
                if (_off + 13 > BookInputStream.PAGE_SIZE) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }

                if (_comparison == 0 && _inGroupEntry) {
                    // 本文/見出し位置の取得
                    long tPage = ByteUtil.getLong4(_cache, _off+1);
                    int tOff = ByteUtil.getInt2(_cache, _off+5);
                    long hPage = ByteUtil.getLong4(_cache, _off+7);
                    int hOff = ByteUtil.getInt2(_cache, _off+11);
                    result = new Result(_sub, hPage, hOff, tPage, tOff);
                }
                _off += 13;
            } else {
                _entryLength = _cache[_off+1] & 0xff;
                if (_off + _entryLength + 14 > BookInputStream.PAGE_SIZE) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }

                byte[] b = new byte[_entryLength];
                System.arraycopy(_cache, _off+2, b, 0, b.length);
                _off += _entryLength + 2;
                if (_comparison == 0 && _inGroupEntry
                    && _compareGroup(_word, b) == 0) {
                    // 本文/見出し位置の取得
                    long tPage = ByteUtil.getLong4(_cache, _off);
                    int tOff = ByteUtil.getInt2(_cache, _off+4);
                    long hPage = ByteUtil.getLong4(_cache, _off+6);
                    int hOff = ByteUtil.getInt2(_cache, _off+10);
                    result = new Result(_sub, hPage, hOff, tPage, tOff);
                }
                _off += 12;
            }
        } else {
            // 未知のID
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }

        _entryIndex++;

        return result;
    }
}

// end of SingleWordSearcher.java
