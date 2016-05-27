package fuku.webbook;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.util.WebUtils;

import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_BEAN;
import static fuku.webbook.WebBookConstants.COOKIE_WEBBOOK;

/**
 * 表示設定コントローラ。
 *
 * @author Hisaya FUKUMOTO
 */
public class PreferenceFormController extends SimpleFormController {


    /**
     * コンストラクタ。
     *
     */
    public PreferenceFormController() {
        super();
    }

    /**
     * データ入力画面表示に利用するコマンドを返します。
     *
     * @param req クライアントからのリクエスト
     * @exception Exception エラーが発生した場合
     */
    @Override
    protected Object formBackingObject(HttpServletRequest req) throws Exception {
        WebBookCookieBean cookie = new WebBookCookieBean();
        cookie.setCookie(WebUtils.getCookie(req, COOKIE_WEBBOOK));
        PreferenceForm form = (PreferenceForm)super.formBackingObject(req);
        form.setMethod(cookie.getMethod());
        form.setMaximum(cookie.getMaximum());
        form.setInlineImage(cookie.isInlineImage());
        form.setInlineObject(cookie.isInlineObject());
        form.setCandidateSelector(cookie.isCandidateSelector());
        WebBookBean webbook =
            (WebBookBean)WebUtils.getSessionAttribute(req, KEY_WEBBOOK_BEAN);
        form.setWebBookBean(webbook);
        return form;
    }

    /**
     * フォーム送信処理を行います。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @param command フォームオブジェクト
     * @param errors エラーインスタンス
     * @return ModelAndViewのインスタンス
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest req,
                                    HttpServletResponse res,
                                    Object command,
                                    BindException errors) {
        PreferenceForm form = (PreferenceForm)command;

        WebBookCookieBean cookie = new WebBookCookieBean();
        cookie.setCookie(WebUtils.getCookie(req, COOKIE_WEBBOOK));

        cookie.setMethod(form.getMethod());
        cookie.setMaximum(form.getMaximum());
        cookie.setInlineImage(form.isInlineImage());
        cookie.setInlineObject(form.isInlineObject());
        cookie.setCandidateSelector(form.isCandidateSelector());
        res.addCookie(cookie.getCookie());

        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("command", form);
        map.put("webbook", form.getWebBookBean());
        return new ModelAndView(getSuccessView(), map);
    }

    /**
     * コマンド以外のモデルデータを作成します。
     *
     * @param req クライアントからのリクエスト
     * @param command フォームオブジェクト
     * @param errors エラーインスタンス
     * @return モデルのマップ
     */
    @Override
    protected Map<Object,Object> referenceData(HttpServletRequest req,
                                               Object command, Errors errors) {
        PreferenceForm form = (PreferenceForm)command;
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("webbook", form.getWebBookBean());
        return map;
    }
}

// end of PreferenceFormController.java
