package fuku.webbook;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * URNリダイレクトサーブレットクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class URNServlet extends HttpServlet {

    private static final int IETF_RFC = 0;
    private static final int IETF_FYI = 1;
    private static final int IETF_STD = 2;
    private static final int IETF_BCP = 3;
    private static final int IETF_ID = 4;
    private static final int ISSN = 5;
    private static final int ISBN = 6;

    private static final Pattern[] URN = {
        Pattern.compile("urn:ietf:rfc:(\\d+)"),
        Pattern.compile("urn:ietf:fyi:(\\d+)"),
        Pattern.compile("urn:ietf:std:(\\d+)"),
        Pattern.compile("urn:ietf:bcp:(\\d+)"),
        Pattern.compile("urn:ietf:id:[A-Za-z\\d-]+"),
        Pattern.compile("urn:issn:(\\d{4}-\\d{3}[\\dX])"),
        Pattern.compile("urn:isbn:(\\d-\\d+-\\d+-[\\dx])")
    };


    /**
     * コンストラクタ。
     *
     */
    public URNServlet() {
        super();
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
        String query = req.getQueryString();
        if (StringUtils.isBlank(query)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Matcher m;
        String url = null;
        int n = URN.length;
        for (int i=0; i<n; i++) {
            m = URN[i].matcher(query);
            if (m.matches()) {
                String str = m.group(1);
                switch (i) {
                    case IETF_RFC:
                    case IETF_FYI:
                    case IETF_STD:
                    case IETF_BCP:
                    case IETF_ID:
                        url = _getIETF(i, str);
                        break;
                    case ISSN:
                        url = _getISSN(str);
                        break;
                    case ISBN:
                        url = _getISBN(str);
                        break;
                    default:
                }
                break;
            }
        }

        if (StringUtils.isBlank(url)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        res.sendRedirect(url);
    }

    /**
     * IETFのリダイレクトURLを返します。
     *
     * @param type IETFタイプ
     * @param str クエリ文字列
     * @return リダイレクトURL
     */
    private String _getIETF(int type, String str) {
        String url = "http://www.ietf.org/";
        switch (type) {
            case IETF_RFC:
                url += "rfc/rfc" + str + ".txt";
                break;
            case IETF_FYI:
                url += "rfc/fyi/fyi" + str + ".txt";
                break;
            case IETF_STD:
                url += "rfc/std/std" + str + ".txt";
                break;
            case IETF_BCP:
                url += "rfc/bcp/bcp" + str + ".txt";
                break;
            case IETF_ID:
                url += "internet-drafts/draft-" + str + ".txt";
                break;
            default:
                break;
        }
        return url;
    }

    /**
     * ISSNのリダイレクトURLを返します。
     *
     * @param str クエリ文字列
     * @return リダイレクトURL
     */
    private String _getISSN(String str) {
        return "http://www.issn.org/urn/?issn=" + str;
    }

    /**
     * ISBNのリダイレクトURLを返します。
     *
     * @param str クエリ文字列
     * @return リダイレクトURL
     */
    private String _getISBN(String str) {
        str = str.replaceAll("-", "");
        String url = null;
        char ch = str.charAt(0);
        switch (ch) {
            case '4':
                url = "http://www.amazon.co.jp/exec/obidos/ASIN/" + str;
                break;
            case '3':
                url = "http://www.amazon.de/exec/obidos/ASIN/" + str;
                break;
            case '2':
                url = "http://www.amazon.fr/exec/obidos/ASIN/" + str;
                break;
            case '1':
                url = "http://www.amazon.co.uk/exec/obidos/ASIN/" + str;
                break;
            case '0':
            default:
                url = "http://www.amazon.com/exec/obidos/ASIN/" + str;
                break;
        }
        return url;
    }
}

// end of URNServlet.java
