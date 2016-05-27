package fuku.webbook;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.WebUtils;

import fuku.eb4j.SubBook;
import fuku.eb4j.EBException;
import fuku.eb4j.util.ImageUtil;

import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_CONFIG;
import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_BEAN;

/**
 * リソースデータサーブレットクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ResourceServlet extends HttpServlet {

    /** PNG */
    private static final int PNG = 0;
    /** JPEG */
    private static final int JPEG = 1;
    /** WAVE */
    private static final int WAVE = 2;
    /** MIDI */
    private static final int MIDI = 3;
    /** MPEG */
    private static final int MPEG = 4;

    /** メディアタイプ */
    private static final String[] MEDIA_TYPE = {
        ".png", ".jpeg", ".wav", ".mid", ".mpeg"
    };

    /** MIMEタイプ */
    private static final String[] MIME_TYPE = {
        "image/png", "image/jpeg", "audio/x-wav", "audio/midi", "video/mpeg"
    };

    /** ログ */
    private transient Logger _logger = null;


    /**
     * コンストラクタ。
     *
     */
    public ResourceServlet() {
        super();
        _logger = LoggerFactory.getLogger(getClass());
    }


    /**
     * GETリクエストの処理。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @exception ServletException GETに相当するリクエストが処理できない場合
     * @exception IOException GETリクエストの処理中に入出力エラーが発生した場合
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        doPost(req, res);
    }

    /**
     * POSTリクエストの処理。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @exception ServletException POSTに相当するリクエストが処理できない場合
     * @exception IOException POSTリクエストの処理中に入出力エラーが発生した場合
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        String path = req.getPathInfo();
        if (StringUtils.isBlank(path)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // メディアタイプの設定
        int type = 0;
        int len = MEDIA_TYPE.length;
        for (; type<len; type++) {
            if (path.endsWith(MEDIA_TYPE[type])) {
                break;
            }
        }
        if (type >= len) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        res.setContentType(MIME_TYPE[type]);
        OutputStream out = res.getOutputStream();

        // 要求対象のファイル
        File reqFile = new File(path);
        String name = reqFile.getName();

        WebBookConfig config =
            (WebBookConfig)getServletContext().getAttribute(KEY_WEBBOOK_CONFIG);
        WebBookBean webbook =
            (WebBookBean)WebUtils.getSessionAttribute(req, KEY_WEBBOOK_BEAN);

        String idstr = reqFile.getParentFile().getName();
        int id = -1;
        try {
            id = Integer.parseInt(idstr);
        } catch (NumberFormatException e) {
        }
        BookEntry entry = webbook.getBookEntry(id);
        if (entry == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        SubBook subbook = entry.getSubBook();

        boolean cacheFlag = false;
        switch (type) {
            case JPEG: {
                cacheFlag = config.isImageCache();
                break;
            }
            case WAVE:
            case MIDI: {
                cacheFlag = config.isSoundCache();
                break;
            }
            default: {
                int prefix = name.charAt(0);
                if (prefix == 'N' || prefix == 'W') {
                    cacheFlag = config.isGaijiCache();
                } else if (prefix == 'M' || prefix == 'C') {
                    cacheFlag = config.isImageCache();
                }
                break;
            }
        }
        File cacheFile = null;
        if (cacheFlag) {
            // キャッシュがあれば出力
            cacheFile = new File(config.getCacheDirectory(), path);
            if (cacheFile.canRead()) {
                _send(out, cacheFile);
                return;
            }
        }

        // データの読み込み
        byte[] resData = null;
        File resFile = null;
        try {
            switch (type) {
                case PNG: {
                    int prefix = name.charAt(0);
                    if (prefix == 'N' || prefix == 'W') {
                        // 外字
                        boolean narrow = false;
                        if (prefix == 'N') {
                            narrow = true;
                        }
                        int idx1 = name.indexOf('-');
                        int idx2 = name.indexOf("_F-", idx1+1);
                        int idx3 = name.indexOf("_B-", idx2+3);
                        int code = Integer.parseInt(name.substring(idx1+1, idx2), 16);
                        int fore = Integer.parseInt(name.substring(idx2+3, idx3), 16);
                        int back = Integer.parseInt(name.substring(idx3+3, name.length()-4), 16);
                        resData = _getFontImage(subbook, narrow, code, fore, back);
                    } else if (prefix == 'M') {
                        // モノクロ画像
                        int idx1 = name.indexOf('-');
                        int idx2 = name.indexOf("_W-", idx1+1);
                        int idx3 = name.indexOf("_H-", idx2+3);
                        long pos = Long.parseLong(name.substring(idx1+1, idx2), 16);
                        int width = Integer.parseInt(name.substring(idx2+3, idx3), 16);
                        int height = Integer.parseInt(name.substring(idx3+3, name.length()-4), 16);
                        resData = _getMonoImage(subbook, pos, width, height);
                    } else if (prefix == 'C') {
                        // カラー画像
                        long pos = Long.parseLong(name.substring(2, name.length()-4), 16);
                        resData = _getColorImage(subbook, pos, type);
                    }
                    break;
                }
                case JPEG: {
                    long pos = Long.parseLong(name.substring(2, name.length()-5), 16);
                    resData = _getColorImage(subbook, pos, type);
                    break;
                }
                case WAVE:
                case MIDI: {
                    int idx1 = name.indexOf('-');
                    int idx2 = name.indexOf("_E-", idx1+1);
                    long pos1 = Long.parseLong(name.substring(idx1+1, idx2), 16);
                    long pos2 = Long.parseLong(name.substring(idx2+3, name.length()-4), 16);
                    resData = _getSound(subbook, type, pos1, pos2);
                    break;
                }
                case MPEG: {
                    resFile = subbook.getMovieFile(name.substring(0, name.length()-5));
                    break;
                }
                default:
                    break;
            }
        } catch (RuntimeException e) {
            resData = null;
            resFile = null;
        }

        // 応答の出力
        if (resFile != null) {
            _send(out, resFile);
        } else if (!ArrayUtils.isEmpty(resData)) {
            // キャッシュの保存
            _store(cacheFile, resData);
            out.write(resData, 0, resData.length);
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 外字イメージを返します。
     *
     * @param subbook 副本
     * @param narrow 半角/全角フラグ
     * @param code 外字のコード
     * @param fore 前景色
     * @param back 背景色
     * @return PNGデータ
     */
    private byte[] _getFontImage(SubBook subbook, boolean narrow,
                                 int code, int fore, int back) {
        byte[] data = null;
        int width = 0;
        try {
            if (narrow) {
                if (!subbook.getFont().hasNarrowFont()) {
                    return new byte[0];
                }
                data = subbook.getFont().getNarrowFont(code);
                width = subbook.getFont().getNarrowFontWidth();
            } else {
                if (!subbook.getFont().hasWideFont()) {
                    return new byte[0];
                }
                data = subbook.getFont().getWideFont(code);
                width = subbook.getFont().getWideFontWidth();
            }
        } catch (EBException e) {
            _logger.warn("failed to load font image", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        int height = subbook.getFont().getFontHeight();
        return ImageUtil.bitmapToPNG(data, width, height,
                                     new Color(fore), new Color(back), true, 9);
    }

    /**
     * モノクロ画像を返します。
     *
     * @param subbook 副本
     * @param pos 画像の位置
     * @param width 画像の幅
     * @param height 画像の高さ
     * @return PNGデータ
     */
    private byte[] _getMonoImage(SubBook subbook, long pos, int width, int height) {
        byte[] data = null;
        try {
            data = subbook.getGraphicData().getMonoGraphic(pos, width, height);
        } catch (EBException e) {
            _logger.warn("failed to load mono image", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        return ImageUtil.bitmapToPNG(data, width, height,
                                     Color.BLACK, Color.WHITE, false, 9);
    }

    /**
     * カラー画像を返します。
     *
     * @param subbook 副本
     * @param pos 画像の位置
     * @param type メディアタイプ
     * @return イメージデータ
     */
    private byte[] _getColorImage(SubBook subbook, long pos, int type) {
        byte[] data = null;
        try {
            data = subbook.getGraphicData().getColorGraphic(pos);
        } catch (EBException e) {
            _logger.warn("failed to load color image", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        if (type == PNG) {
            return ImageUtil.dibToPNG(data, 9);
        }
        return data;
    }

    /**
     * 音声を返します。
     *
     * @param subbook 副本
     * @param start 開始位置
     * @param end 終了位置
     * @return 音声データ
     */
    private byte[] _getSound(SubBook subbook, int type, long start, long end) {
        byte[] data = null;
        try {
            switch (type) {
                case WAVE:
                    data = subbook.getSoundData().getWaveSound(start, end);
                    break;
                case MIDI:
                    data = subbook.getSoundData().getMidiSound(start, end);
                    break;
                default:
                    break;
            }
        } catch (EBException e) {
            _logger.warn("failed to load sound data", e);
            return new byte[0];
        }
        if (ArrayUtils.isEmpty(data)) {
            return new byte[0];
        }
        return data;
    }

    /**
     * データをファイルに保存します。
     *
     * @param file ファイル
     * @param data データ
     */
    private void _store(File file, byte[] data) {
        if (file == null || ArrayUtils.isEmpty(data)) {
            return;
        }
        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            _logger.error("failed to create directories: " + dir.getAbsolutePath());
            return;
        }
        FileChannel channel = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            channel = fos.getChannel();
            channel.write(ByteBuffer.wrap(data));
        } catch (IOException e) {
            _logger.warn("failed to store file", e);
        } finally {
            IOUtils.closeQuietly(fos);
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * ファイルの内容を出力します。
     *
     * @param out 出力ストリーム
     * @param file 出力ファイル
     */
    private void _send(OutputStream out, File file) {
        BufferedInputStream bis = null;
        byte[] b = new byte[8192];
        int n = 0;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            while ((n=bis.read(b, 0, b.length)) >= 0) {
                out.write(b, 0, n);
            }
        } catch (IOException e) {
            _logger.warn("failed to send data", e);
        } finally {
            IOUtils.closeQuietly(bis);
        }
    }
}

// end of ResourceServlet.java
