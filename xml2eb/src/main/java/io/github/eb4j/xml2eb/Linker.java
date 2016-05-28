package io.github.eb4j.xml2eb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.util.HexUtil;

/**
 * リンカークラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Linker {

    private static final int BODY = 0;
    private static final int MENU = 1;
    private static final int COPYRIGHT = 2;
    private static final int HEAD = 3;
    private static final int WORD = 4;
    private static final int ENDWORD = 5;
    private static final int KEYWORD = 6;
    private static final int GRAPHIC = 7;
    private static final int SOUND = 8;

    /** ログ */
    private Logger _logger = null;
    /** 出力ファイル */
    private File _outfile = null;
    /** 入力ファイル */
    private File[] _infile = null;
    /** オフセット位置 */
    private long[] _startBlock = null;

    /** 参照情報 */
    private Reference _ref = null;
    /** ファイルマップ */
    private Map<File,RandomAccessFile> _fileMap = null;


    /**
     * コンストラクタ。
     *
     * @param file 出力ファイル
     */
    public Linker(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _outfile = file;
        _infile = new File[9];
        _startBlock = new long[9];
        _fileMap = new HashMap<File,RandomAccessFile>();
    }


    /**
     * 見出しファイルを設定します。
     *
     * @param file 見出しファイル
     */
    public void setHeadFile(File file) {
        _infile[HEAD] = file;
    }

    /**
     * 本文ファイルを設定します。
     *
     * @param file 本文ファイル
     */
    public void setBodyFile(File file) {
        _infile[BODY] = file;
    }

    /**
     * 著作権ファイルを設定します。
     *
     * @param file 著作権ファイル
     */
    public void setCopyrightFile(File file) {
        _infile[COPYRIGHT] = file;
    }

    /**
     * メニューファイルを設定します。
     *
     * @param file メニューファイル
     */
    public void setMenuFile(File file) {
        _infile[MENU] = file;
    }

    /**
     * 前方一致インデックスベースファイルを設定します。
     *
     * @param file インデックスベースファイル
     */
    public void setWordFile(File file) {
        _infile[WORD] = file;
    }

    /**
     * 後方一致インデックスベースファイルを設定します。
     *
     * @param file インデックスベースファイル
     */
    public void setEndwordFile(File file) {
        _infile[ENDWORD] = file;
    }

    /**
     * キーワードインデックスベースファイルを設定します。
     *
     * @param file インデックスベースファイル
     */
    public void setKeywordFile(File file) {
        _infile[KEYWORD] = file;
    }

    /**
     * 画像ファイルを設定します。
     *
     * @param file 画像ファイル
     */
    public void setGraphicFile(File file) {
        _infile[GRAPHIC] = file;
    }

    /**
     * 音声ファイルを設定します。
     *
     * @param file 音声ファイル
     */
    public void setSoundFile(File file) {
        _infile[SOUND] = file;
    }

    /**
     * 参照情報を設定します。
     *
     * @param ref 参照情報
     */
    public void setReference(Reference ref) {
        _ref = ref;
    }

    /**
     * 指定ファイル位置の結合後位置を返します。
     *
     * @param pos ファイル位置
     * @return 結合後のファイル位置
     */
    private long _getPosition(Position pos) {
        File file = pos.getFile();
        int len = _infile.length;
        for (int i=0; i<len; i++) {
            if (file.equals(_infile[i])) {
                return pos.getPosition(_startBlock[i]);
            }
        }
        _logger.warn("unknown file position: " + pos);
        return pos.getPosition();
    }

    /**
     * 指定ファイルがインデックスファイルかどうかを返します。
     *
     * @param file ファイル
     * @return インデックスファイルの場合はtrue、そうでない場合はfalse
     */
    private boolean _isIndex(File file) {
        String name = file.getName();
        int len = _infile.length;
        for (int i=0; i<len; i++) {
            if (_infile[i] != null) {
                if (file.equals(_infile[i])) {
                    return false;
                }
                String base = _infile[i].getName();
                if (name.startsWith(base)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ファイルリストを返します。
     *
     * @param file ベースファイル
     * @return ファイルリスト
     */
    private File[] _getFileList(File file) {
        ArrayList<File> list = new ArrayList<File>();
        String name = file.getName();
        File dir = file.getParentFile();
        if (dir == null) {
            dir = new File(".");
        }
        File[] files = dir.listFiles();
        int n = files.length;
        for (int i=0; i<n; i++) {
            if (files[i].getName().startsWith(name)) {
                list.add(files[i]);
            }
        }
        return list.toArray(new File[list.size()]);
    }

    /**
     * 一時ファイルを削除します。
     *
     */
    public void delete() {
        int len = _infile.length;
        for (int i=0; i<len; i++) {
            if (_infile[i] != null) {
                if (_infile[i].exists()) {
                    _logger.info("delete file" + _infile[i].getPath());
                    if (!_infile[i].delete()) {
                        _logger.error("failed to delete file: " + _infile[i].getPath());
                    }
                } else {
                    File[] files = _getFileList(_infile[i]);
                    int n = files.length;
                    for (int j=0; j<n; j++) {
                        _logger.info("delete file" + files[j].getPath());
                        if (!files[j].delete()) {
                            _logger.error("failed to delete file: " + files[j].getPath());
                        }
                    }
                }
            }
        }
    }

    /**
     * 各ファイルを結合します。
     *
     */
    public void link() {
        // 書籍管理情報
        byte[] control = new byte[2048];
        Arrays.fill(control, (byte)0x00);
        int off = 16;
        int cnt = 0;
        long block = 2;
        long size;
        if (_infile[BODY] != null) {
            _logger.debug("body start block: 0x" + HexUtil.toHexString(block));
            _startBlock[BODY] = block;
            size = _setControlEntry(control, off, _infile[BODY],
                                    0x00, block, 0x02000000);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[MENU] != null) {
            _logger.debug("menu start block: 0x" + HexUtil.toHexString(block));
            _startBlock[MENU] = block;
            size = _setControlEntry(control, off, _infile[MENU],
                                    0x01, block, 0x02000000);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[COPYRIGHT] != null) {
            _logger.debug("copyright start block: 0x" + HexUtil.toHexString(block));
            _startBlock[COPYRIGHT] = block;
            size = _setControlEntry(control, off, _infile[COPYRIGHT],
                                    0x02, block, 0x02000000);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[HEAD] != null) {
            _logger.debug("head start block: 0x" + HexUtil.toHexString(block));
            _startBlock[HEAD] = block;
            size = _setControlEntry(control, off, _infile[HEAD],
                                    0x05, block, 0x02000000);
            off += 16;
            cnt++;
            size = _setControlEntry(control, off, _infile[HEAD],
                                    0x07, block, 0x02000000);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[WORD] != null) {
            _logger.debug("word start block: 0x" + HexUtil.toHexString(block));
            _startBlock[WORD] = block;
            size = _setControlEntry(control, off, _getFileList(_infile[WORD]),
                                    0x91, block, 0x02415554);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[ENDWORD] != null) {
            _logger.debug("endword start block: 0x" + HexUtil.toHexString(block));
            _startBlock[ENDWORD] = block;
            size = _setControlEntry(control, off, _getFileList(_infile[ENDWORD]),
                                    0x71, block, 0x02415554);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[KEYWORD] != null) {
            _logger.debug("keyword start block: 0x" + HexUtil.toHexString(block));
            _startBlock[KEYWORD] = block;
            size = _setControlEntry(control, off, _getFileList(_infile[KEYWORD]),
                                    0x80, block, 0x02415554);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[GRAPHIC] != null) {
            _logger.debug("graphic start block: 0x" + HexUtil.toHexString(block));
            _startBlock[GRAPHIC] = block;
            size = _setControlEntry(control, off, _infile[GRAPHIC],
                                    0xd2, block, 0x00000000);
            off += 16;
            cnt++;
            block += size;
        }
        if (_infile[SOUND] != null) {
            _logger.debug("sound start block: 0x" + HexUtil.toHexString(block));
            _startBlock[SOUND] = block;
            size = _setControlEntry(control, off, _infile[SOUND],
                                    0xd8, block, 0x00000000);
            off += 16;
            cnt++;
            block += size;
        }
        control[0] = (byte)((cnt >>> 8) & 0xff);
        control[1] = (byte)(cnt & 0xff);

        // 参照情報を解決する
        _fixBodyReference();
        _fixHeadReference();
        _fixIndexReference();
        _fixGraphicReference();
        _fixSoundReference();

        // ストリームを閉じる
        Iterator<RandomAccessFile> it = _fileMap.values().iterator();
        while (it.hasNext()) {
            RandomAccessFile stream = it.next();
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
        _fileMap.clear();

        // ファイルの結合
        _logger.info("link file: " + _outfile.getPath());
        BlockOutputStream out = null;
        try {
            out =
                new BlockOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(_outfile)));
            out.write(control, 0, control.length);
            out.flush();
            int len = _infile.length;
            for (int i=0; i<len; i++) {
                if (_infile[i] != null) {
                    _link(out, _infile[i]);
                }
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 書籍管理情報を設定します。
     *
     * @param b 書籍管理情報
     * @param off 設定位置
     * @param file データファイル
     * @param id 書籍構成要素識別子
     * @param start 先頭ブロック
     * @param param パラメータ
     * @return この書籍構成要素のブロック数
     */
    private long _setControlEntry(byte[] b, int off, File file,
                                  int id, long start, long param) {
        File[] files = {file};
        return _setControlEntry(b, off, files, id, start, param);
    }

    /**
     * 書籍管理情報を設定します。
     *
     * @param b 書籍管理情報
     * @param off 設定位置
     * @param file データファイル
     * @param id 書籍構成要素識別子
     * @param start 先頭ブロック
     * @param param パラメータ
     * @return この書籍構成要素のブロック数
     */
    private long _setControlEntry(byte[] b, int off, File[] file,
                                  int id, long start, long param) {
        long size = 0;
        int n = file.length;
        for (int i=0; i<n; i++) {
            long len = file[i].length();
            size += ((len + 2047) / 2048);
        }
        b[off++] = (byte)id;
        b[off++] = (byte)0x00;
        b[off++] = (byte)((start >>> 24) & 0xff);
        b[off++] = (byte)((start >>> 16) & 0xff);
        b[off++] = (byte)((start >>> 8) & 0xff);
        b[off++] = (byte)(start & 0xff);
        b[off++] = (byte)((size >>> 24) & 0xff);
        b[off++] = (byte)((size >>> 16) & 0xff);
        b[off++] = (byte)((size >>> 8) & 0xff);
        b[off++] = (byte)(param & 0xff);
        b[off++] = (byte)((param >>> 24) & 0xff);
        b[off++] = (byte)((param >>> 16) & 0xff);
        b[off++] = (byte)((param >>> 8) & 0xff);
        b[off++] = (byte)(param & 0xff);
        return size;
    }

    /**
     * 本文参照を修正します。
     *
     */
    private void _fixBodyReference() {
        Map<Position,String> map = _ref.getBodyRef();
        _logger.info("resolve body reference: " + map.size());
        Iterator<Map.Entry<Position,String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position,String> entry = it.next();
            Position pos = entry.getKey();
            String tag = entry.getValue();
            if (_ref.hasBodyTag(tag)) {
                Position tpos = _ref.getBodyTag(tag);
                _fixPosition(pos, _getPosition(tpos));
            } else {
                _logger.error("undefined body tag: " + tag);
            }
        }
    }

    /**
     * 見出し参照を修正します。
     *
     */
    private void _fixHeadReference() {
        Map<Position,String> map = _ref.getHeadRef();
        _logger.info("resolve head reference: " + map.size());
        Iterator<Map.Entry<Position,String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position,String> entry = it.next();
            Position pos = entry.getKey();
            String tag = entry.getValue();
            if (_ref.hasHeadTag(tag)) {
                Position tpos = _ref.getHeadTag(tag);
                _fixPosition(pos, _getPosition(tpos));
            } else {
                _logger.error("undefined head tag: " + tag);
            }
        }
    }

    /**
     * インデックス参照を修正します。
     *
     */
    private void _fixIndexReference() {
        Map<Position,String> map = _ref.getIndexRef();
        _logger.info("resolve index reference: " + map.size());
        Iterator<Map.Entry<Position,String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position,String> entry = it.next();
            Position pos = entry.getKey();
            String tag = entry.getValue();
            _fixPosition(pos, tag);
        }
    }

    /**
     * 画像参照を修正します。
     *
     */
    private void _fixGraphicReference() {
        Map<Position,String> map = _ref.getGraphicRef();
        _logger.info("resolve graphic reference: " + map.size());
        Iterator<Map.Entry<Position,String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position,String> entry = it.next();
            Position pos = entry.getKey();
            String tag = entry.getValue();
            if (_ref.hasGraphicTag(tag)) {
                Position tpos = _ref.getGraphicTag(tag);
                _fixPosition(pos, _getPosition(tpos));
            } else {
                _logger.error("undefined graphic tag: " + tag);
            }
        }
    }

    /**
     * 音声参照を修正します。
     *
     */
    private void _fixSoundReference() {
        Map<Position,String> map = _ref.getSoundRef();
        _logger.info("resolve sound reference:" + map.size());
        Iterator<Map.Entry<Position,String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position,String> entry = it.next();
            Position pos = entry.getKey();
            String tag = entry.getValue();
            if (_ref.hasSoundTag(tag)) {
                Position[] tpos = _ref.getSoundTag(tag);
                _fixPosition(pos, _getPosition(tpos[0]), _getPosition(tpos[1]));
            } else {
                _logger.error("undefined sound tag: " + tag);
            }
        }
    }

    /**
     * ファイルの参照情報を修正します。
     *
     * @param pos ファイル位置
     * @param target 参照先
     */
    private void _fixPosition(Position pos, long target) {
        File file = pos.getFile();
        byte[] b = new byte[6];
        long block = target / 2048 + 1;
        int off = (int)(target % 2048);
        if (!_isIndex(file)) {
            block = _toBCD4(block);
            off = _toBCD2(off);
        }
        b[0] = (byte)((block >>> 24) & 0xff);
        b[1] = (byte)((block >>> 16) & 0xff);
        b[2] = (byte)((block >>> 8) & 0xff);
        b[3] = (byte)(block & 0xff);
        b[4] = (byte)((off >>> 8) & 0xff);
        b[5] = (byte)(off & 0xff);
        try {
            RandomAccessFile stream = _fileMap.get(file);
            if (stream == null) {
                stream = new RandomAccessFile(file, "rw");
                _fileMap.put(file, stream);
            }
            stream.seek(pos.getPosition());
            stream.write(b, 0, b.length);
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        }
    }

    /**
     * ファイルの参照情報を修正します。
     *
     * @param pos ファイル位置
     * @param start 参照先開始位置
     * @param end 参照先終了位置
     */
    private void _fixPosition(Position pos, long start, long end) {
        byte[] b = new byte[12];
        long block = start / 2048 + 1;
        int off = (int)(start % 2048);
        block = _toBCD4(block);
        off = _toBCD2(off);
        b[0] = (byte)((block >>> 24) & 0xff);
        b[1] = (byte)((block >>> 16) & 0xff);
        b[2] = (byte)((block >>> 8) & 0xff);
        b[3] = (byte)(block & 0xff);
        b[4] = (byte)((off >>> 8) & 0xff);
        b[5] = (byte)(off & 0xff);
        block = end / 2048 + 1;
        off = (int)(end % 2048);
        block = _toBCD4(block);
        off = _toBCD2(off);
        b[6] = (byte)((block >>> 24) & 0xff);
        b[7] = (byte)((block >>> 16) & 0xff);
        b[8] = (byte)((block >>> 8) & 0xff);
        b[9] = (byte)(block & 0xff);
        b[10] = (byte)((off >>> 8) & 0xff);
        b[11] = (byte)(off & 0xff);
        try {
            File file = pos.getFile();
            RandomAccessFile stream = _fileMap.get(file);
            if (stream == null) {
                stream = new RandomAccessFile(file, "rw");
                _fileMap.put(file, stream);
            }
            stream.seek(pos.getPosition());
            stream.write(b, 0, b.length);
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        }
    }

    /**
     * ファイルの参照情報を修正します。
     *
     * @param pos ファイル位置
     * @param tag インデックスタグ
     */
    private void _fixPosition(Position pos, String tag) {
        File file = pos.getFile();
        File dir = file.getParentFile();
        if (dir == null) {
            dir = new File(".");
        }
        String name = file.getName();
        String base = null;
        int level = 0;
        long block = 0;
        try {
            int idx = name.lastIndexOf(".");
            level = Integer.parseInt(name.substring(idx+1));
            base = name.substring(0, idx);
            block = Integer.parseInt(tag);
        } catch (NumberFormatException e) {
            _logger.warn(e.getMessage(), e);
        }
        if (level <= 0) {
            _logger.error("unknown index file: " + name);
            return;
        }
        if (block <= 0) {
            _logger.error("unknown index tag: " + tag);
            return;
        }
        long size = 0;
        File[] indexFile = _getFileList(new File(dir, base));
        Arrays.sort(indexFile);
        int len = indexFile.length;
        for (int i=len-1; i>=0; i--) {
            try {
                String str = indexFile[i].getName();
                int idx = str.lastIndexOf(".");
                int no = Integer.parseInt(str.substring(idx+1));
                if (no < level) {
                    break;
                }
                size += indexFile[i].length();
            } catch (NumberFormatException e) {
                _logger.warn(e.getMessage(), e);
            }
        }
        block += (size / 2048);
        if (_infile[WORD] != null
            && base.equals(_infile[WORD].getName())) {
            block += _startBlock[WORD];
        } else if (_infile[ENDWORD] != null
                   && base.equals(_infile[ENDWORD].getName())) {
            block += _startBlock[ENDWORD];
        } else if (_infile[KEYWORD] != null
                   && base.equals(_infile[KEYWORD].getName())) {
            block += _startBlock[KEYWORD];
        } else {
            _logger.error("unknown index file: " + name);
            return;
        }
        block--;
        _logger.debug(pos + ": 0x" + HexUtil.toHexString(block) + " tag=" + tag);
        byte[] b = new byte[4];
        b[0] = (byte)((block >>> 24) & 0xff);
        b[1] = (byte)((block >>> 16) & 0xff);
        b[2] = (byte)((block >>> 8) & 0xff);
        b[3] = (byte)(block & 0xff);
        try {
            RandomAccessFile stream = _fileMap.get(file);
            if (stream == null) {
                stream = new RandomAccessFile(file, "rw");
                _fileMap.put(file, stream);
            }
            stream.seek(pos.getPosition());
            stream.write(b, 0, b.length);
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        }
    }

    /**
     * 指定されたファイルの内容を指定されたストリームに出力します。
     *
     * @param out 出力ストリーム
     * @param file ファイル
     */
    private void _link(OutputStream out, File file) {
        if (!file.exists()) {
            File[] files = _getFileList(file);
            Arrays.sort(files);
            int len = files.length;
            for (int i=len-1; i>=0; i--) {
                _link(out, files[i]);
            }
            return;
        }
        _logger.info("link file: " + file.getPath());
        byte[] b = new byte[2048];
        int n;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            while (true) {
                n = bis.read(b, 0, b.length);
                if (n < 0) {
                    break;
                }
                out.write(b, 0, n);
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    /**
     * 指定された値をBCDに変換します。
     *
     * @param val 値
     * @return BCD値
     */
    private long _toBCD4(long val) {
        long bcd = 0;
        bcd += (val % 10);
        bcd += (((val / 10) % 10) << 4);
        bcd += (((val / 100) % 10) << 8);
        bcd += (((val / 1000) % 10) << 12);
        bcd += (((val / 10000) % 10) << 16);
        bcd += (((val / 100000) % 10) << 20);
        bcd += (((val / 1000000) % 10) << 24);
        bcd += (((val / 10000000) % 10) << 28);
        return bcd;
    }

    /**
     * 指定された値をBCDに変換します。
     *
     * @param val 値
     * @return BCD値
     */
    private int _toBCD2(int val) {
        int bcd = 0;
        bcd += (val % 10);
        bcd += (((val / 10) % 10) << 4);
        bcd += (((val / 100) % 10) << 8);
        bcd += (((val / 1000) % 10) << 12);
        return bcd;
    }
}

// end of Linker.java
