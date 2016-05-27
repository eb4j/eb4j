package io.github.eb4j.xml2eb.util;

import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * イメージロード待機イメージオブザーバ。
 *
 * @author Hisaya FUKUMOTO
 */
public class WaitImageObserver implements ImageObserver {

    /** 完了フラグ */
    private volatile boolean _complated = false;
    /** 待機用オブジェクト */
    private Object _lock = new Object();


    /**
     * コンストラクタ。
     *
     */
    public WaitImageObserver() {
        super();
    }


    /**
     * イメージのロードが完了したかどうかを返します。
     *
     * @return ロードが完了した場合はtrue、そうでない場合はfalse
     */
    public boolean isComplated() {
        return _complated;
    }

    /**
     * イメージのロードが完了するまで待機します。
     *
     */
    public void waitFor() {
        synchronized (_lock) {
            while (!_complated) {
                try {
                    _lock.wait(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * 非同期インタフェースのコールバックメソッド。
     *
     * @param img イメージ
     * @param infoflags イメージに関する情報
     * @param x x座標
     * @param y y座標
     * @param w 幅
     * @param h 高さ
     * @return イメージ全体がロードされた場合はfalse、そうでない場合はtrue
     */
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        synchronized (_lock) {
            if ((infoflags & ALLBITS) != 0) {
                _complated = true;
                return false;
            } else if ((infoflags & ABORT) != 0) {
                _complated = true;
                return false;
            } else if ((infoflags & ERROR) != 0) {
                _complated = true;
                return false;
            }
            _lock.notifyAll();
        }
        return true;
    }
}

// end of WaitImageObserver.java
