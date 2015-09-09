package fuku.xml2eb.util;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.apache.commons.io.IOUtils;

/**
 * XMLユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class XmlUtil {

    /**
     * コンストラクタ。
     *
     */
    private XmlUtil() {
        super();
    }


    /**
     * DOMドキュメントをファイルに出力します。
     *
     * @param doc DOMドキュメントオブジェクト
     * @param file 出力ファイル
     * @exception IOException 入出力エラーが発生した場合
     */
    public static void write(Document doc, File file) throws IOException {
        InputStream stream = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trans = null;
            stream = XmlUtil.class.getResourceAsStream("indent.xsl");
            if (stream != null) {
                Templates template =
                    factory.newTemplates(new StreamSource(stream));
                trans = template.newTransformer();
            } else {
                trans = factory.newTransformer();
                trans.setOutputProperty("method", "xml");
                trans.setOutputProperty("encoding", "UTF-8");
                trans.setOutputProperty("indent", "yes");
            }
            trans.transform(new DOMSource(doc), new StreamResult(file));
        } catch (TransformerException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}

// end of XmlUtil.java
