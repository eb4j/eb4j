package io.github.eb4j.ext;

import io.github.eb4j.EBException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * UnicodeMap class to search alternative unicode for extfont codepoint.
 */
public class UnicodeMap {

    private HashMap<Integer, String> narrowMap = new HashMap<>();
    private HashMap<Integer, String> wideMap = new HashMap<>();

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
            File[] listFile = dir.listFiles();
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
            throw new EBException(EBException.CANT_FIND_UNICODEMAP);
        }
    }

    /**
     * Simple constructor.
     * @param mapFile map file.
     * @throws EBException when loading error happened.
     */
    public UnicodeMap(final File mapFile) throws EBException {
        if (!mapFile.isFile()) {
            throw new EBException(EBException.FILE_NOT_FOUND);
        }
        loadMap(mapFile);
    }

    /**
     * Simple map getter.
     * @return unmodifiable map.
     */
    public Map<Integer, String> getNarrowMap() {
        return Collections.unmodifiableMap(narrowMap);
    }

    /**
     * Simple map getter.
     * @return unmodifiable map.
     */
    public Map<Integer, String> getWideMap() {
        return Collections.unmodifiableMap(wideMap);
    }

    /**
     * Get unicode character code from narrow map.
     * @param extCode to query.
     * @return unicode string
     */
    public String getNarrow(final int extCode) {
        return narrowMap.get(extCode);
    }

    /**
     * Get unicode character code from wide map.
     * @param extCode to query.
     * @return unicode string
     */
    public String getWide(final int extCode) {
        return wideMap.get(extCode);
    }

    /**
     * Get Unicode character code from map.
     * @param extCode to query
     * @return unicode string
     */
    public String get(final int extCode) {
       if (narrowMap.containsKey(extCode)) {
           return narrowMap.get(extCode);
       } else {
           return wideMap.get(extCode);
       }
    }

    private void loadMap(final File file) throws EBException {
        StringTokenizer st;
        try {
            BufferedReader tsvFile = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                    Charset.forName("SHIFT-JIS")));
            String dataRow = tsvFile.readLine();
            while (dataRow != null) {
                List<String> dataArray = new ArrayList<>();
                st = new StringTokenizer(dataRow, "\t");
                while (st.hasMoreElements()) {
                    dataArray.add(st.nextElement().toString());
                }
                if (dataArray.size() >= 2) {
                    String key = dataArray.get(0);
                    if (!key.startsWith("#")) {
                        String val = dataArray.get(1);
                        String alt = null;
                        if (dataArray.size() == 3) {
                            alt = dataArray.get(2);
                        }
                        if (key.startsWith("h")) {
                            Integer keyNum = Integer.parseInt(key.substring(1, 5), 16);
                            addEntry(keyNum, val, alt, false);
                        } else if (key.startsWith("z")) {
                            Integer keyNum = Integer.parseInt(key.substring(1, 5), 16);
                            addEntry(keyNum, val, alt, true);
                        }
                    }
                }
                dataRow = tsvFile.readLine();
            }
        } catch (IOException e) {
            throw new EBException(EBException.CANT_FIND_UNICODEMAP);
        }
    }

    private void addEntry(Integer keyNum, String val, String alt, boolean wide) {
        if (val.startsWith("u") || val.startsWith("U")) {
            if (val.contains(",")) {
                StringBuilder sb = new StringBuilder();
                for (String item : val.split(",")) {
                    if (item.startsWith("u")) {
                        sb.append(Character.toChars(
                                Integer.valueOf(item.substring(1, 5), 16)));
                    } else if (item.startsWith("U")) {
                        sb.append(Character.toChars(
                                Integer.valueOf(item.substring(1, 9), 16)));
                    } else {
                        sb.append(item);
                    }
                }
                if (wide) {
                    wideMap.put(keyNum, sb.toString());
                } else {
                    narrowMap.put(keyNum, sb.toString());
                }
            } else {
                String uniValue;
                if (val.startsWith("u")) {
                    uniValue = new String(Character.toChars(
                            Integer.valueOf(val.substring(1, 5), 16)));
                } else {
                    uniValue = new String(Character.toChars(
                            Integer.valueOf(val.substring(1, 9), 16)));
                }
                if (wide) {
                    wideMap.put(keyNum, uniValue);
                } else {
                    narrowMap.put(keyNum, uniValue);
                }
            }
        } else if (val.equals("-") && alt != null) {
            if (wide) {
                wideMap.put(keyNum, alt);
            } else {
                narrowMap.put(keyNum, alt);
            }
        }
    }
}
