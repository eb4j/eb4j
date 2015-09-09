<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
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
          <li class="none">
          <c:choose>
            <c:when test="${bookEntry.id == multi.bookId && multiEntry.id == multi.multiId}">
              <strong><span><c:out value="${multiEntry.name}"/></span></strong>
            </c:when>
            <c:otherwise>
              <c:url value="multiSearch.html" var="url">
                <c:param name="bookId" value="${bookEntry.id}"/>
                <c:param name="multiId" value="${multiEntry.id}"/>
              </c:url>
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
  </div>
</div>

<div id="bodyColumn">
  <div id="contentBox">
    <div class="section">
      <h2>
        <c:out value="${multi.bookName}"/>：<c:out value="${multi.multiName}"/>
      </h2>

      <c:url value="multiSearch.html" var="url">
        <c:param name="bookId" value="${multi.bookId}"/>
        <c:param name="multiId" value="${multi.multiId}"/>
      </c:url>
      <form:form action="${url}">
      <table class="searchTable">
      <c:forEach items="${multi.entryMap}" var="entry" varStatus="vs">
        <tr>
        <th>${entry.key}</th>
        <c:choose>
          <c:when test="${empty entry.value}">
            <td>
              <form:input path="word[${vs.index}]"
                          id="word${vs.index}" cssClass="searchWord"/>
            </td>
            <td></td>
          </c:when>
          <c:when test="${command.candidateSelector}">
            <td>
              <form:select path="word[${vs.index}]" id="word${vs.index}"
                           cssStyle="display:none;" cssClass="candidate">
                <webbook:candidateOption candidate="${entry.value}"
                                         prefix="word${vs.index}"/>
              </form:select>
              <div id="word${vs.index}_value" class="selectedValue">　</div>
            </td>
            <td>
              <div id="word${vs.index}_tree" class="candidate">
                <div id="word${vs.index}_groupItem0" class="groupItem">候補</div>
                <webbook:candidateList candidate="${entry.value}"
                                       prefix="word${vs.index}"/>
              </div>
            </td>
          </c:when>
          <c:otherwise>
            <td>
              <form:select path="word[${vs.index}]" id="word${vs.index}">
                <webbook:candidateOption candidate="${entry.value}"
                                         prefix="word${vs.index}"/>
              </form:select>
            </td>
            <td></td>
          </c:otherwise>
        </c:choose>
        <c:if test="${vs.index == 0}">
          <td>
          <input type="submit" value="検索">
          </td>
        </c:if>
        </tr>
      </c:forEach>
      </table>
      </form:form>
    </div>

    <c:if test="${resultList != null}">
    <div class="section">
      <h2>検索結果</h2>
      <c:choose>
        <c:when test="${empty resultList}">
          該当する項目がありませんでした。
        </c:when>
        <c:otherwise>
        <h3><c:out value="${multi.bookName}"/></h3>
        <ul>
        <c:forEach items="${resultList}" var="result">
          <c:url value="multiSearchResult.html" var="url">
            <c:param name="bookId" value="${multi.bookId}"/>
            <c:param name="multiId" value="${multi.multiId}"/>
            <c:param name="position" value="${result.textPosition}"/>
          </c:url>
          <li>
            <a href="${url}">${result.anchorHeading}</a>
          </li>
        </c:forEach>
        </ul>
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

<%-- end of multiSearch.jsp --%>
