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
      <a href="<c:url value='search.html'/>">単語検索</a>
    </li>

    <c:choose>
      <c:when test="${webbook.multiSupported}">
      <li class="expanded">
      <a href="<c:url value='multiSearchList.html'/>">複合検索</a>
      <ul>
      <c:forEach items="${webbook.multiBookEntryList}" var="bookEntry">
        <li class="expanded"><c:out value="${bookEntry.name}"/>
        <ul>
        <c:forEach items="${bookEntry.multiSearchEntryList}" var="multiEntry">
          <c:url value="multiSearch.html" var="url">
            <c:param name="bookId" value="${bookEntry.id}"/>
            <c:param name="multiId" value="${multiEntry.id}"/>
          </c:url>
          <li class="none">
          <c:choose>
            <c:when test="${bookEntry.id == multi.bookId && multiEntry.id == multi.multiId}">
              <strong>
              <a href="${url}"><c:out value="${multiEntry.name}"/></a>
              </strong>
            </c:when>
            <c:otherwise>
              <a href="${url}"><c:out value="${multiEntry.name}"/></a>
            </c:otherwise>
          </c:choose>
          </li>
        </c:forEach>
        </ul>
        </li>
      </c:forEach>
      </ul>
      </li>
      </c:when>
      <c:otherwise>
      <li class="none">
        <strong><span>複合検索</span></strong>
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

    <c:if test="${!empty resultList}">
    <h5>検索結果</h5>
    <ul>
    <li class="expanded">
      <c:choose>
        <c:when test="${content.position == -1}">
          <c:out value="${multi.bookName}"/>
        </c:when>
        <c:otherwise>
          <strong><c:out value="${multi.bookName}"/></strong>
        </c:otherwise>
      </c:choose>
      <ul>
      <c:forEach items="${resultList}" var="result">
        <li class="none">
          <c:choose>
            <c:when test="${result.textPosition == content.position}">
              ${result.heading}
            </c:when>
            <c:otherwise>
              <c:url value="multiSearchResult.html" var="url">
                <c:param name="bookId" value="${multi.bookId}"/>
                <c:param name="multiId" value="${multi.multiId}"/>
                <c:param name="position" value="${result.textPosition}"/>
              </c:url>
              <a href="${url}">${result.anchorHeading}</a>
            </c:otherwise>
          </c:choose>
         </li>
      </c:forEach>
      </ul>
    </li>
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

<%-- end of multiSearchResult.jsp --%>
