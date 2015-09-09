package fuku.webbook.acl;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletRequest;

/**
 * IPアドレス制御クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class AddressEntry extends AbstractACLEntry {

    /** IPアドレスリスト */
    private List<byte[][]> _addrList = new ArrayList<byte[][]>();


    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     * @param list IPアドレスリスト
     */
    public AddressEntry(boolean allow, String list) {
        super(allow);
        String[] hostList = list.split(",\\s*");
        String host, mask;
        byte[][] bits;
        int i, j, n, m, b;
        int len = hostList.length;
        for (i=0; i<len; i++) {
            host = hostList[i].trim();
            n = host.indexOf("/");
            if (n >= 0) {
                try {
                    bits = new byte[2][];
                    mask = host.substring(n+1);
                    host = host.substring(0, n);
                    bits[0] = InetAddress.getByName(host).getAddress();
                    try {
                        // x.x.x.x/n 形式
                        m = Integer.parseInt(mask);
                        b = 0;
                        for (j=0; j<m; j++) {
                            b = (b >>> 1) | 0x80000000;
                        }
                        bits[1] = new byte[4];
                        for (j=0; j<4; j++) {
                            bits[1][j] = (byte)((b >>> (8 * (3 - j))) & 0xff);
                        }
                    } catch (NumberFormatException e) {
                        // x.x.x.x/y.y.y.y 形式
                        bits[1] = InetAddress.getByName(mask).getAddress();
                    }
                    _addrList.add(bits);
                } catch (UnknownHostException e) {
                }
            } else {
                try {
                    // x.x.x.x
                    bits = new byte[2][];
                    bits[0] = InetAddress.getByName(host).getAddress();
                    bits[1] = new byte[4];
                    bits[1][0] = (byte)0xff;
                    bits[1][1] = (byte)0xff;
                    bits[1][2] = (byte)0xff;
                    bits[1][3] = (byte)0xff;
                    _addrList.add(bits);
                } catch (UnknownHostException e) {
                }
            }
        }
    }

    /**
     * 指定された要求情報について、許可するかどうかを返します。
     *
     * @param req サーブレット要求情報
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isAllowed(ServletRequest req) {
        return isAllowed(req.getRemoteAddr());
    }

    /**
     * 指定されたホストが許可されているかどうかを判定します。
     *
     * @param host ホスト名またはIPアドレス
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    public boolean isAllowed(String host) {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return false;
        }
        return isAllowed(addr);
    }

    /**
     * 指定されたIPアドレスが許可されているかどうかを判定します。
     *
     * @param ipaddr IPアドレス
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    public boolean isAllowed(InetAddress ipaddr) {
        boolean match = false;
        if (ipaddr instanceof Inet4Address) {
            int i, j, addr, mask;
            byte[][] bits;

            int target = 0;
            byte[] b = ipaddr.getAddress();
            for (i=0; i<4; i++) {
                target = (target << 8) | (b[i] & 0xff);
            }

            int size = _addrList.size();
            for (i=0; i<size; i++) {
                bits = _addrList.get(i);
                addr = 0;
                for (j=0; j<4; j++) {
                    addr = (addr << 8) | (bits[0][j] & 0xff);
                }
                mask = 0;
                for (j=0; j<4; j++) {
                    mask = (mask << 8) | (bits[1][j] & 0xff);
                }
                addr = (target ^ addr) & mask;
                if (addr == 0) {
                    match = true;
                    break;
                }
            }
        }
        return !(match ^ isAllowEntry());
    }
}

// end of AddressEntry.java
