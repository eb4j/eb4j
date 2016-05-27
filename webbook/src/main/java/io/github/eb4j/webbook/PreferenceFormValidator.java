package io.github.eb4j.webbook;

import java.util.Map;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;

/**
 * 表示設定Bean確認クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class PreferenceFormValidator implements Validator {

    /**
     * コンストラクタ。
     *
     */
    public PreferenceFormValidator() {
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
        return PreferenceForm.class.isAssignableFrom(clazz);
    }

    /**
     * 指定されたオブジェクトを確認します。
     *
     * @param target 確認対象のオブジェクト
     * @param errors バリデーションエラー保持オブジェクト
     */
    @Override
    public void validate(Object target, Errors errors) {
        PreferenceForm form = (PreferenceForm)target;
        WebBookBean webbook = form.getWebBookBean();
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

// end of PreferenceFormValidator.java
