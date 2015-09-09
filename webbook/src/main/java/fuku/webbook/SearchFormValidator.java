package fuku.webbook;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;

/**
 * 単語検索Bean確認クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class SearchFormValidator implements Validator {

    /**
     * コンストラクタ。
     *
     */
    public SearchFormValidator() {
        super();
    }


    /**
     * 指定されたクラスをサポートしているかどうかを返します。
     *
     * @param clazz クラス
     * @return サポートしている場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean supports(Class clazz) {
        return SearchForm.class.isAssignableFrom(clazz);
    }

    /**
     * 指定されたオブジェクトを確認します。
     *
     * @param target 確認対象のオブジェクト
     * @param errors バリデーションエラー保持オブジェクト
     */
    @Override
    public void validate(Object target, Errors errors) {
        SearchForm form = (SearchForm)target;
        if (StringUtils.isBlank(form.getWord())) {
            errors.rejectValue("word", "error.required", "Required");
        }
        int targetId = form.getTarget();
        WebBookBean webbook = form.getWebBookBean();
        if (targetId != 0 && webbook.getBookEntry(targetId) == null) {
            errors.rejectValue("target", "error.invalid", "Invalid");
        }
        int method = form.getMethod();
        Map<Integer,String> map = webbook.getSearchMethodMap();
        if (!map.containsKey(method)) {
            errors.rejectValue("method", "error.invalid", "Invalid");
        }
        if (form.getMaximum() <= 0) {
            errors.rejectValue("maximum", "error.invalid", "Invalid");
        }
    }
}

// end of SearchFormValidator.java
