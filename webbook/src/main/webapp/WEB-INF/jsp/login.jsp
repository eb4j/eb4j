<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
                      "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>WebBook</title>
</head>

<body>

<h2>認証</h2>

<form name="f" method="POST" action="j_security_check">
<table>
  <tr>
    <th>ログイン名：</th>
    <td><input type="text" name="j_username" value="${param.j_username}"></td>
  </tr>
  <tr>
    <th>パスワード：</th>
    <td><input type="password" name="j_password"></td>
  </tr>
  <tr>
    <td colspan="2">
      <input type="submit" value="ログイン">
    </td>
  </tr>
</table>
</form>
<script type="text/javascript">document.f.j_username.focus();</script>

<c:if test="${param.incorrect}">
<p>
認証に失敗しました。
入力されたログイン名とパスワードを確認してください。
</p>
</c:if>

</body>
</html>

<%-- end of login.jsp --%>
