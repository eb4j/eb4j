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
<meta http-equiv="Content-Script-Type" content="text/javascript">
<script type="text/javascript" src="js/prototype.js"></script>
<script type="text/javascript" src="webbook.js"></script>
<title>WebBook</title>
</head>

<body>

<div id="banner">
  <span>WebBook</span>
  <div class="clear"><hr></div>
</div>

<div id="header">
  <div class="xleft">
    <a href="<c:url value='preference.html'/>">表示設定</a>
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
      <strong><span>単語検索</span></strong>
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
      <h2>単語検索</h2>

      <c:url value="search.html" var="url"/>
      <form:form action="${url}">
      <table class="searchTable">
      <tr>
      <th>検索語</th>
      <td>
      <form:input path="word" cssClass="searchWord"/>
      </td>
      <td>
      <input type="submit" value="検索">
      </td>
      </tr>

      <tr>
      <th>検索対象</th>
      <td>
      <form:select path="target">
        <c:choose>
          <c:when test="${empty webbook.bookEntryList}">
            <form:option value="-1" label="書籍が存在しません"/>
          </c:when>
          <c:otherwise>
            <form:option value="0" label="すべての書籍"/>
            <form:options items="${webbook.bookEntryList}"
                          itemValue="id" itemLabel="name"/>
          </c:otherwise>
        </c:choose>
      </form:select>
      </td>
      </tr>

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
      </table>
      </form:form>
    </div>

    <c:if test="${resultMap != null}">
    <div class="section">
      <h2>検索結果</h2>
      <c:choose>
        <c:when test="${empty resultMap}">
          該当する項目がありませんでした。
        </c:when>
        <c:otherwise>
        <c:forEach items="${resultMap}" var="entry">
          <h3><c:out value="${entry.key.name}"/></h3>
          <ul>
          <c:forEach items="${entry.value}" var="result">
            <c:url value="searchResult.html" var="url">
              <c:param name="bookId" value="${entry.key.id}"/>
              <c:param name="position" value="${result.textPosition}"/>
            </c:url>
            <li>
              <a href="${url}">${result.anchorHeading}</a>
            </li>
          </c:forEach>
          </ul>
        </c:forEach>
        </c:otherwise>
      </c:choose>
    </div>
    </c:if>
  </div>
</div>

<div class="clear"><hr></div>

<div id="footer">
  <div class="clear"><hr></div>
</div>

</body>
</html>

<%-- end of search.jsp --%>
