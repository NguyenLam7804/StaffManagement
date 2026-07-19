<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${not empty staff and staff.staffId > 0 ? 'Edit Staff' : 'Add New Staff'}</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-5" style="max-width: 600px;">
        <!-- Tiêu đề Form thay đổi linh hoạt theo ID -->
        <h2 class="text-center mb-4">
            ${not empty staff and staff.staffId > 0 ? 'Edit Staff' : 'Add New Staff'}
        </h2>
        
        <!-- VÙNG HIỂN THỊ THÔNG BÁO LỖI (Ví dụ: Trùng Email từ Server gửi về) -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>Lỗi:</strong> ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <form action="staff-list" method="post">
            <!-- Đảm bảo luôn gửi staffId về, nếu thêm mới bị lỗi quay về thì value vẫn giữ là 0 -->
            <input type="hidden" name="staffId" value="${not empty staff ? staff.staffId : 0}" />

            <div class="mb-3">
                <label class="form-label">Full Name</label>
                <input type="text" name="fullName" class="form-control" value="${staff.fullName}" required>
            </div>

            <div class="mb-3">
                <label class="form-label d-block">Gender</label>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="gender" value="true" ${staff.gender == true ? 'checked' : ''} required>
                    <label class="form-check-label">Male</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="gender" value="false" ${staff.gender == false ? 'checked' : ''}>
                    <label class="form-check-label">Female</label>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label">Phone Number</label>
                <input type="text" name="phone" class="form-control" value="${staff.phone}" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control" value="${staff.email}" required>
            </div>

            <!-- CHỐT CHẶN PASSWORD: Chỉ hiện khi tạo mới (kể cả khi tạo mới lỗi quay về form nhờ check staffId == 0) -->
            <c:if test="${empty staff or staff.staffId == 0}">
                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <input type="password" name="password" class="form-control" required>
                </div>
            </c:if>

            <div class="mb-4">
                <label class="form-label">Role</label>
                <select name="roleId" class="form-select" required>
                    <option value="">-- Select Role --</option>
                    <c:forEach items="${roles}" var="role">
                        <option value="${role.roleID}" ${role.roleID == staff.roleId ? 'selected' : ''}>
                            ${role.roleName}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- Tên nút bấm thay đổi linh hoạt theo ID -->
            <button type="submit" class="btn btn-primary">
                ${not empty staff and staff.staffId > 0 ? 'Update' : 'Create'}
            </button>
            <a href="staff-list" class="btn btn-secondary">Cancel</a>
        </form>
    </div>
    
    <!-- Nhúng thêm JS Bootstrap để nút X tắt thông báo lỗi hoạt động mượt mà nếu cần -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>