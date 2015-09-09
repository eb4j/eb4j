<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
                      "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Style-Type" content="text/css">
<link rel="stylesheet" href="webbook.css" type="text/css" media="screen">
<title>WebBook</title>
</head>

<body>

<div id="banner">
  <span>WebBook</span>
  <div class="clear"><hr></div>
</div>

<div id="header">
  <div class="xleft">
    <strong><span>表示設定</span></strong>
    | <a href="<c:url value='about.html'/>">このソフトウェアについて</a>
    <c:if test="${pageContext.request.remoteUser != null
                  && pageContext.request.authType == 'FORM'}">
    | <a href="<c:url value='logout.html'/>">ログアウト</a>
    </c:if>
  </div>
  <div class="xright">
    <span>Version 1.0.5</span>
  </div>
  <div class="clear"><hr></div>
</div>

<div id="leftColumn">
  <div id="navcolumn">
    <h5>メニュー</h5>
    <ul>
    <li class="none">
      <a href="<c:url value='search.html'/>">単語検索</a>
    </li>

    <c:choose>
      <c:when test="${webbook.multiSupported}">
      <li class="collapsed">
        <a href="<c:url value='multiSearchList.html'/>">複合検索</a>
      </li>
      </c:when>
      <c:otherwise>
      <li class="none">
        <span>複合検索</span>
      </li>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${webbook.menuSupported}">
      <li class="collapsed">
        <a href="<c:url value='bookMenu.html'/>">書籍のメニュー</a>
      </li>
      </c:when>
      <c:otherwise>
      <li class="none">
        <span>書籍のメニュー</span>
      </li>
      </c:otherwise>
    </c:choose>

    <c:choose>
      <c:when test="${webbook.copyrightSupported}">
      <li class="collapsed">
        <a href="<c:url value='bookCopyright.html'/>">書籍の著作権</a>
      </li>
      </c:when>
      <c:otherwise>
      <li class="none">
        <span>書籍の著作権</span>
      </li>
      </c:otherwise>
    </c:choose>

    <li class="collapsed">
      <a href="<c:url value='bookInformation.html'/>">書籍の情報</a>
    </li>
    </ul>
  </div>
</div>

<div id="bodyColumn">
  <div id="contentBox">
    <div class="section">
      <h2>表示設定</h2>

      <c:url value="preference.html" var="url"/>
      <form:form action="${url}">
      <table class="preference">
      <tr>
      <th>検索方法</th>
      <td>
      <form:select path="method">
        <c:choose>
          <c:when test="${empty webbook.searchMethodMap}">
            <form:option value="-1" label="検索方法なし"/>
          </c:when>
          <c:otherwise>
            <form:options items="${webbook.searchMethodMap}"/>
          </c:otherwise>
        </c:choose>
      </form:select>
      </td>
      </tr>

      <tr>
      <th>表示件数</th>
      <td>
      <form:select path="maximum">
        <c:forEach begin="10" end="50" step="10" varStatus="vs">
          <form:option value="${vs.current}" label="${vs.current}"/>
        </c:forEach>
      </form:select>
      </td>
      </tr>

      <tr>
      <th>インライン表示</th>
      <td>
      <label><form:radiobutton path="inlineImage" value="false"/>OFF</label>
      <label><form:radiobutton path="inlineImage" value="true"/>ON</label>
      </td>
      </tr>

      <tr>
      <th>インライン再生</th>
      <td>
      <label><form:radiobutton path="inlineObject" value="false"/>OFF</label>
      <label><form:radiobutton path="inlineObject" value="true"/>ON</label>
      </td>
      </tr>

      <tr>
      <th>候補セレクタ</th>
      <td>
      <label><form:radiobutton path="candidateSelector" value="false"/>OFF</label>
      <label><form:radiobutton path="candidateSelector" value="true"/>ON</label>
      </td>
      </tr>

      <tr>
      <td class="button" colspan="2">
      <input type="submit" value="保存">
      <input type="reset" value="リセット">
      </td>
      </tr>
      </table>
      </form:form>

      <dl class="preference">
        <dt>検索方法</dt>
        <dd>単語検索でのデフォルトの検索方法を指定します。</dd>
        <dt>表示件数</dt>
        <dd>単語検索での書籍毎の検索結果の最大件数を指定します。</dd>
        <dt>インライン表示</dt>
        <dd>画像をインラインで表示するかどうかを指定します。</dd>
        <dt>インライン再生</dt>
        <dd>音声/動画をインラインで再生するかどうかを指定します。</dd>
        <dt>候補セレクタ</dt>
        <dd>複合検索での候補表示にJavaScriptを使用するかどうかを指定します。
            候補に含まれる外字を表示したい場合はONにしてください。
        </dd>
      </dl>

      <p class="preference">
        クッキー未対応のブラウザからは表示設定の保存はできません。
      </p>
    </div>
  </div>
</div>

<div class="clear"><hr></div>

<div id="footer">
  <div class="clear"><hr></div>
</div>

</body>
</html>

<%-- end of preference.jsp --%>
