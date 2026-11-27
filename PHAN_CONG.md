# Phân công source

Giữ nguyên package Java và một pom.xml. Entity, repository, SQL, đăng nhập nội bộ, giỏ hàng, thanh toán, UI dùng chung thuộc nền chung. Không sao chép các file này sang nhiều nhánh.

## quan-ly

- `src/main/java/com/highlands/service/DanhMucService.java`
- `src/main/java/com/highlands/service/KhuyenMaiService.java`
- `src/main/java/com/highlands/service/SanPhamService.java`
- `src/main/java/com/highlands/service/ThongKeService.java`
- `src/main/java/com/highlands/service/ToppingService.java`
- `src/main/java/com/highlands/ui/DichVu.java`
- `src/main/java/com/highlands/ui/FormChinh.java`
- `src/main/java/com/highlands/ui/HopThoaiKhuyenMai.java`
- `src/main/java/com/highlands/ui/HopThoaiMuc.java`
- `src/main/java/com/highlands/ui/TrangKhuyenMai.java`
- `src/main/java/com/highlands/ui/TrangSanPham.java`
- `src/main/java/com/highlands/ui/TrangThongKe.java`

## nhan-vien

- `src/main/java/com/highlands/service/DonOnlineService.java`
- `src/main/java/com/highlands/ui/FormNhanVien.java`
- `src/main/java/com/highlands/ui/TrangBanHang.java`
- `src/main/java/com/highlands/ui/TrangDonOnline.java`
- `src/main/java/com/highlands/ui/TrangLichSuDonHang.java`
- `src/main/java/com/highlands/ui/TrangThanhToan.java`

## khach-hang

- `src/main/java/com/highlands/service/DanhGiaPhanHoiService.java`
- `src/main/java/com/highlands/service/DatHangService.java`
- `src/main/java/com/highlands/service/KhachHangService.java`
- `src/main/java/com/highlands/ui/DangNhapKhachHang.java`
- `src/main/java/com/highlands/ui/FormKhachHang.java`
- `src/main/java/com/highlands/ui/HopThoaiThanhToanKhach.java`
- `src/main/java/com/highlands/ui/TrangDanhGiaPhanHoi.java`
- `src/main/java/com/highlands/ui/TrangDatHang.java`
- `src/main/java/com/highlands/ui/TrangDonCuaToi.java`
- `src/main/java/com/highlands/ui/TrangThongTinCaNhan.java`

## Lưu ý

Main ban đầu chỉ là nền chung, chưa build được cho tới khi merge đủ ba nhánh. Sau khi tích hợp, cả ba thành viên tạo nhánh mới từ main đầy đủ. SQL nháp và mẫu được giữ để tham khảo, không chạy tự động. Database hiện hữu HighlandsDB là điều kiện bắt buộc; script.sql không tạo đầy đủ schema.
