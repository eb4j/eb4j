package fuku.eb4j;

import java.io.File;

import fuku.eb4j.io.EBFile;
import fuku.eb4j.io.BookInputStream;
import fuku.eb4j.util.ByteUtil;

/**
 * 付録パッケージクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Appendix {

    /** 付録パッケージのディレクトリ */
    private String _appendixPath = null;

    /** 付録パッケージの種類 */
    private int _appendixType = -1;

    /** 付録パッケージの副本 */
    private SubAppendix[] _sub = null;


    /**
     * コンストラクタ。
     *
     * @param path 付録パッケージのパス
     * @exception EBException 初期化中にエラーが発生した場合
     */
    public Appendix(String path) throws EBException {
        this(new File(path));
    }

    /**
     * コンストラクタ。
     *
     * @param dir 付録パッケージのパス
     * @exception EBException 初期化中にエラーが発生した場合
     */
    public Appendix(File dir) throws EBException {
        super();

        _appendixPath = dir.getPath();
        if (!dir.isDirectory()) {
            throw new EBException(EBException.DIR_NOT_FOUND, _appendixPath);
        }
        if (!dir.canRead()) {
            throw new EBException(EBException.CANT_READ_DIR, _appendixPath);
        }
        _loadCatalog(dir);
    }


    /**
     * この付録パッケージのパスを文字列で返します。
     *
     * @return 付録パッケージのパスの文字列形式
     */
    public String getPath() {
        return _appendixPath;
    }

    /**
     * この付録パッケージの種類を返します。
     *
     * @return appendixパッケージの種類を示すフラグ
     * @see Book#DISC_EB
     * @see Book#DISC_EPWING
     */
    public int getAppendixType() {
        return _appendixType;
    }

    /**
     * この付録パッケージの副本数を返します。
     *
     * @return 副本数
     */
    public int getSubAppendixCount() {
        int ret = 0;
        if (_sub != null) {
            ret = _sub.length;
        }
        return ret;
    }

    /**
     * この付録パッケージの副本リストを返します。
     *
     * @return 副本の配列
     */
    public SubAppendix[] getSubAppendixes() {
        if (_sub == null) {
            return new SubAppendix[0];
        }
        int len = _sub.length;
        SubAppendix[] list = new SubAppendix[len];
        System.arraycopy(_sub, 0, list, 0, len);
        return list;
    }

    /**
     * この付録パッケージの指定したインデックスの副本を返します。
     *
     * @param index インデックス
     * @return 副本 (範囲外のインデックス時はnull)
     */
    public SubAppendix getSubAppendix(int index) {
        if (index < 0 || index >= _sub.length) {
            return null;
        }
        return _sub[index];
    }

    /**
     * CATALOG(S)ファイルから情報を読み込みます。
     *
     * @param dir 付録パッケージのディレクトリ
     * @exception EBException CATALOG(S)ファイルの読み込み中にエラーが発生した場合
     */
    private void _loadCatalog(File dir) throws EBException {
        // カタログファイルの検索
        EBFile file = null;
        try {
            file = new EBFile(dir, "catalog", EBFile.FORMAT_PLAIN);
            _appendixType = Book.DISC_EB;
        } catch (EBException e) {
            file = new EBFile(dir, "catalogs", EBFile.FORMAT_PLAIN);
            _appendixType = Book.DISC_EPWING;
        }

        BookInputStream bis = file.getInputStream();
        try {
            byte[] b = new byte[16];
            bis.readFully(b, 0, b.length);

            // 副本数の取得
            int subCount = ByteUtil.getInt2(b, 0);
            if (subCount <= 0) {
                throw new EBException(EBException.UNEXP_FILE, file.getPath());
            }

            // 副本の情報を取得
            _sub = new SubAppendix[subCount];
            b = new byte[Book.SIZE_CATALOG[_appendixType]];
            int off = 2 + Book.SIZE_TITLE[_appendixType];
            for (int i=0; i<subCount; i++) {
                bis.readFully(b, 0, b.length);
                // 副本オブジェクトの作成
                String path = new String(b, off, Book.SIZE_DIRNAME).trim();
                _sub[i] = new SubAppendix(this, path);
            }
        } finally {
            bis.close();
        }
    }
}

// end of Appendix.java
