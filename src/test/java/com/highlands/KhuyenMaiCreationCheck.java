package com.highlands;

import com.highlands.entity.*;
import com.highlands.repository.*;
import com.highlands.service.*;
import com.highlands.ui.KhuyenMaiDialogCheck;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Chỉ dùng H2 trong bộ nhớ; không kết nối hay thay đổi HighlandsDB đang sử dụng. */
public class KhuyenMaiCreationCheck {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "false");
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(KhachHangRegistrationCheck.TestConfig.class);
            ctx.registerBean(KhuyenMaiService.class);
            ctx.registerBean(BanHangService.class);
            ctx.registerBean(DatHangService.class);
            ctx.registerBean(ThanhToanService.class);
            ctx.refresh();
            var service = ctx.getBean(KhuyenMaiService.class);
            var repo = ctx.getBean(KhuyenMaiRepository.class);
            var menu = ctx.getBean(BanHangService.class); // Khởi tạo trước khi tạo bất kỳ mã nào.
            check(!menu.kiemTraKhuyenMai("NEW20", BigDecimal.valueOf(100000)).hopLe(), "Missing code");
            bad(service, k -> k.setMa(" "), "nhập mã");
            bad(service, k -> k.setTen(" "), "Tên khuyến mãi");
            bad(service, k -> k.setTen("x".repeat(101)), "100");
            bad(service, k -> k.setMa("GIẢM 20"), "Mã chỉ gồm");
            bad(service, k -> k.setMa("X".repeat(51)), "50");
            bad(service, k -> k.setLoai("INVALID"), "Loại giảm");
            for (double value : new double[]{0, -1, 101, Double.NaN, Double.POSITIVE_INFINITY})
                bad(service, k -> k.setPhanTramGiam(value), "Phần trăm");
            bad(service, k -> k.setDonToiThieu(-1L), "không được âm");
            bad(service, k -> k.setNgayBatDau(null), "chọn ngày");
            bad(service, k -> k.setNgayKetThuc(null), "chọn ngày");
            bad(service, k -> k.setNgayKetThuc(LocalDate.now().minusDays(1)), "Ngày kết thúc");
            bad(service, k -> { k.setLoai("GIAM_TIEN"); k.setSoTienGiam(0L); }, "lớn hơn 0");
            bad(service, k -> { k.setLoai("GIAM_TIEN"); k.setSoTienGiam(null); }, "lớn hơn 0");
            check(repo.count() == 0, "Invalid data persisted");

            var percentage = valid(" new20 ");
            percentage.setSoTienGiam(123L);
            percentage = service.them(percentage);
            check(percentage.getMa().equals("NEW20") && percentage.getSoTienGiam() == null, "Normalize percent");
            bad(service, k -> k.setMa("nEw20"), "đã tồn tại");
            check(menu.kiemTraKhuyenMai("new20", BigDecimal.valueOf(100000)).giam().longValueExact() == 20000,
                    "Existing sales service did not see new code immediately");
            var fixed = valid("FIX10K"); fixed.setLoai("GIAM_TIEN"); fixed.setSoTienGiam(10000L);
            fixed.setDonToiThieu(50000L); service.them(fixed);
            check(!menu.kiemTraKhuyenMai("FIX10K", BigDecimal.valueOf(49999)).hopLe(), "Minimum order");
            check(menu.kiemTraKhuyenMai("FIX10K", BigDecimal.valueOf(50000)).giam().longValueExact() == 10000,
                    "Fixed discount");
            var expired = valid("EXPIRED"); expired.setNgayBatDau(LocalDate.now().minusDays(2));
            expired.setNgayKetThuc(LocalDate.now().minusDays(1)); service.them(expired);
            check(!menu.kiemTraKhuyenMai("EXPIRED", BigDecimal.valueOf(100000)).hopLe(), "Expired code");
            var future = valid("FUTURE"); future.setNgayBatDau(LocalDate.now().plusDays(1));
            future.setNgayKetThuc(LocalDate.now().plusDays(2)); service.them(future);
            check(!menu.kiemTraKhuyenMai("FUTURE", BigDecimal.valueOf(100000)).hopLe(), "Future code");

            var cat = new DanhMuc(); cat.setTen("Test");
            cat = ctx.getBean(DanhMucRepository.class).save(cat);
            var product = new SanPham(); product.setTen("Coffee"); product.setGia(100000); product.setDanhMuc(cat);
            product = ctx.getBean(SanPhamRepository.class).save(product);
            var staff = new NhanVien(); staff.setMaNhanVien("PROMOTEST"); staff.setHoTen("Test");
            staff = ctx.getBean(NhanVienRepository.class).save(staff);
            var customer = new KhachHang(); customer.setHoTen("Test"); customer.setTenDangNhap("promo.test");
            customer.setSoDienThoai("0912345678"); customer.setMatKhauHash("fixture");
            customer = ctx.getBean(KhachHangRepository.class).save(customer);
            var cart = List.of(new DongGio(product, List.of(), 1));
            var receipt = ctx.getBean(ThanhToanService.class).thanhToan(staff.getId(), cart, "new20",
                    BigDecimal.valueOf(100000), "promo-counter");
            check(receipt.giamGia().longValueExact() == 20000, "Staff checkout with new code");
            var order = ctx.getBean(DatHangService.class).datHang(customer.getId(), cart, "fix10k",
                    DatHangService.TIEN_MAT, new DatHangService.GiaoHang("0912345678", "Test address", ""), "promo-customer");
            check(order.giamGia().longValueExact() == 10000, "Customer checkout with new code");
            KhuyenMaiDialogCheck.verify(service);
            System.out.println("PASS: promotion validation, duplicate/uppercase, date/minimum rules, immediate staff and customer checkout, Swing dialog and refresh");
        }
    }

    private static KhuyenMai valid(String code) {
        var k = new KhuyenMai(); k.setTen("Khuyến mãi thử nghiệm"); k.setMa(code);
        k.setLoai("PHAN_TRAM"); k.setPhanTramGiam(20);
        k.setNgayBatDau(LocalDate.now()); k.setNgayKetThuc(LocalDate.now()); return k;
    }
    private static void bad(KhuyenMaiService service, java.util.function.Consumer<KhuyenMai> edit, String error) {
        var k = valid("INVALID"); edit.accept(k);
        try { service.them(k); throw new AssertionError("Accepted invalid input: " + error); }
        catch (LoiNghiepVu e) { check(e.getMessage().contains(error), e.getMessage()); }
    }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
