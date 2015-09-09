package fuku.webbook;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;

/**
 * 複合検索Bean確認クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class MultiSearchFormValidator implements Validator {

    /**
     * コンストラクタ。
     *
     */
    public MultiSearchFormValidator() {
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
        return MultiSearchForm.class.isAssignableFrom(clazz);
    }

    /**
     * 指定されたオブジェクトを確認します。
     *
     * @param target 確認対象のオブジェクト
     * @param errors バリデーションエラー保持オブジェクト
     */
    @Override
    public void validate(Object target, Errors errors) {
        MultiSearchForm form = (MultiSearchForm)target;
        String[] word = form.getWord();
        int n = word.length;
        boolean blank = true;
        for (int i=0; i<n; i++) {
            if (StringUtils.isNotBlank(word[i])) {
                blank = false;
            }
        }
        if (blank) {
            for (int i=0; i<n; i++) {
                String key = "word[" + i + "]";
                errors.rejectValue(key, "error.required", "Required");
            }
        }
    }
}

// end of MultiSearchFormValidator.java
