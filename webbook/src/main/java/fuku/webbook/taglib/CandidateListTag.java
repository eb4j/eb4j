package fuku.webbook.taglib;

import java.io.IOException;
import java.util.List;
import javax.servlet.jsp.JspWriter;

import org.springframework.web.servlet.tags.HtmlEscapingAwareTag;

import fuku.webbook.Candidate;

/**
 * 候補タグ。
 *
 * @author Hisaya FUKUMOTO
 */
public class CandidateListTag extends HtmlEscapingAwareTag {

    /** 候補リスト */
    private List _candidateList = null;
    /** IDの接頭辞 */
    private String _prefix = null;


    /**
     * コンストラクタ。
     *
     */
    public CandidateListTag() {
        super();
    }


    /**
     * 候補リストを設定します。
     *
     * @param candidate 候補リスト
     */
    public void setCandidate(List candidate) {
        _candidateList = candidate;
    }

    /**
     * 候補リストを返します。
     *
     * @return 候補リスト
     */
    public List getCandidate() {
        return _candidateList;
    }

    /**
     * IDの接頭辞を設定します。
     *
     * @param prefix IDの接頭辞
     */
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }

    /**
     * IDの接頭辞を返します。
     *
     * @return IDの接頭辞
     */
    public String getPrefix() {
        return _prefix;
    }

    /**
     * タグ内容を出力します。
     *
     * @return SKIP_BODY
     * @exception Exception 出力エラーが発生した場合
     */
    @Override
	protected int doStartTagInternal() throws Exception {
        int len = 0;
        if (_candidateList != null) {
            len = _candidateList.size();
        }
        if (len > 0) {
            JspWriter jspWriter = pageContext.getOut();

            String id = _prefix + "_component0";
            jspWriter.println("<div id=\"" + id + "\" class=\"component\">");
            id = _prefix + "_scroll0";
            jspWriter.println("<div id=\"" + id + "\" class=\"scroll\">");
            id = _prefix + "_group0";
            jspWriter.println("<div id=\"" + id + "\" class=\"group\">");
            id = _prefix + "_item0";
            jspWriter.print("<div id=\"" + id + "\" class=\"item\">");
            jspWriter.print("未選択");
            jspWriter.println("</div>");

            for (int i=0; i<len; i++) {
                Candidate candidate = (Candidate)_candidateList.get(i);
                if (candidate.isGroup()) {
                    id = _prefix + "_groupItem" + candidate.getIndex();
                    jspWriter.print("<div id=\"" + id + "\" class=\"groupItem\">");
                } else {
                    id = _prefix + "_item" + candidate.getIndex();
                    jspWriter.print("<div id=\"" + id + "\" class=\"item\">");
                }
                jspWriter.print(candidate.getLabel());
                jspWriter.println("</div>");
            }
            jspWriter.println("</div>");
            jspWriter.println("</div>");

            id = _prefix + "_up0";
            jspWriter.print("<div id=\"" + id + "\" class=\"arrow\">");
            id = _prefix + "_upImage0";
            jspWriter.print("<img id=\"" + id + "\" src=\"up.gif\">");
            jspWriter.println("</div>");

            id = _prefix + "_down0";
            jspWriter.print("<div id=\"" + id + "\" class=\"arrow\">");
            id = _prefix + "_downImage0";
            jspWriter.print("<img id=\"" + id + "\" src=\"down.gif\">");
            jspWriter.println("</div>");

            jspWriter.println("</div>");

            for (int i=0; i<len; i++) {
                Candidate candidate = (Candidate)_candidateList.get(i);
                if (candidate.isGroup()) {
                    _writeCandidate(jspWriter, candidate);
                }
            }
        }
        return SKIP_BODY;
    }

    /**
     * 候補を出力します。
     *
     * @param jspWriter JSPライタ
     * @param candidate 候補
     * @exception IOException 出力エラーが発生した場合
     */
    private void _writeCandidate(JspWriter jspWriter,
                                 Candidate candidate) throws IOException {
        String id = _prefix + "_component" + candidate.getIndex();
        jspWriter.println("<div id=\"" + id + "\" class=\"component\">");
        id = _prefix + "_scroll" + candidate.getIndex();
        jspWriter.println("<div id=\"" + id + "\" class=\"scroll\">");
        id = _prefix + "_group" + candidate.getIndex();
        jspWriter.println("<div id=\"" + id + "\" class=\"group\">");

        List<Candidate> candidateList = candidate.getCandidateList();
        int len = candidateList.size();
        for (int i=0; i<len; i++) {
            Candidate item = candidateList.get(i);
            if (item.isGroup()) {
                id = _prefix + "_groupItem" + item.getIndex();
                jspWriter.print("<div id=\"" + id + "\" class=\"groupItem\">");
            } else {
                id = _prefix + "_item" + item.getIndex();
                jspWriter.print("<div id=\"" + id + "\" class=\"item\">");
            }
            jspWriter.print(item.getLabel());
            jspWriter.println("</div>");
        }
        jspWriter.println("</div>");
        jspWriter.println("</div>");

        id = _prefix + "_up" + candidate.getIndex();
        jspWriter.print("<div id=\"" + id + "\" class=\"arrow\">");
        id = _prefix + "_upImage" + candidate.getIndex();
        jspWriter.print("<img id=\"" + id + "\" src=\"up.gif\">");
        jspWriter.println("</div>");

        id = _prefix + "_down" + candidate.getIndex();
        jspWriter.print("<div id=\"" + id + "\" class=\"arrow\">");
        id = _prefix + "_downImage" + candidate.getIndex();
        jspWriter.print("<img id=\"" + id + "\" src=\"down.gif\">");
        jspWriter.println("</div>");

        jspWriter.println("</div>");

        for (int i=0; i<len; i++) {
            Candidate item = candidateList.get(i);
            if (item.isGroup()) {
                _writeCandidate(jspWriter, item);
            }
        }
    }
}

// end of CandidateListTag.java
