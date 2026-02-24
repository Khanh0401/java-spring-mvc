<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
            <!DOCTYPE html>
            <html lang="en">

            <head>
                <meta charset="utf-8" />
                <meta http-equiv="X-UA-Compatible" content="IE=edge" />
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
                <meta name="description" content="Dự án laptopshop" />
                <meta name="author" content="Hỏi Dân IT" />
                <title> Manage Order </title>
                <link href="/css/styles.css" rel="stylesheet" />
                <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
            </head>

            <body class="sb-nav-fixed">
                <jsp:include page="../layout/header.jsp" />
                <div id="layoutSidenav">
                    <jsp:include page="../layout/sidebar.jsp" />
                    <div id="layoutSidenav_content">
                        <main>
                            <div class="container-fluid px-4">
                                <h1 class="mt-4"> Manage Order </h1>
                                <ol class="breadcrumb mb-4">
                                    <li class="breadcrumb-item"> <a href="/admin"> Dashboard </a> </li>
                                    <li class="breadcrumb-item active"> Orders </li>
                                </ol>
                            </div>

                            <div class="mt-5">
                                <div class="row">
                                    <div class="col-12 mx-auto">
                                        <div class="d-flex justify-content-between">
                                            <h3> Table order </h3>
                                        </div>

                                        <hr />

                                        <table class="table table-bordered table-hover">
                                            <thead>
                                                <tr>
                                                    <th> ID </th>
                                                    <th> Total Price </th>
                                                    <th> User </th>
                                                    <th> Status </th>
                                                    <th> Action </th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                                <c:forEach var="order" items="${orders}">
                                                    <tr>
                                                        <th> ${order.id} </th>
                                                        <td>
                                                            <fmt:formatNumber type="number"
                                                                value="${order.totalPrice}" />
                                                        </td>
                                                        <td> ${order.receiverName} </td>
                                                        <td> ${order.status} </td>
                                                        <td>
                                                            <a href=" /admin/order/${order.id}" class="btn btn-success">
                                                                View </a>
                                                            <a href="/admin/order/update/${order.id}"
                                                                class="btn btn-warning mx-2">
                                                                Update </a>
                                                            <a href="/admin/order/delete/${order.id}"
                                                                class="btn btn-danger mx-2">
                                                                Delete </a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>

                                                <c:if test="${empty orders}">
                                                    <tr>
                                                        <td colspan="6" style="text-align: center;">
                                                            Không có order
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>

                                        <!-- Phân trang start here -->
                                        <c:if test="${not empty orders}">
                                            <nav aria-label="Page navigation">
                                                <ul class="pagination justify-content-center">
                                                    <li class="page-item">
                                                        <a class="${1 eq currentPage ? 'disabled page-link' : 'page-link'}"
                                                            href="/admin/order?page=${currentPage - 1}"
                                                            aria-label="Previous">
                                                            <span aria-hidden="true"> &laquo; </span>
                                                        </a>
                                                    </li>

                                                    <c:forEach begin="0" end="${totalPages - 1}" varStatus="loop">
                                                        <li class="page-item">
                                                            <a class="${(loop.index + 1) eq currentPage ? 'active page-link' : 'page-link'}"
                                                                href="/admin/order?page=${loop.index + 1}">
                                                                ${loop.index + 1}
                                                            </a>
                                                        </li>
                                                    </c:forEach>

                                                    <li class="page-item">
                                                        <a class="${currentPage eq totalPages? 'disabled page-link' : 'page-link'}"
                                                            href="/admin/order?page=${currentPage + 1}"
                                                            aria-label="Next">
                                                            <span aria-hidden="true"> &raquo; </span>
                                                        </a>
                                                    </li>
                                                </ul>
                                            </nav>
                                        </c:if>
                                        <!-- Phân trang end -->
                                    </div>
                                </div>
                            </div>
                        </main>

                        <jsp:include page="../layout/footer.jsp" />
                    </div>
                </div>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                    crossorigin="anonymous"></script>
                <script src="js/scripts.js"></script>
            </body>

            </html>