package io.github.eb4j.webbook.taglib;

import java.util.List;
import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.tags.form.AbstractFormTag;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TagWriter;
import org.springframework.web.util.TagUtils;

import io.github.eb4j.webbook.Candidate;

/**
 * 候補タグ。
 *
 * @author Hisaya FUKUMOTO
 */
public class CandidateOptionTag extends AbstractFormTag {

    /** 候補リスト */
    private List _candidateList = null;
    /** IDの接頭辞 */
    private String _prefix = null;


    /**
     * コンストラクタ。
     *
     */
    public CandidateOptionTag() {
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
     * @param tagWriter タグライタ
     * @return SKIP_BODY
     * @exception JspException 出力エラーが発生した場合
     */
    @Override
    protected int writeTagContent(TagWriter tagWriter) throws JspException {
        TagUtils.assertHasAncestorOfType(this, SelectTag.class, "candidate", "select");
        BindStatus bindStatus =
            (BindStatus)pageContext.getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
        int len = 0;
        if (_candidateList != null) {
            len = _candidateList.size();
        }
        if (len > 0) {
            tagWriter.startTag("option");
            String id = _prefix + "_option0";
            tagWriter.writeAttribute("id", id);
            tagWriter.writeAttribute("value", "");
            if (_isSelected(bindStatus, "")) {
                tagWriter.writeAttribute("selected", "selected");
            }
            tagWriter.appendValue("未選択");
            tagWriter.endTag();
            for (int i=0; i<len; i++) {
                Candidate candidate = (Candidate)_candidateList.get(i);
                _writeCandidate(tagWriter, candidate, bindStatus);
            }
        }
        return SKIP_BODY;
    }

    /**
     * 候補を出力します。
     *
     * @param tagWriter タグライタ
     * @param candidate 候補
     * @param bindStatus BindStatusのインスタンス
     * @exception JspException 出力エラーが発生した場合
     */
    private void _writeCandidate(TagWriter tagWriter,
                                 Candidate candidate,
                                 BindStatus bindStatus) throws JspException {
        String label = candidate.getEscapedLabel();
        if (candidate.isGroup()) {
            tagWriter.startTag("optgroup");
            tagWriter.writeAttribute("label", label);
			tagWriter.forceBlock();
            List<Candidate> list = candidate.getCandidateList();
            int n = list.size();
            for (int i=0; i<n; i++) {
                _writeCandidate(tagWriter, list.get(i), bindStatus);
            }
            tagWriter.endTag();
        } else {
            tagWriter.startTag("option");
            String id = _prefix + "_option" + candidate.getIndex();
            tagWriter.writeAttribute("id", id);
            tagWriter.writeAttribute("value", label);
            if (_isSelected(bindStatus, label)) {
                tagWriter.writeAttribute("selected", "selected");
            }
            tagWriter.appendValue(label);
            tagWriter.endTag();
        }
    }

    /**
     * 値が選択されているかどうかを判定します。
     *
     * @param bindStatus BindStatusのインスタンス
     * @param value 値
     * @return 選択されている場合はtrue、そうでない場合はfalse
     */
    private boolean _isSelected(BindStatus bindStatus, String value) {
        Object boundValue = null;
        if (bindStatus != null) {
            if (bindStatus.getEditor() != null) {
                boundValue = bindStatus.getEditor().getValue();
            }
            if (boundValue == null) {
                boundValue = bindStatus.getValue();
            }
        }
        return value.equals(boundValue);
    }
}

// end of CandidateOptionTag.java
