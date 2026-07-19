<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Staff Management Dashboard</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    
    <!-- Nhúng SweetAlert2 để làm Popup Xóa hiện đại -->
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

    <style>
        .stat-card { transition: all 0.3s; border: none; }
        .stat-card:hover { transform: translateY(-5px); box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        th a { color: white; text-decoration: none; }
        th a:hover { color: #ffc107; text-decoration: underline; }
    </style>
</head>
<body class="bg-light">
<div class="container mt-4 mb-5">

    <!-- Dashboard Stats -->
    <div class="row mb-4">
        <div class="col-md-4">
            <div class="card stat-card bg-primary text-white text-center p-3 rounded shadow-sm">
                <div class="card-body">
                    <h5 class="card-title"><i class="fas fa-users fa-2x mb-2"></i></h5>
                    <h4>Total Employees</h4>
                    <h2 class="font-weight-bold">${totalStaffCount != null ? totalStaffCount : 0}</h2>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card stat-card bg-warning text-dark text-center p-3 rounded shadow-sm">
                <div class="card-body">
                    <h5 class="card-title"><i class="fas fa-user-shield fa-2x mb-2"></i></h5>
                    <h4>Total Admins</h4>
                    <h2 class="font-weight-bold">${totalAdminCount != null ? totalAdminCount : 0}</h2>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card stat-card bg-success text-white text-center p-3 rounded shadow-sm">
                <div class="card-body">
                    <h5 class="card-title"><i class="fas fa-user-tie fa-2x mb-2"></i></h5>
                    <h4>Total Staffs</h4>
                    <h2 class="font-weight-bold">${totalUserCount != null ? totalUserCount : 0}</h2>
                </div>
            </div>
        </div>
    </div>

    <!-- Search & Filter Bar -->
    <div class="card shadow-sm border-0 rounded p-4 mb-4 bg-white">
        <form action="staff-list" method="get" class="row g-3 align-items-center">
            <input type="hidden" name="action" value="list">
            <div class="col-md-5">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <span class="input-group-text bg-white border-right-0"><i class="text-muted fas fa-search"></i></span>
                    </div>
                    <input type="text" class="form-control border-left-0" name="search" value="${param.search}" placeholder="Search by name or email...">
                </div>
            </div>
            <div class="col-md-3">
                <select class="form-control" name="roleFilter">
                    <option value="">-- All Roles --</option>
                    <option value="1" ${param.roleFilter == '1' ? 'selected' : ''}>Admin</option>
                    <option value="2" ${param.roleFilter == '2' ? 'selected' : ''}>Staff</option>
                </select>
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary btn-block font-weight-bold">Filter</button>
            </div>
            <div class="col-md-2 text-right">
                <a href="export-excel" class="btn btn-info btn-sm text-white mb-1"><i class="fas fa-file-excel"></i> Excel</a>
                <a href="staff-list?action=new" class="btn btn-success">+ Add New</a>
            </div>
        </form>
    </div>

    <!-- Data Table -->
    <div class="card shadow-sm border-0 rounded bg-white">
        <div class="table-responsive">
            <table class="table table-hover text-center mb-0">
                <thead class="thead-dark">
                    <tr>
                        <th><a href="staff-list?action=list&search=${param.search}&roleFilter=${param.roleFilter}&sort=StaffID&order=${param.order == 'asc' ? 'desc' : 'asc'}">ID <i class="fas fa-sort"></i></a></th>
                        <th class="text-left"><a href="staff-list?action=list&search=${param.search}&roleFilter=${param.roleFilter}&sort=FullName&order=${param.order == 'asc' ? 'desc' : 'asc'}">Full Name <i class="fas fa-sort"></i></a></th>
                        <th>Gender</th>
                        <th>Phone Number</th>
                        <th class="text-left">Email</th>
                        <th>Role</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty staffList}">
                            <c:forEach var="staff" items="${staffList}">
                                <tr>
                                    <td>${staff.staffId}</td>
                                    <td class="text-left font-weight-bold">${staff.fullName}</td>
                                    <td>
                                        <span class="badge ${staff.gender ? 'badge-primary' : 'badge-danger'}">
                                            ${staff.gender ? 'Male' : 'Female'}
                                        </span>
                                    </td>
                                    <td>${staff.phone}</td>
                                    <td class="text-left">${staff.email}</td>
                                    <td>
                                        <span class="badge ${staff.roleId == 1 ? 'badge-warning' : 'badge-secondary'}">
                                            ${staff.roleId == 1 ? 'Admin' : 'Staff'}
                                        </span>
                                    </td>
                                    <td>
                                        <a href="staff-list?action=edit&id=${staff.staffId}" class="btn btn-sm btn-light border mr-1 text-warning"><i class="fas fa-edit"></i></a>
                                        <!-- Thay thế confirm bằng hàm confirmDelete Custom -->
                                        <a href="#" class="btn btn-sm btn-light border text-danger" onclick="confirmDelete(event, ${staff.staffId})"><i class="fas fa-trash"></i></a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="7" class="text-muted py-4">No records found.</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
        
        <!-- Pagination -->
        <div class="card-footer bg-white d-flex justify-content-between align-items-center border-top-0 py-3">
            <div class="text-muted small">
                Showing page <b>${currentPage}</b> of <b>${totalPages}</b> pages
            </div>
            <nav aria-label="Page navigation">
                <ul class="pagination mb-0">
                    <c:if test="${currentPage > 1}">
                        <li class="page-item"><a class="page-link" href="staff-list?action=list&page=${currentPage - 1}&search=${param.search}&roleFilter=${param.roleFilter}&sort=${param.sort}&order=${param.order}">Previous</a></li>
                    </c:if>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <li class="page-item ${currentPage == i ? 'active' : ''}">
                            <a class="page-link" href="staff-list?action=list&page=${i}&search=${param.search}&roleFilter=${param.roleFilter}&sort=${param.sort}&order=${param.order}">${i}</a>
                        </li>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages}">
                        <li class="page-item"><a class="page-link" href="staff-list?action=list&page=${currentPage + 1}&search=${param.search}&roleFilter=${param.roleFilter}&sort=${param.sort}&order=${param.order}">Next</a></li>
                    </c:if>
                </ul>
            </nav>
        </div>
    </div>
</div>

<!-- Script điều khiển Popup SweetAlert2 -->
<script>
function confirmDelete(event, staffId) {
    event.preventDefault(); // Ngăn trình duyệt tự động chuyển hướng link '#'
    
    Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545', // Màu đỏ của Bootstrap nút Danger
        cancelButtonColor: '#6c757d',  // Màu xám Secondary
        confirmButtonText: 'Yes, delete it!',
        cancelButtonText: 'Cancel'
    }).then((result) => {
        if (result.isConfirmed) {
            // Thực hiện chuyển hướng đến Servlet thực hiện tác vụ xóa khi ấn OK
            window.location.href = 'staff-list?action=delete&id=' + staffId;
        }
    });
}
</script>
</body>
</html>