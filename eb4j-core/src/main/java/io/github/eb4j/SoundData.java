package io.github.eb4j;

import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.BookInputStream;

import java.nio.charset.Charset;

/**
 * Sound data class.
 *
 * @author Hisaya FUKUMOTO
 */
public class SoundData {

    /** バイナリデータファイル */
    private EBFile _file = null;
    /** インデックススタイル */
    private IndexStyle _style = null;


    /**
     * Sound data object constructed from a sound data file and index style.
     *
     * @param file sound data file.
     * @param style index style.
     */
    protected SoundData(final EBFile file, final IndexStyle style) {
        super();
        _file = file;
        _style = style;
    }


    /**
     * Returns specified WAVE sound data.
     *
     * @param pos1 start position of a sound data.
     * @param pos2 end position of a sound data.
     * @return Byte array of WAVE sound data.
     * @exception EBException if file read error is happends.
     */
    public byte[] getWaveSound(final long pos1, final long pos2) throws EBException {
        long size;
        if (pos1 < pos2) {
            size = pos2 - pos1 + 1;
        } else {
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }

        /* WAVE Format
         *  "RIFF"
         *  size (4byte) = "WAVE" + header fragment + size of data part + data
         *  "WAVE"
         *  header fragment (28byte) = "fmt " + ... + "data"
         *  size of data part (4byte)
         */
        byte[] wave = null;
        BookInputStream bis = _file.getInputStream();
        try {
            bis.seek(pos1);
            byte[] b = new byte[4];
            bis.readFully(b, 0, b.length);
            if ("fmt ".equals(new String(b, 0, 4, Charset.forName("ASCII")))) {
                if (size >= 32) {
                    size -= 32;
                } else {
                    size = 0;
                }
                wave = new byte[(int)(44+size)];
                System.arraycopy(b, 0, wave, 12, 4);
                bis.readFully(wave, 16, 28);
            } else {
                wave = new byte[(int)(44+size)];
                bis.seek(_style.getStartPage(), 32);
                bis.readFully(wave, 12, 28);
                wave[40] = (byte)(size & 0xff);
                wave[41] = (byte)((size >>> 8) & 0xff);
                wave[42] = (byte)((size >>> 16) & 0xff);
                wave[43] = (byte)((size >>> 24) & 0xff);
                bis.seek(pos1);
            }
            bis.readFully(wave, 44, (int)size);
        } finally {
            bis.close();
        }

        wave[0] = 'R';
        wave[1] = 'I';
        wave[2] = 'F';
        wave[3] = 'F';
        wave[4] = (byte)((36+size) & 0xff);
        wave[5] = (byte)(((36+size) >>> 8) & 0xff);
        wave[6] = (byte)(((36+size) >>> 16) & 0xff);
        wave[7] = (byte)(((36+size) >>> 24) & 0xff);
        wave[8] = 'W';
        wave[9] = 'A';
        wave[10] = 'V';
        wave[11] = 'E';
        return wave;
    }

    /**
     * Returns specified MIDI sound data.
     *
     * @param pos1 データ開始位置
     * @param pos2 データ終了位置
     * @return MIDI音声データのバイト配列
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public byte[] getMidiSound(final long pos1, final long pos2) throws EBException {
        long size;
        if (pos1 < pos2) {
            size = pos2 - pos1;
        } else {
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }

        byte[] midi = new byte[(int)size];
        BookInputStream bis = _file.getInputStream();
        try {
            bis.seek(pos1);
            bis.readFully(midi, 0, midi.length);
        } finally {
            bis.close();
        }
        return midi;
    }
}

// end of SoundData.java
