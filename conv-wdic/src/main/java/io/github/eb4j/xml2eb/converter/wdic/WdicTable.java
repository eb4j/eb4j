package io.github.eb4j.xml2eb.converter.wdic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.xml2eb.util.WaitImageObserver;

/**
 * テーブルクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicTable {

    /** ログ */
    private Logger _logger = null;
    /** 辞書項目 */
    private WdicItem _item = null;
    /** 辞書ID */
    private String _id = null;
    /** テーブルデータリスト */
    List<String> _list = new ArrayList<String>();

    /** テーブルイメージ */
    private BufferedImage _img = null;


    /**
     * コンストラクタ。
     *
     * @param item 辞書項目
     */
    public WdicTable(WdicItem item) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _item = item;
        String grpId = _item.getWdic().getGroupId();
        String partId = _item.getWdic().getPartId();
        _id = grpId + ":" + partId + ":" + _item.getHead();
    }


    /**
     * テーブルデータを追加します。
     *
     * @param line テーブルデータ
     */
    public void add(String line) {
        _list.add(line);
    }


    /**
     * テーブルイメージを返します。
     *
     * @return イメージ
     */
    public BufferedImage getImage() {
        if (_img == null) {
            _makeImage();
        }
        return _img;
    }

    /**
     * 表のイメージを作成します。
     *
     */
    private void _makeImage() {
        List<WdicTableRow> rowList = _parse();
        int size = rowList.size();

        // カラム数の確認
        int cols = -1;
        for (int i=0; i<size; i++) {
            WdicTableRow row = rowList.get(i);
            int n = row.size();
            if (cols == -1) {
                cols = n;
            } else if (n != cols) {
                _logger.warn("different column size: " + _id + " [" + cols + "," + n + "]");
                if (n < cols) {
                    cols = n;
                }
            }
        }
        if (cols < 1) {
            _logger.warn("column data unavailable: " + _id);
            return;
        }

        // 各要素のパッディング
        int hpad = 4;
        int vpad = 2;

        // 表示幅と高さ
        int[] width = new int[cols];
        int[] height = new int[size];
        // まず単要素の最大幅と最大高さを判定
        for (int i=0; i<size; i++) {
            WdicTableRow row = rowList.get(i);
            for (int j=0; j<cols; j++) {
                WdicTableItem item = row.get(j);
                if (item.isHBonding() || item.isVBonding()) {
                    continue;
                }
                if (j+1 >= cols || !row.get(j+1).isHBonding()) {
                    // 単要素のみ
                    int w = item.getWidth();
                    if (w > width[j]) {
                        width[j] = w;
                    }
                }
                if (i+1 >= size || !rowList.get(i+1).get(j).isVBonding()) {
                    // 単要素のみ
                    int h = item.getHeight();
                    if (h > height[i]) {
                        height[i] = h;
                    }
                }
            }
        }
        // 横結合要素について幅と高さを判定
        for (int i=0; i<size; i++) {
            WdicTableRow row = rowList.get(i);
            for (int j=0; j<cols; j++) {
                WdicTableItem item = row.get(j);
                if (item.isHBonding() || item.isVBonding()) {
                    continue;
                }
                if (j+1 < cols && row.get(j+1).isHBonding()) {
                    int w = item.getWidth();
                    int cnt = 2;
                    int tw = width[j] + width[j+1] + hpad * 2 + 1;
                    for (int k=j+2; k<cols; k++) {
                        if (!row.get(k).isHBonding()) {
                            break;
                        }
                        cnt++;
                        tw += width[k] + hpad * 2 + 1;
                    }
                    if (w > tw) {
                        // 結合要素の幅のほうが大い場合は差を均等に配分
                        int dw = (w - tw) / cnt;
                        for (int k=0; k<cnt; k++) {
                            width[j+k] += dw;
                        }
                    }
                }
                if (i+1 < size && rowList.get(i+1).get(j).isVBonding()) {
                    int h = item.getHeight();
                    int cnt = 2;
                    int th = height[i] + height[i+1] + vpad * 2 + 1;
                    for (int k=i+2; k<size; k++) {
                        if (!rowList.get(k).get(j).isVBonding()) {
                            break;
                        }
                        cnt++;
                        th += height[k] + vpad * 2 + 1;
                    }
                    if (h > th) {
                        // 結合要素の高さのほうが大い場合は差を均等に配分
                        int dh = (h - th) / cnt;
                        for (int k=0; k<cnt; k++) {
                            height[i+k] += dh;
                        }
                    }
                }
            }
        }

        int imageWidth = (cols + 1) * (1 + hpad * 2);
        for (int i=0; i<cols; i++) {
            imageWidth += width[i];
        }
        int imageHeight = (size + 1) * (1 + vpad * 2);
        for (int i=0; i<size; i++) {
            imageHeight += height[i];
        }
        _img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g2 = _img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, imageWidth, imageHeight);

        g2.setColor(Color.BLACK);
        int y = 1 + vpad * 2;
        for (int i=0; i<size; i++) {
            WdicTableRow row = rowList.get(i);
            int x = 1 + hpad * 2;
            int h = height[i];
            for (int j=0; j<cols; j++) {
                WdicTableItem item = row.get(j);
                int w = width[j];
                if (!item.isHBonding() && !item.isVBonding()) {
                    // 横結合要素の幅を算出
                    int tw = w;
                    for (int k=j+1; k<cols; k++) {
                        if (!row.get(k).isHBonding()) {
                            break;
                        }
                        tw += width[k] + hpad * 2 + 1;
                    }
                    // 横結合要素の高さを算出
                    int th = h;
                    for (int k=i+1; k<size; k++) {
                        if (!rowList.get(k).get(j).isVBonding()) {
                            break;
                        }
                        th += height[k] + vpad * 2 + 1;
                    }

                    BufferedImage[] imgs = item.getImage();
                    int n = imgs.length;
                    int dy = (th - item.getHeight()) / 2;
                    for (int k=0; k<n; k++) {
                        int dx = 0;
                        switch (item.getAlign()) {
                            case WdicTableItem.CENTER:
                                dx = (tw - imgs[k].getWidth()) / 2;
                                break;
                            case WdicTableItem.RIGHT:
                                dx = tw - imgs[k].getWidth();
                                break;
                            case WdicTableItem.LEFT:
                            default: {
                                break;
                            }
                        }
                        WaitImageObserver obs = new WaitImageObserver();
                        if (!g2.drawImage(imgs[k], x+dx, y+dy, obs)) {
                            obs.waitFor();
                        }
                        dy += imgs[k].getHeight();
                    }
                }
                if (!item.isHBonding()) {
                    // 要素区切りの縦線
                    g2.drawLine(x-hpad-1, y-vpad-1, x-hpad-1, y+h+vpad);
                }
                if (!item.isVBonding()) {
                    // 行区切りの横線
                    g2.drawLine(x-hpad-1, y-vpad-1, x+w+hpad, y-vpad-1);
                }
                x += w + 1 + hpad * 2;
            }
            y += h + 1 + vpad * 2;
        }
        // 外枠
        g2.drawRect(hpad, vpad, imageWidth-(hpad*2), imageHeight-(vpad*2));
        g2.dispose();

        // リソースの破棄
        for (int i=0; i<size; i++) {
            WdicTableRow row = rowList.get(i);
            for (int j=0; j<cols; j++) {
                row.get(j).destroy();
            }
        }
    }

    /**
     * 表データをパースします。
     *
     * @return 行データのリスト
     */
    private List<WdicTableRow> _parse() {
        List<WdicTableRow> rowList = null;
        if (!_list.isEmpty()) {
            try {
                String str = _list.get(0);
                if (str.startsWith("| ")) {
                    rowList = _parseFullSpec();
                } else {
                    rowList = _parseSimple();
                }
            } catch (Exception e) {
                String sep = System.getProperty("line.separator", "\n");
                StringBuilder buf = new StringBuilder();
                int len = _list.size();
                for (int i=0; i<len; i++) {
                    buf.append(_list.get(i));
                    buf.append(sep);
                }
                _logger.warn("unexpected table format: " + _id + sep + buf.toString(), e);
            }
        }
        if (rowList == null) {
            rowList = new ArrayList<WdicTableRow>();
        }
        return rowList;
    }

    /**
     * 完全形式のテーブルデータをパースします。
     *
     * @return 行データのリスト
     */
    private List<WdicTableRow> _parseFullSpec() {
        List<WdicTableRow> rowList = new ArrayList<WdicTableRow>();
        int len = _list.size();
        int col = -1;
        int hnum = -1;
        int vnum = -1;
        WdicTableRow row = null;
        for (int i=1; i<len; i++) {
            String line = _list.get(i).trim();
            if ("|>".equals(line)) {
                row = new WdicTableRow();
                rowList.add(row);
            } else {
                if (vnum > 0 && row.size() == col) {
                    vnum--;
                    for (int j=0; j<hnum; j++) {
                        WdicTableItem item = new WdicTableItem(_item, false, null);
                        item.setHBonding(true);
                        if (vnum > 0) {
                            item.setVBonding(true);
                        }
                        row.add(item);
                    }
                }
                int idx = WdicUtil.indexOf(line, "|", 1);
                if (idx == 2) {
                    row.add(new WdicTableItem(_item, false, line.substring(2)));
                } else {
                    String str = line.substring(1, idx);
                    row.add(new WdicTableItem(_item, false, line.substring(idx+1)));
                    idx = WdicUtil.indexOf(str, ".", 1);
                    hnum = Integer.parseInt(str.substring(0, idx)) - 1;
                    vnum = Integer.parseInt(str.substring(idx+1)) - 1;
                    for (int j=0; j<hnum; j++) {
                        WdicTableItem item = new WdicTableItem(_item, false, null);
                        item.setHBonding(true);
                        if (vnum > 0) {
                            item.setVBonding(true);
                        }
                        row.add(item);
                    }
                    col = row.size();
                }
            }
        }
        return rowList;
    }

    /**
     * 簡易形式のテーブルデータをパースします。
     *
     * @return 行データのリスト
     */
    private List<WdicTableRow> _parseSimple() {
        List<WdicTableRow> rowList = new ArrayList<WdicTableRow>();
        int len = _list.size();
        for (int i=0; i<len; i++) {
            WdicTableRow row = new WdicTableRow();
            rowList.add(row);

            String line = _list.get(i).trim();
            boolean header = false;
            if (line.startsWith("|= ")) {
                header = true;
            }

            int idx1 = 3;
            int idx2 = WdicUtil.indexOf(line, "|", idx1);
            while (idx2 != -1) {
                String str = line.substring(idx1, idx2);
                row.add(new WdicTableItem(_item, header, str));
                idx1 = idx2 + 1;
                idx2 = WdicUtil.indexOf(line, "|", idx1);
            }
            String str = line.substring(idx1);
            row.add(new WdicTableItem(_item, header, str));

            int n = rowList.size();
            if (n < 2) {
                continue;
            }
            WdicTableRow prevRow = rowList.get(n-2);
            int n1 = prevRow.size();
            int n2 = row.size();
            if (n1 != n2) {
                for (int j=0; j<n2; j++) {
                    WdicTableItem item = row.get(j);
                    if (item.isVBonding()) {
                        // 縦結合の場合
                        // 前行が横結合されている要素数
                        int cnt = 0;
                        for (int k=j+1; k<n1; k++) {
                            if (!prevRow.get(k).isHBonding()) {
                                break;
                            }
                            cnt++;
                        }
                        // 前行が横結合されているためその分追加
                        for (int k=0; k<cnt; k++) {
                            WdicTableItem padItem =
                                new WdicTableItem(_item, item.isHeader());
                            padItem.setVBonding(true);
                            padItem.setHBonding(true);
                            row.add(j+1, padItem);
                        }
                        n2 += cnt;
                        j += cnt;
                    }
                }
            }
            for (int j=1; j<n1; j++) {
                WdicTableItem item = row.get(j);
                if (item.isHBonding() && row.get(j-1).isVBonding()) {
                    // この要素が横結合であり前の要素が縦結合であれば縦結合
                    item.setVBonding(true);
                }
            }
            for (int j=0; j<n1; j++) {
                WdicTableItem item1 = prevRow.get(j);
                WdicTableItem item2 = row.get(j);
                if (item2.isVBonding() && item1.isHBonding()) {
                    // この要素が縦結合であり前行の要素が横結合であれば横結合
                    item2.setHBonding(true);
                }
            }
        }
        return rowList;
    }
}

// end of WdicTable.java
