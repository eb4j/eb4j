package io.github.eb4j.xml2eb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * インデックス出力クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class IndexWriter {

    /** ログ */
    private Logger _logger = null;

    /** ベースファイル */
    private File _file = null;
    /** インデックス階層リスト */
    private List<IndexLayer> _layerList = null;
    /** 出力ストリームリスト */
    private List<OutputStream> _streamList = null;
    /** 現在の階層の深さ */
    private int _depth = 0;
    /** 参照情報 */
    private Reference _ref = null;


    /**
     * コンストラクタ。
     *
     * @param file ベースファイル
     */
    public IndexWriter(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
        _layerList = new ArrayList<IndexLayer>();
        _streamList = new ArrayList<OutputStream>();
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
     * すべての出力ストリームを閉じます。
     *
     */
    public void close() {
        if (_layerList.isEmpty()) {
            return;
        }
        try {
            IndexLayer layer = _layerList.get(0);
            Index lastIndex = layer.getLastIndex();
            for (int i=1; i<_depth; i++) {
                layer = _layerList.get(i-1);
                Index upperIndex = new Index(lastIndex.getWord(), layer.getBlock());
                _addUpperLayer(i, upperIndex);
            }
            _writeLeafLayer(true);
            for (int i=1; i<_depth; i++) {
                _writeUpperLayer(i, true);
            }
        } catch (IOException e) {
        }
        for (int i=0; i<_depth; i++) {
            OutputStream out = _streamList.get(i);
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 検索語リストの内容を書き込みます。
     *
     * @param set 検索語セット
     * @exception IOException 入出力エラーが発生した場合
     */
    public void write(WordSet set) throws IOException {
        if (set.isEmpty()) {
            return;
        }

        IndexLayer layer = new IndexLayer(_file, _depth++);
        OutputStream stream =
            new BufferedOutputStream(new FileOutputStream(layer.getFile()));
        _layerList.add(layer);
        _streamList.add(stream);

        Iterator<Word> it = set.iterator();
        while (it.hasNext()) {
            Word word = it.next();
            int n = layer.getBlockLength() + word.getWordLength() + 13;
            if (n > 2048) {
                _writeLeafLayer();
                if (_depth == 1) {
                    IndexLayer upperLayer = new IndexLayer(_file, _depth++);
                    OutputStream upperStream =
                        new BufferedOutputStream(new FileOutputStream(upperLayer.getFile()));
                    _layerList.add(upperLayer);
                    _streamList.add(upperStream);
                }
                Index upperIndex = layer.getLastIndex();
                _addUpperLayer(1, upperIndex);
                layer.clear();
            }
            Index index = new Index(word, layer.getBlock());
            layer.add(index);
        }
    }

    /**
     * 上位インデックスに追加します。
     *
     * @param level インデックス階層レベル
     * @param index インデックス
     * @exception IOException 入出力エラーが発生した場合
     */
    private void _addUpperLayer(int level, Index index) throws IOException {
        _logger.trace("level=" + level + ", word=" + index.getWord());
        IndexLayer layer = _layerList.get(level);
        int wordLen = index.getWord().getWordLength();
        int curLen = layer.getWordLength();
        int n = 0;
        if (curLen < wordLen) {
            n = (layer.size() + 2) * (wordLen + 4) + 4;
        } else {
            n = (layer.size() + 2) * (curLen + 4) + 4;
        }
        if (n > 2048) {
            _writeUpperLayer(level);
            if (_depth == level + 1) {
                IndexLayer upperLayer = new IndexLayer(_file, _depth++);
                OutputStream upperStream =
                    new BufferedOutputStream(new FileOutputStream(upperLayer.getFile()));
                _layerList.add(upperLayer);
                _streamList.add(upperStream);
            }
            Index lastIndex = layer.getLastIndex();
            Index upperIndex = new Index(lastIndex.getWord(), layer.getBlock());
            _addUpperLayer(level+1, upperIndex);
            layer.clear();
            curLen = layer.getWordLength();
        }

        layer.add(index);
        if (curLen < wordLen) {
            layer.setWordLength(wordLen);
        }
    }

    /**
     * 下位インデックスを書き込みます。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    private void _writeLeafLayer() throws IOException {
        _writeLeafLayer(false);
    }

    /**
     * 下位インデックスを書き込みます。
     *
     * @param last 最後のインデックスの場合はtrue、そうでない場合はfalse
     * @exception IOException 入出力エラーが発生した場合
     */
    private void _writeLeafLayer(boolean last) throws IOException {
        IndexLayer layer = _layerList.get(0);
        if (layer.isEmpty()) {
            return;
        }

        int id = 0x80;
        if (layer.getBlock() == 1) {
            id |= 0x40;
        }
        if (last) {
            id |= 0x20;
        }
        byte[] buf = new byte[2048];
        Arrays.fill(buf, (byte)0x00);
        buf[0] = (byte)id;

        int size = layer.size();
        buf[2] = (byte)((size >>> 8) & 0xff);
        buf[3] = (byte)(size & 0xff);

        long pos = (layer.getBlock() - 1) * 2048;
        int off = 4;
        for (int i=0; i<size; i++) {
            Index index = layer.get(i);
            Word word = index.getWord();
            byte[] b = word.getWord();
            int len = b.length;
            buf[off++] = (byte)(len & 0xff);
            System.arraycopy(b, 0, buf, off, len);
            off += len;
            String name = word.getReferenceTag();
            _ref.putBodyRef(layer.getFile(), pos+off, name);
            off += 6;
            _ref.putHeadRef(layer.getFile(), pos+off, name);
            off += 6;
        }
        OutputStream out = _streamList.get(0);
        out.write(buf, 0, buf.length);
        out.flush();
    }

    /**
     * 上位インデックスを書き込みます。
     *
     * @param level 階層レベル
     * @exception IOException 入出力エラーが発生した場合
     */
    private void _writeUpperLayer(int level) throws IOException {
        _writeUpperLayer(level, false);
    }

    /**
     * 上位インデックスを書き込みます。
     *
     * @param level 階層レベル
     * @param last 最後のインデックスの場合はtrue、そうでない場合はfalse
     * @exception IOException 入出力エラーが発生した場合
     */
    private void _writeUpperLayer(int level, boolean last) throws IOException {
        IndexLayer layer = _layerList.get(level);
        if (layer.isEmpty()) {
            return;
        }

        int len = layer.getWordLength();
        if (last) {
            // ダミーを追加
            byte[] b = new byte[len];
            Arrays.fill(b, (byte)0xff);
            Index lastIndex = layer.getLastIndex();
            Word dummyWord = new Word(b, "");
            long lastBlock = lastIndex.getLowerBlock();
            Index dummy = new Index(dummyWord, lastBlock);
            layer.add(dummy);
        }

        int id = 0x00;
        if (layer.getBlock() == 1) {
            id |= 0x40;
        }
        if (last) {
            id |= 0x20;
        }
        byte[] buf = new byte[2048];
        Arrays.fill(buf, (byte)0x00);
        buf[0] = (byte)id;
        buf[1] = (byte)(len & 0xff);

        int size = layer.size();
        buf[2] = (byte)((size >>> 8) & 0xff);
        buf[3] = (byte)(size & 0xff);

        long pos = (layer.getBlock() - 1) * 2048;
        int off = 4;
        for (int i=0; i<size; i++) {
            Index index = layer.get(i);
            Word word = index.getWord();
            byte[] b = word.getWord();
            System.arraycopy(b, 0, buf, off, b.length);
            off += len;
            String name = Long.toString(index.getLowerBlock());
            _ref.putIndexRef(layer.getFile(), pos+off, name);
            off += 4;
        }
        OutputStream out = _streamList.get(level);
        out.write(buf, 0, buf.length);
        out.flush();

        if (last) {
            // ダミーを削除
            layer.remove(size-1);
        }
    }
}

// end of IndexWriter.java
