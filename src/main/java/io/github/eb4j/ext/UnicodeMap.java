package io.github.eb4j.ext;

import io.github.eb4j.EBException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;


/**
 * UnicodeMap class to search alternative unicode for extfont codepoint.
 */
public class UnicodeMap {

    private HashMap<Integer, String> unicodeMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param title target dictionary title
     * @param dir target dictionary directory
     * @throws EBException when unicode map does not exist.
     */
    public UnicodeMap(final String title, final File dir) throws EBException {
        boolean loaded = false;
        if (dir != null) {
            if (!dir.isDirectory()) {
                throw new EBException(EBException.DIR_NOT_FOUND, dir.getPath());
            }
            if (!dir.canRead()) {
                throw new EBException(EBException.CANT_READ_DIR, dir.getPath());
            }
            File [] listFile = dir.listFiles();
            if (listFile != null) {
                try {
                    for (File file : listFile) {
                        if (file.isFile()) {
                            if (file.getName().toLowerCase().endsWith(".map")) {
                                loadMap(file);
                                loaded = true;
                                break;
                            }
                        }
                    }
                } catch (EBException ignored) {
                }
            }
        }
        if (!loaded) {
            if (isUnno(title)) {
                setUnnoMap();
            } else {
                throw new EBException(EBException.CANT_FIND_UNICODEMAP);
            }
        }
    }

    public String get(final int extCode) {
        return unicodeMap.get(extCode);
    }

    private void loadMap(final File file) throws EBException {
        StringTokenizer st;
        try {
            BufferedReader tsvFile = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                    Charset.forName("SHIFT-JIS")));
            String dataRow = tsvFile.readLine();
            while (dataRow != null) {
                List<String> dataArray = new ArrayList<>() ;
                st = new StringTokenizer(dataRow, "\t");
                while (st.hasMoreElements()) {
                    dataArray.add(st.nextElement().toString());
                }
                if (dataArray.size() >= 2) {
                    String key = dataArray.get(0);
                    if (!key.startsWith("#")) {
                        String val = dataArray.get(1);
                        if ((key.startsWith("h") || key.startsWith("z"))) {
                            Integer keyNum = Integer.parseInt(key.substring(1, 5), 16);
                            if (val.startsWith("u")) {
                                if (val.contains(",")) {
                                    StringBuilder sb = new StringBuilder();
                                    for (String item : val.split(",")) {
                                        if (item.startsWith("u")) {
                                            sb.append(Character.toChars(Integer.valueOf(item.substring(1, 5), 16)));
                                        }
                                    }
                                    unicodeMap.put(keyNum, sb.toString());
                                } else {
                                    String uniValue = new String(Character.toChars(Integer.valueOf(val.substring(1, 5), 16)));
                                    unicodeMap.put(keyNum, uniValue);
                                }
                            } else if (val.startsWith("-") && dataArray.size() == 3) {
                                String uniValue = dataArray.get(2);
                                unicodeMap.put(keyNum, uniValue);
                            }
                        }
                    }
                }
                dataRow = tsvFile.readLine();
            }
        } catch (IOException e) {
            throw new EBException(EBException.CANT_FIND_UNICODEMAP);
        }
    }

    private boolean isUnno(final String target) {
        List<String> signature = new ArrayList<>();
        signature.add("６ビ技実用英語　英和・和英６０２");
        signature.add("６用例ファイル（ビ技実用英語）６０２");
        signature.add("ビジネス技術実用英語大辞典Ｖ５　英和編＆和英編");
        signature.add("用例ファイル（ビ技実用英語Ｖ５）");

        for (String sig: signature) {
            if (target.startsWith(sig)) {
                return true;
            }
        }
        return false;
    }

    private void setUnnoMap() {
        unicodeMap.put(0xA221, "\u00b0");
        unicodeMap.put(0xA222, "\\");
        unicodeMap.put(0xA223, "\u00e9");
        unicodeMap.put(0xA224, "\u00e0");
        unicodeMap.put(0xA225, "\u00e8");
        unicodeMap.put(0xA226, "\u00ea");
        unicodeMap.put(0xA227, "\u00e4");
        unicodeMap.put(0xA228, "\u00eb");
        unicodeMap.put(0xA229, "\u00f6");
        unicodeMap.put(0xA22A, "\u00fc");
        unicodeMap.put(0xA22B, "\u00f1");
        unicodeMap.put(0xA22E, "\u00dc");
        unicodeMap.put(0xA122, "\uff5f");
        unicodeMap.put(0xA123, "\uff60");
        unicodeMap.put(0xA126, "\u25b6");
        unicodeMap.put(0xA127, "\u25cf");
        unicodeMap.put(0xA128, "\u2713");
        unicodeMap.put(0xA129, "\u00bd");
        unicodeMap.put(0xA12A, "\u00dc");
        unicodeMap.put(0xA12B, "\u00be");
    }
}
