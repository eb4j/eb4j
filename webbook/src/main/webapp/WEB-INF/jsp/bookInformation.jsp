<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="webbook" uri="/webbook" %>
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

    <li class="expanded">
    <c:choose>
      <c:when test="${content.bookId == -1}">
        <strong><span>書籍の情報</span></strong>
      </c:when>
      <c:otherwise>
        <a href="<c:url value='bookInformation.html'/>">書籍の情報</a>
      </c:otherwise>
    </c:choose>
    <ul>
    <c:forEach items="${webbook.bookEntryList}" var="entry">
      <c:url value="bookInformation.html" var="url">
        <c:param name="bookId" value="${entry.id}"/>
      </c:url>
      <li class="none">
      <c:choose>
        <c:when test="${entry.id == content.bookId}">
        <strong>
        <c:choose>
          <c:when test="${content.position == -1}">
            <span><c:out value="${entry.name}"/></span>
          </c:when>
          <c:otherwise>
            <a href="${url}"><c:out value="${entry.name}"/></a>
          </c:otherwise>
        </c:choose>
        </strong>
        </c:when>
        <c:otherwise>
          <a href="${url}"><c:out value="${entry.name}"/></a>
        </c:otherwise>
      </c:choose>
      </li>
    </c:forEach>
    </ul>
  </div>
</div>

<div id="bodyColumn">
  <div id="contentBox">
    <div class="section">
    <c:choose>
      <c:when test="${content.bookId == -1}">
        <h2>書籍の情報</h2>
        <ul>
        <c:forEach items="${webbook.bookEntryList}" var="entry">
          <c:url value="bookInformation.html" var="url">
            <c:param name="bookId" value="${entry.id}"/>
          </c:url>
          <li>
          <a href="${url}"><c:out value="${entry.name}"/></a>
          </li>
        </c:forEach>
        </ul>
      </c:when>
      <c:when test="${content.position == 0}">
        <h2><c:out value="${content.bookName}"/></h2>
        <h3>半角外字一覧</h3>
        <webbook:fontTable bookEntry="${content.bookEntry}" fontType="narrow"/>
      </c:when>
      <c:when test="${content.position == 1}">
        <h3>全角外字一覧</h3>
        <webbook:fontTable bookEntry="${content.bookEntry}" fontType="wide"/>
      </c:when>
      <c:otherwise>
        <h2><c:out value="${content.bookName}"/></h2>
        <table class="infoTable">
        <tr>
          <th>書籍の種類</th>
          <td><c:out value="${content.bookType}"/></td>
        </tr>
        <tr>
          <th>文字セット</th>
          <td><c:out value="${content.charset}"/></td>
        </tr>
        <tr>
          <th>検索方法</th>
          <td>
          <c:forEach items="${content.searchMethodList}" var="method">
            <c:out value="${method}"/><br>
          </c:forEach>
          </td>
        </tr>
        <tr>
          <th>外字サイズ</th>
          <td>
          <c:choose>
            <c:when test="${empty content.fontSizeList}">
              -
            </c:when>
            <c:otherwise>
              <c:forEach items="${content.fontSizeList}" var="size">
                <c:out value="${size}"/>
              </c:forEach>
            </c:otherwise>
          </c:choose>
          </td>
        </tr>
        <tr>
          <th>半角外字コード</th>
          <td>
          <c:choose>
            <c:when test="${empty content.narrowFontRange}">
              -
            </c:when>
            <c:otherwise>
              <c:url value="bookInformation.html" var="url">
                <c:param name="bookId" value="${content.bookId}"/>
                <c:param name="position" value="0"/>
              </c:url>
              ${content.narrowFontRange} (<a href="${url}">一覧</a>)
            </c:otherwise>
          </c:choose>
          </td>
        </tr>
        <tr>
          <th>全角外字コード</th>
          <td>
          <c:choose>
            <c:when test="${empty content.wideFontRange}">
              -
            </c:when>
            <c:otherwise>
              <c:url value="bookInformation.html" var="url">
                <c:param name="bookId" value="${content.bookId}"/>
                <c:param name="position" value="1"/>
              </c:url>
              ${content.wideFontRange} (<a href="${url}">一覧</a>)
            </c:otherwise>
          </c:choose>
          </td>
        </tr>
        </table>
      </c:otherwise>
    </c:choose>
    </div>
  </div>
</div>

<div class="clear"><hr></div>

<div id="footer">
  <div class="clear"><hr></div>
</div>

</body>
</html>

<%-- end of bookInformation.jsp --%>
