<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="utf-8" />
                    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
                    <meta name="description" content="Dự án laptopshop" />
                    <meta name="author" content="Hỏi Dân IT" />
                    <title> Update Order </title>
                    <link href="/css/styles.css" rel="stylesheet" />

                    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
                    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"
                        crossorigin="anonymous"></script>
                </head>

                <body class="sb-nav-fixed">
                    <jsp:include page="../layout/header.jsp" />
                    <div id="layoutSidenav">
                        <jsp:include page="../layout/sidebar.jsp" />
                        <div id="layoutSidenav_content">
                            <main>
                                <div class="container-fluid px-4">
                                    <h1 class="mt-4"> Update User </h1>

                                    <ol class="breadcrumb mb-4">
                                        <li class="breadcrumb-item active"> <a href="/admin"> Dashboard </a> </li>
                                        <li class="breadcrumb-item active"> <a href="/admin/order"> Order </a> </li>
                                        <li class="breadcrumb-item active"> Update Order </li>
                                    </ol>

                                    <div class="container mt-5">
                                        <div class="row">
                                            <div class="col-md-6 col-12 mx-auto">
                                                <h3> Update order </h3>
                                                <hr />

                                                <form:form method="post" action="/admin/order/update"
                                                    modelAttribute="newOrder">

                                                    <input type="hidden" name="${_csrf.parameterName}"
                                                        value="${_csrf.token}" />

                                                    <div class="mb-3">
                                                        <label class="form-label"> ID: </label>
                                                        <form:input type="text" class="form-control" path="id"
                                                            readonly="true" />
                                                    </div>

                                                    <div class="mb-3">
                                                        <label class="form-label"> Price: </label>
                                                        <p class="form-control-plaintext">
                                                            <fmt:formatNumber value="${newOrder.totalPrice}" />
                                                        </p>
                                                    </div>

                                                    <div class="mb-3">
                                                        <label class="form-label"> Name: </label>
                                                        <form:input type="text" class="form-control" path="receiverName"
                                                            disabled="true" />
                                                    </div>

                                                    <div class="mb-3 col-12 col-md-6">
                                                        <label class="form-label"> Status: </label>
                                                        <form:select class="form-select" path="status">
                                                            <form:option value="DELIVERED"> DELIVERED </form:option>
                                                            <form:option value="PENDING"> PENDING </form:option>
                                                            <form:option value="PROCESSING"> PROCESSING </form:option>
                                                            <form:option value="COMPLETE"> COMPLETE </form:option>
                                                            <form:option value="CANCELLED"> CANCELLED </form:option>
                                                        </form:select>
                                                    </div>

                                                    <button type="submit" class="btn btn-warning"> Update </button>
                                                </form:form>
                                            </div>
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