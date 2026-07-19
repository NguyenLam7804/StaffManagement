package fu.swt301.sms.servlet;

import fu.swt301.sms.entity.Staff;
import fu.swt301.sms.service.StaffService;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@WebServlet(name = "ExportExcelServlet", urlPatterns = {"/export-excel"})
public class ExportExcelServlet extends HttpServlet {
    
    // Gọi tầng Service để lấy dữ liệu đúng chuẩn 3-tier
    private final StaffService staffService = new StaffService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Thiết lập kiểu nội dung trả về là file Excel (.xlsx)
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=staff_list.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Staff List");

            // 1. Tạo hàng tiêu đề (Header Row) khớp với các thuộc tính của lớp Staff
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Full Name", "Email", "Phone", "Role ID"};
            
            // Định dạng chữ in đậm (Bold) cho Header của file Excel
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 2. Lấy dữ liệu nhân viên từ tầng Service (Hết lỗi đỏ biến list)
            List<Staff> list = staffService.getAllStaff();

            // 3. Ghi toàn bộ dữ liệu nhân viên vào file Excel (Đã sửa đúng tên các hàm getter)
            int rowIdx = 1;
            for (Staff staff : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(staff.getStaffId());
                row.createCell(1).setCellValue(staff.getFullName());
                row.createCell(2).setCellValue(staff.getEmail());
                row.createCell(3).setCellValue(staff.getPhone());
                row.createCell(4).setCellValue(staff.getRoleId());
            }

            // 4. Đẩy dữ liệu workbook ra luồng xuất của hệ thống phản hồi để tải về trình duyệt
            workbook.write(response.getOutputStream());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}