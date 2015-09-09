package fuku.xml2eb;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.sound.sampled.AudioFormat;

/**
 * 参照情報クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Reference {

    /** 外字マップ */
    private Map<String,Integer> _charMap = null;
    /** タグマップ */
    private Map<String,Position> _tagMap = null;
    /** 種別マップ */
    private Map<String,String> _formatMap = null;
    /** 音声フォーマットマップ */
    private Map<String,AudioFormat> _audioFormatMap = null;
    /** 参照リスト */
    private Map<Position,String> _refMap = null;


    /**
     * コンストラクタ。
     *
     */
    public Reference() {
        super();
        _charMap = new HashMap<String,Integer>();
        _tagMap = new HashMap<String,Position>();
        _formatMap = new HashMap<String,String>();
        _audioFormatMap = new HashMap<String,AudioFormat>();
        _refMap = new HashMap<Position,String>();
    }

    /**
     * 半角外字を登録します。
     *
     * @param name 名称
     * @param code 外字コード
     */
    public void putNarrowChar(String name, int code) {
        String key = "narrow:" + name;
        _charMap.put(key, Integer.valueOf(code));
    }

    /**
     * 半角外字が登録されているかどうかを返します。
     *
     * @param name 名称
     * @return 登録されている場合はtrue、そうでない場合はfalse
     */
    public boolean hasNarrowChar(String name) {
        String key = "narrow:" + name;
        return _charMap.containsKey(key);
    }

    /**
     * 半角外字コードを返します。
     *
     * @param name 名称
     * @return 外字コード
     */
    public int getNarrowChar(String name) {
        String key = "narrow:" + name;
        Integer code = _charMap.get(key);
        if (code == null) {
            return -1;
        }
        return code.intValue();
    }

    /**
     * 全角外字を登録します。
     *
     * @param name 名称
     * @param code 外字コード
     */
    public void putWideChar(String name, int code) {
        String key = "wide:" + name;
        _charMap.put(key, Integer.valueOf(code));
    }

    /**
     * 全角外字が登録されているかどうかを返します。
     *
     * @param name 名称
     * @return 登録されている場合はtrue、そうでない場合はfalse
     */
    public boolean hasWideChar(String name) {
        String key = "wide:" + name;
        return _charMap.containsKey(key);
    }

    /**
     * 全角外字コードを返します。
     *
     * @param name 名称
     * @return 外字コード
     */
    public int getWideChar(String name) {
        String key = "wide:" + name;
        Integer code = _charMap.get(key);
        if (code == null) {
            return -1;
        }
        return code.intValue();
    }

    /**
     * 見出し位置を登録します。
     *
     * @param name タグ名称
     * @param file ファイル
     * @param pos 開始位置
     */
    public void putHeadTag(String name, File file, long pos) {
        String key = "head:" + name;
        _tagMap.put(key, new Position(file, pos));
    }

    /**
     * 見出し位置が登録されているかどうかを返します。
     *
     * @param name タグ名称
     * @return 登録されている場合はtrue、そうでない場合はfalse
     */
    public boolean hasHeadTag(String name) {
        String key = "head:" + name;
        return _tagMap.containsKey(key);
    }

    /**
     * 見出し位置を返します。
     *
     * @param name タグ名称
     * @return 開始位置
     */
    public Position getHeadTag(String name) {
        String key = "head:" + name;
        return _tagMap.get(key);
    }

    /**
     * 本文位置を登録します。
     *
     * @param name タグ名称
     * @param file ファイル
     * @param pos 開始位置
     */
    public void putBodyTag(String name, File file, long pos) {
        String key = "body:" + name;
        _tagMap.put(key, new Position(file, pos));
    }

    /**
     * 本文位置が登録されているかどうかを返します。
     *
     * @param name タグ名称
     * @return 登録されている場合はtrue、そうでない場合はfalse
     */
    public boolean hasBodyTag(String name) {
        String key = "body:" + name;
        return _tagMap.containsKey(key);
    }

    /**
     * 本文位置を返します。
     *
     * @param name タグ名称
     * @return 開始位置
     */
    public Position getBodyTag(String name) {
        String key = "body:" + name;
        return _tagMap.get(key);
    }

    /**
     * 画像データ位置を登録します。
     *
     * @param name タグ名称
     * @param format フォーマット
     * @param file ファイル
     * @param pos 開始位置
     */
    public void putGraphicTag(String name, String format, File file, long pos) {
        String key = "graphic:" + name;
        _tagMap.put(key, new Position(file, pos));
        _formatMap.put(key, format);
    }

    /**
     * 画像データ位置が登録されているかどうかを返します。
     *
     * @param name タグ名称
     * @return 登録されている場合はtrue、そうでない場合はfalse
     */
    public boolean hasGraphicTag(String name) {
        String key = "graphic:" + name;
        return _tagMap.containsKey(key);
    }

    /**
     * 画像データ位置を返します。
     *
     * @param name タグ名称
     * @return 開始位置
     */
    public Position getGraphicTag(String name) {
        String key = "graphic:" + name;
        return _tagMap.get(key);
    }

    /**
     * 画像フォーマットを返します。
     *
     * @param name タグ名称
     * @return 画像フォーマット
     */
    public String getGraphicFormat(String name) {
        String key = "graphic:" + name;
        return _formatMap.get(key);
    }

    /**
     * 音声データ位置を登録します。
     *
     * @param name タグ名称
     * @param format フォーマット
     * @param file ファイル
     * @param start 開始位置
     * @param end 終了位置
     */
    public void putSoundTag(String name, String format, File file, long start, long end) {
        String key = "sound-start:" + name;
        _tagMap.put(key, new Position(file, start));
        key = "sound-end:" + name;
        _tagMap.put(key, new Position(file, end));
        key = "sound:" + name;
        _formatMap.put(key, format);
    }

    /**
     * 音声データ位置が登録されているかどうかを返します。
     *
     * @param name タグ名称
     * @return 登録されている場合はtrue、そうでない場合はfalse
     */
    public boolean hasSoundTag(String name) {
        String key = "sound-start:" + name;
        return _tagMap.containsKey(key);
    }

    /**
     * 音声データ位置を返します。
     *
     * @param name タグ名称
     * @return 位置 {開始位置,終了位置}
     */
    public Position[] getSoundTag(String name) {
        Position[] pos = new Position[2];
        String key = "sound-start:" + name;
        pos[0] = _tagMap.get(key);
        key = "sound-end:" + name;
        pos[1] = _tagMap.get(key);
        return pos;
    }

    /**
     * 音声フォーマットを返します。
     *
     * @param name タグ名称
     * @return 音声フォーマット
     */
    public String getSoundFormat(String name) {
        String key = "sound:" + name;
        return _formatMap.get(key);
    }

    /**
     * 音声フォーマットを登録します。
     *
     * @param name タグ名称
     * @param audioFormat 音声フォーマット
     */
    public void putAudioFormat(String name, AudioFormat audioFormat) {
        _audioFormatMap.put(name, audioFormat);
    }

    /**
     * 音声フォーマットを登録します。
     *
     * @param name タグ名称
     * @return 音声フォーマット
     */
    public AudioFormat getAudioFormat(String name) {
        return _audioFormatMap.get(name);
    }

    /**
     * 見出し参照位置を登録します。
     *
     * @param file ファイル
     * @param pos 位置
     * @param name 参照タグ名称
     */
    public void putHeadRef(File file, long pos, String name) {
        String tag = "head:" + name;
        _refMap.put(new Position(file, pos), tag);
    }

    /**
     * 本文参照位置を登録します。
     *
     * @param file ファイル
     * @param pos 位置
     * @param name 参照タグ名称
     */
    public void putBodyRef(File file, long pos, String name) {
        String tag = "body:" + name;
        _refMap.put(new Position(file, pos), tag);
    }

    /**
     * インデックス参照位置を登録します。
     *
     * @param file ファイル
     * @param pos 参照位置
     * @param name 参照タグ名称
     */
    public void putIndexRef(File file, long pos, String name) {
        String tag = "index:" + name;
        _refMap.put(new Position(file, pos), tag);
    }

    /**
     * 画像参照位置を登録します。
     *
     * @param file ファイル
     * @param pos 参照位置
     * @param name 参照タグ名称
     */
    public void putGraphicRef(File file, long pos, String name) {
        String tag = "graphic:" + name;
        _refMap.put(new Position(file, pos), tag);
     }

    /**
     * 音声参照位置を登録します。
     *
     * @param file ファイル
     * @param pos 参照位置
     * @param name 参照タグ名称
     */
    public void putSoundRef(File file, long pos, String name) {
        String tag = "sound:" + name;
        _refMap.put(new Position(file, pos), tag);
    }

    /**
     * 見出し参照位置を返します。
     *
     * @return 見出し参照位置
     */
    public Map<Position,String> getHeadRef() {
        return _getRef("head:");
    }

    /**
     * 本文参照位置を返します。
     *
     * @return 本文参照位置
     */
    public Map<Position,String> getBodyRef() {
        return _getRef("body:");
    }

    /**
     * インデックス参照位置を返します。
     *
     * @return インデックス参照位置
     */
    public Map<Position,String> getIndexRef() {
        return _getRef("index:");
    }

    /**
     * 画像参照位置を返します。
     *
     * @return 画像参照位置
     */
    public Map<Position,String> getGraphicRef() {
        return _getRef("graphic:");
    }

    /**
     * 音声参照位置を返します。
     *
     * @return 音声参照位置
     */
    public Map<Position,String> getSoundRef() {
        return _getRef("sound:");
    }

    /**
     * 指定された接頭辞のタグ名称を持つ参照位置マップを返します。
     *
     * @param prefix タグ名称の接頭辞
     * @return 参照位置マップ
     */
    private Map<Position,String> _getRef(String prefix) {
        int len = prefix.length();
        Map<Position,String> map = new HashMap<Position,String>();
        Iterator<Map.Entry<Position,String>> it = _refMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Position,String> entry = it.next();
            String tag = entry.getValue();
            if (tag.startsWith(prefix)) {
                tag = tag.substring(len);
                map.put(entry.getKey(), tag);
            }
        }
        return map;
    }
}

// end of Reference.java
