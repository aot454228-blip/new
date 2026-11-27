package com.highlands.ui;

import com.highlands.entity.KhuyenMai;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

// Hộp thoại thêm / sửa khuyến mãi
public class HopThoaiKhuyenMai extends HopThoai {

    private static final String[] LOAI = {"PHAN_TRAM", "GIAM_TIEN"};

    private final DichVu dv;
    private final KhuyenMai cu; // null = thêm mới

    private final ONhap ten = oChu("VD: Giảm giá khai trương", 100);
    private final ONhap ma = oChu("VD: HIGHLANDS20", 50);
    private final JComboBox<String> loai = new JComboBox<>(new String[]{"Giảm theo phần trăm", "Giảm số tiền cố định"});
    private final ONhap phanTram = oChu("VD: 20", 10);
    private final ONhap soTien = oChu("VD: 10000", 15);
    private final ONhap toiThieu = oChu("Để trống = không yêu cầu", 15);
    private final BoChonNgay batDau = new BoChonNgay();
    private final BoChonNgay ketThuc = new BoChonNgay();
    private final ONhap anh = oChu("/images/ten-anh.jpg", 500);
    private final Anh.XemTruoc xemTruoc = new Anh.XemTruoc(96);
    private File anhDaChon;
    private final CardLayout theGiam = new CardLayout();
    private final JPanel oGiam = new JPanel(theGiam);
    private final JLabel nhanGiam = Theme.nhan("");

    public HopThoaiKhuyenMai(Window cha, DichVu dv, KhuyenMai k) {
        super(cha, k == null ? "Thêm mã giảm giá" : "Sửa khuyến mãi (ID " + k.getId() + ")",
                k == null ? "Tạo mã giảm giá" : "Lưu thay đổi", 2);
        this.dv = dv;
        this.cu = k;
        luoi.setPreferredSize(new Dimension(650, 440));
        ((AbstractDocument) ma.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int off, int len, String text, AttributeSet a)
                    throws BadLocationException {
                String chu = text == null ? "" : text.toUpperCase(Locale.ROOT);
                if (fb.getDocument().getLength() - len + chu.length() <= 50)
                    super.replace(fb, off, len, chu, a);
                else baoLoi("Mã giảm giá tối đa 50 ký tự");
            }
            @Override public void insertString(FilterBypass fb, int off, String text, AttributeSet a)
                    throws BadLocationException { replace(fb, off, 0, text, a); }
        });

        Theme.kieuCombo(loai);
        oGiam.setOpaque(false);
        oGiam.add(phanTram, LOAI[0]);
        oGiam.add(soTien, LOAI[1]);
        loai.addActionListener(e -> doiLoai());

        JLabel huongDan = Theme.nhan("Các trường có * là bắt buộc. Mã được tự động viết IN HOA.");
        huongDan.setForeground(Theme.BRAND);
        themO("", huongDan, true);
        themO("Tên khuyến mãi *", ten);
        themO(k == null ? "Mã giảm giá *" : "Mã giảm giá", ma);
        themO("Loại giảm", loai);
        // Nhãn của ô giảm đổi theo loại nên tự dựng thay vì dùng themO
        JPanel pGiam = new JPanel(new BorderLayout(0, 4));
        pGiam.setOpaque(false);
        pGiam.add(nhanGiam, BorderLayout.NORTH);
        pGiam.add(oGiam, BorderLayout.CENTER);
        themO("", pGiam);
        themO("Đơn hàng tối thiểu (VND, trống = 0)", toiThieu, true);
        themO(k == null ? "Ngày bắt đầu *" : "Ngày bắt đầu", batDau);
        themO(k == null ? "Ngày kết thúc *" : "Ngày kết thúc", ketThuc);

        JPanel pAnh = new JPanel(new BorderLayout(14, 0));
        pAnh.setOpaque(false);
        pAnh.add(xemTruoc, BorderLayout.WEST);
        JPanel chonAnh = new JPanel(new BorderLayout(0, 8));
        chonAnh.setOpaque(false);
        anh.setEditable(k != null);
        anh.setGoiY("Chưa chọn ảnh (không bắt buộc)");
        chonAnh.add(anh, BorderLayout.NORTH);
        JPanel nutAnh = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nutAnh.setOpaque(false);
        Nut chon = new Nut("Chọn ảnh...", Nut.Kieu.THUONG);
        chon.addActionListener(e -> chonAnh());
        Nut bo = new Nut("Bỏ ảnh", Nut.Kieu.THUONG);
        bo.addActionListener(e -> { anhDaChon = null; anh.setText(""); });
        nutAnh.add(chon);
        nutAnh.add(bo);
        chonAnh.add(nutAnh, BorderLayout.CENTER);
        chonAnh.add(Theme.nhan("PNG, JPG, GIF, BMP · tối đa 10 MB"), BorderLayout.SOUTH);
        pAnh.add(chonAnh, BorderLayout.CENTER);
        themO("Hình ảnh", pAnh, true);
        anh.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { xemTruoc.setUrl(anh.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { xemTruoc.setUrl(anh.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { xemTruoc.setUrl(anh.getText().trim()); }
        });

        if (k != null) {
            ten.setText(k.getTen());
            ma.setText(k.getMa() == null ? "" : k.getMa());
            loai.setSelectedIndex("GIAM_TIEN".equals(k.getLoai()) ? 1 : 0);
            if (!"GIAM_TIEN".equals(k.getLoai())) {
                double p = k.getPhanTramGiam();
                phanTram.setText(p == Math.floor(p) ? String.valueOf((long) p) : String.valueOf(p));
            }
            soTien.setText(k.getSoTienGiam() == null ? "" : String.valueOf(k.getSoTienGiam()));
            toiThieu.setText(k.getDonToiThieu() == null ? "" : String.valueOf(k.getDonToiThieu()));
            batDau.datNgay(k.getNgayBatDau());
            ketThuc.datNgay(k.getNgayKetThuc());
            anh.setText(k.getHinhAnh() == null ? "" : k.getHinhAnh());
        } else {
            batDau.datNgay(LocalDate.now());
            ketThuc.datNgay(LocalDate.now().plusMonths(1));
        }
        doiLoai();
        xemTruoc.setUrl(anh.getText().trim());
    }

    // Chọn loại giảm nào thì hiện ô nhập tương ứng
    private void doiLoai() {
        boolean laTien = loai.getSelectedIndex() == 1;
        theGiam.show(oGiam, LOAI[laTien ? 1 : 0]);
        nhanGiam.setText(laTien ? "Số tiền giảm (VND) *" : "Phần trăm giảm (0 < giá trị ≤ 100) *");
    }

    @Override
    protected Callable<Object> taoViecLuu() {
        boolean laTien = loai.getSelectedIndex() == 1;
        if (ten.getText().isBlank()) throw new LoiNhap("Vui lòng nhập tên khuyến mãi");
        if (cu == null && ma.getText().isBlank()) throw new LoiNhap("Vui lòng nhập mã giảm giá");
        KhuyenMai k = new KhuyenMai();
        k.setTen(ten.getText().trim());
        k.setMa(ma.getText().trim().isEmpty() ? null : ma.getText().trim());
        k.setLoai(LOAI[laTien ? 1 : 0]);
        if (laTien) {
            Long t = docTienKhuyenMai(soTien, "Số tiền giảm");
            if (t == null) throw new LoiNhap("Vui lòng nhập số tiền giảm");
            k.setSoTienGiam(t);
            k.setPhanTramGiam(0);
        } else {
            Double p = docSoThuc(phanTram, "Phần trăm giảm");
            if (p == null) throw new LoiNhap("Vui lòng nhập phần trăm giảm");
            k.setPhanTramGiam(p);
        }
        k.setDonToiThieu(docTienKhuyenMai(toiThieu, "Đơn tối thiểu"));
        k.setNgayBatDau(batDau.ngay);
        k.setNgayKetThuc(ketThuc.ngay);
        k.setHinhAnh(anh.getText().trim().isEmpty() ? null : anh.getText().trim());

        // Kiểm tra đầy đủ (trống tên, trùng mã, ngày...) nằm ở KhuyenMaiService
        File tep = anhDaChon;
        return () -> {
            Path banSao = null;
            try {
                if (tep != null) {
                    if (!tep.isFile() || tep.length() > 10 * 1024 * 1024 || ImageIO.read(tep) == null)
                        throw new LoiNhap("Ảnh không còn hợp lệ. Vui lòng chọn lại ảnh (tối đa 10 MB).");
                    Path thuMuc = Path.of("uploads", "khuyen-mai").toAbsolutePath();
                    Files.createDirectories(thuMuc);
                    // ImageIO nhận diện nội dung nên không phụ thuộc tên/đuôi tệp gốc.
                    banSao = thuMuc.resolve(UUID.randomUUID() + ".img");
                    Files.copy(tep.toPath(), banSao);
                    k.setHinhAnh(banSao.toString());
                }
                return cu == null ? dv.khuyenMai().them(k) : dv.khuyenMai().sua(cu.getId(), k);
            } catch (Exception e) {
                if (banSao != null) {
                    try { Files.deleteIfExists(banSao); } catch (java.io.IOException ignored) { }
                }
                if (e instanceof org.springframework.dao.ConcurrencyFailureException)
                    throw new LoiNhap("Có người vừa tạo mã cùng lúc. Vui lòng bấm lưu lại để kiểm tra mã trùng.");
                if (e instanceof java.io.IOException)
                    throw new LoiNhap("Không thể đọc hoặc lưu ảnh. Vui lòng chọn lại ảnh và kiểm tra quyền ghi thư mục uploads.");
                throw e;
            }
        };
    }

    private Long docTienKhuyenMai(JTextField o, String tenO) {
        if (cu != null) return docTien(o, tenO);
        String chu = o.getText().trim();
        if (chu.isEmpty()) return null;
        if (!chu.matches("[0-9]+|[0-9]{1,3}([., ])[0-9]{3}(\\1[0-9]{3})*"))
            throw new LoiNhap(tenO + " phải là số nguyên VND (VD: 25000 hoặc 25.000)");
        return docTien(o, tenO);
    }

    private void chonAnh() {
        JFileChooser chon = new JFileChooser();
        chon.setDialogTitle("Chọn ảnh khuyến mãi");
        chon.setAcceptAllFileFilterUsed(false);
        chon.setFileFilter(new FileNameExtensionFilter("Ảnh (PNG, JPG, GIF, BMP)", "png", "jpg", "jpeg", "gif", "bmp"));
        if (chon.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File tep = chon.getSelectedFile();
        try {
            if (!tep.isFile() || tep.length() > 10 * 1024 * 1024 || ImageIO.read(tep) == null) {
                baoLoi("Vui lòng chọn tệp ảnh hợp lệ, dung lượng tối đa 10 MB.");
                return;
            }
            anhDaChon = tep;
            anh.setText(tep.getAbsolutePath());
        } catch (java.io.IOException e) {
            baoLoi("Không đọc được ảnh. Vui lòng chọn tệp ảnh khác.");
        }
    }

    /** Lịch Swing thuần: chọn ngày bằng chuột, không phân tích chuỗi ngày nhập tay. */
    static class BoChonNgay extends JPanel {
        private LocalDate ngay;
        private final Nut nut = new Nut("Chọn ngày...", Nut.Kieu.THUONG);

        BoChonNgay() {
            super(new BorderLayout());
            setOpaque(false);
            add(nut);
            nut.setHorizontalAlignment(SwingConstants.LEFT);
            nut.addActionListener(e -> moLich());
        }

        void datNgay(LocalDate ngay) {
            this.ngay = ngay;
            nut.setText(ngay == null ? "Chọn ngày..." : chuNgay(ngay) + "   (chọn ngày)");
        }

        private void moLich() {
            JPopupMenu lich = new JPopupMenu();
            JPanel than = new JPanel(new BorderLayout(4, 8));
            than.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            LocalDate moc = ngay == null ? LocalDate.now() : ngay;
            JSpinner thang = new JSpinner(new SpinnerNumberModel(moc.getMonthValue(), 1, 12, 1));
            JSpinner nam = new JSpinner(new SpinnerNumberModel(moc.getYear(), 1, 9999, 1));
            nam.setEditor(new JSpinner.NumberEditor(nam, "#"));
            JPanel dau = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            dau.add(Theme.nhan("Tháng")); dau.add(thang);
            dau.add(Theme.nhan("Năm")); dau.add(nam);
            than.add(dau, BorderLayout.NORTH);
            JPanel ngayTrongThang = new JPanel(new GridLayout(0, 7, 3, 3));
            Runnable ve = () -> {
                ngayTrongThang.removeAll();
                for (String thu : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"})
                    ngayTrongThang.add(new JLabel(thu, SwingConstants.CENTER));
                YearMonth ym = YearMonth.of((Integer) nam.getValue(), (Integer) thang.getValue());
                for (int i = 1; i < ym.atDay(1).getDayOfWeek().getValue(); i++)
                    ngayTrongThang.add(new JLabel());
                for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                    LocalDate chon = ym.atDay(d);
                    JButton n = new JButton(String.valueOf(d));
                    n.setMargin(new Insets(5, 7, 5, 7));
                    n.setForeground(Theme.BRAND);
                    if (chon.equals(ngay)) n.setBackground(Theme.CHON);
                    n.addActionListener(e -> { datNgay(chon); lich.setVisible(false); });
                    ngayTrongThang.add(n);
                }
                ngayTrongThang.revalidate(); ngayTrongThang.repaint();
                lich.pack();
            };
            thang.addChangeListener(e -> ve.run()); nam.addChangeListener(e -> ve.run());
            than.add(ngayTrongThang, BorderLayout.CENTER);
            JButton homNay = new JButton("Hôm nay");
            homNay.addActionListener(e -> { datNgay(LocalDate.now()); lich.setVisible(false); });
            than.add(homNay, BorderLayout.SOUTH);
            lich.add(than);
            ve.run();
            lich.show(nut, 0, nut.getHeight());
        }
    }
}
