<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
      <strong><a href="<c:url value='search.html'/>">単語検索</a></strong>
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

    <c:if test="${!empty resultMap}">
    <h5>検索結果</h5>
    <ul>
    <c:forEach items="${resultMap}" var="entry">
      <li class="expanded">
        <c:choose>
          <c:when test="${entry.key.id == content.bookId}">
            <strong><c:out value="${entry.key.name}"/></strong>
          </c:when>
          <c:otherwise>
            <c:out value="${entry.key.name}"/>
          </c:otherwise>
        </c:choose>
        <ul>
        <c:forEach items="${entry.value}" var="result">
          <li class="none">
            <c:choose>
              <c:when test="${entry.key.id == content.bookId && result.textPosition == content.position}">
                ${result.heading}
              </c:when>
              <c:otherwise>
                <c:url value="searchResult.html" var="url">
                  <c:param name="bookId" value="${entry.key.id}"/>
                  <c:param name="position" value="${result.textPosition}"/>
                </c:url>
                <a href="${url}">${result.anchorHeading}</a>
              </c:otherwise>
            </c:choose>
           </li>
        </c:forEach>
        </ul>
      </li>
    </c:forEach>
    </ul>
    </c:if>
  </div>
</div>

<div id="bodyColumn">
  <div id="contentBox">
    <div class="section">
      <h2><c:out value="${content.bookName}"/></h2>
      <div class="content">
      ${content.content}
      </div>
    </div>
  </div>
</div>

<div class="clear"><hr></div>

<div id="footer">
  <div class="clear"><hr></div>
</div>

</body>
</html>

<%-- end of searchResult.jsp --%>
