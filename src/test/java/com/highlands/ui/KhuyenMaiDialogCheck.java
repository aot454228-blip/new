package com.highlands.ui;

import com.highlands.entity.KhuyenMai;
import com.highlands.service.KhuyenMaiService;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/** Kiểm thử trên EDT, dùng hộp thoại và nút Lưu thật với cơ sở dữ liệu H2. */
public class KhuyenMaiDialogCheck {
    public static void verify(KhuyenMaiService service) throws Exception {
        DichVu dv = new DichVu(service, null, null, null, null);
        Path source = Path.of("target", "promotion-input.png").toAbsolutePath();
        var sample = new BufferedImage(160, 100, BufferedImage.TYPE_INT_RGB);
        var draw = sample.createGraphics(); draw.setColor(new Color(0xB22830));
        draw.fillRect(0, 0, 160, 100); draw.dispose(); ImageIO.write(sample, "png", source.toFile());
        AtomicReference<Path> copied = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                Theme.caiDat();
                HopThoaiKhuyenMai dialog = new HopThoaiKhuyenMai(null, dv, null);
                try {
                    ((ONhap) get(dialog, "ten")).setText("Ưu đãi Highlands");
                    try { dialog.taoViecLuu(); throw new AssertionError("Blank code accepted"); }
                    catch (HopThoai.LoiNhap expected) { check(expected.getMessage().contains("mã"), "Blank code error"); }
                    ((ONhap) get(dialog, "ma")).setText("ui20");
                    check(((ONhap) get(dialog, "ma")).getText().equals("UI20"), "Live uppercase");
                    var type = (JComboBox<?>) get(dialog, "loai");
                    type.setSelectedIndex(1);
                    check(((ONhap) get(dialog, "soTien")).isVisible() && !((ONhap) get(dialog, "phanTram")).isVisible(), "Fixed card");
                    type.setSelectedIndex(0);
                    check(((ONhap) get(dialog, "phanTram")).isVisible() && !((ONhap) get(dialog, "soTien")).isVisible(), "Percent card");
                    ((ONhap) get(dialog, "phanTram")).setText("20");
                    ((JComboBox<?>) get(dialog, "loai")).setSelectedIndex(1);
                    ((ONhap) get(dialog, "soTien")).setText("1.5");
                    try { dialog.taoViecLuu(); throw new AssertionError("Fractional VND accepted"); }
                    catch (HopThoai.LoiNhap expected) { check(expected.getMessage().contains("số nguyên"), "Money error"); }
                    ((ONhap) get(dialog, "soTien")).setText("25.000");
                    dialog.taoViecLuu();
                    ((JComboBox<?>) get(dialog, "loai")).setSelectedIndex(0);
                    ((HopThoaiKhuyenMai.BoChonNgay) get(dialog, "batDau")).datNgay(LocalDate.now());
                    ((HopThoaiKhuyenMai.BoChonNgay) get(dialog, "ketThuc")).datNgay(LocalDate.now());
                    set(dialog, "anhDaChon", source.toFile());
                    ((ONhap) get(dialog, "anh")).setText(source.toString());
                    Anh.taiDongBo(source.toString());
                    dialog.pack(); dialog.validate();
                    var content = dialog.getContentPane();
                    var image = new BufferedImage(content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
                    var g = image.createGraphics(); content.printAll(g); g.dispose();
                    ImageIO.write(image, "png", Path.of("target", "promotion-dialog.png").toFile());
                    var created = (KhuyenMai) dialog.taoViecLuu().call();
                    copied.set(Path.of(created.getHinhAnh()));
                    check(Files.isRegularFile(copied.get()) && !copied.get().equals(source), "Image copy persisted");
                    try { dialog.taoViecLuu().call(); throw new AssertionError("Duplicate accepted"); }
                    catch (com.highlands.service.LoiNghiepVu expected) { /* keep dialog open and original image */ }
                    check(Files.isRegularFile(copied.get()), "Duplicate removed previous image");
                } catch (Exception e) { throw new RuntimeException(e); }
                finally { dialog.dispose(); }
            });

            TrangKhuyenMai[] panel = new TrangKhuyenMai[1];
            AtomicReference<Throwable> failure = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> {
                panel[0] = new TrangKhuyenMai(dv);
                ((ONhap) get(panel[0], "oTim")).setText("FILTER_WITH_NO_MATCH");
                Timer fill = new Timer(100, null);
                fill.addActionListener(e -> {
                    for (Window window : Window.getWindows()) {
                        if (window instanceof HopThoaiKhuyenMai d && d.isShowing()) {
                            fill.stop();
                            try {
                                // Mở lịch thật, chọn ngày nhuận rồi trả về hôm nay để thử Lưu.
                                var date = (HopThoaiKhuyenMai.BoChonNgay) get(d, "batDau");
                                ((JButton) date.getComponent(0)).doClick();
                                var menu = MenuSelectionManager.defaultManager().getSelectedPath();
                                JPopupMenu popup = (JPopupMenu) menu[0];
                                JPanel calendar = (JPanel) popup.getComponent(0);
                                JPanel header = (JPanel) calendar.getComponent(0);
                                ((JSpinner) header.getComponent(1)).setValue(2);
                                ((JSpinner) header.getComponent(3)).setValue(2028);
                                JPanel days = (JPanel) calendar.getComponent(1);
                                boolean leap = false;
                                for (Component c : days.getComponents()) {
                                    if (c instanceof JButton b && b.getText().equals("29")) {
                                        b.doClick(); leap = true; break;
                                    }
                                }
                                check(leap && get(date, "ngay").equals(LocalDate.of(2028, 2, 29)), "Calendar leap date");
                                date.datNgay(LocalDate.now());
                                ((ONhap) get(d, "ten")).setText("Tạo từ nút Lưu");
                                ((ONhap) get(d, "ma")).setText("refresh10");
                                ((ONhap) get(d, "phanTram")).setText("10");
                                d.getRootPane().getDefaultButton().doClick();
                            } catch (Throwable ex) { failure.set(ex); d.dispose(); }
                        }
                    }
                });
                fill.start();
                Timer timeout = new Timer(15000, e -> {
                    failure.compareAndSet(null, new AssertionError("Save dialog timed out"));
                    for (Window w : Window.getWindows()) if (w instanceof HopThoaiKhuyenMai) w.dispose();
                });
                timeout.setRepeats(false); timeout.start();
                try {
                    var open = TrangKhuyenMai.class.getDeclaredMethod("moForm", KhuyenMai.class);
                    open.setAccessible(true); open.invoke(panel[0], new Object[]{null});
                } catch (Exception e) { failure.set(e); }
                finally { fill.stop(); timeout.stop(); }
            });
            if (failure.get() != null) throw new AssertionError(failure.get());
            boolean[] found = {false};
            for (int i = 0; i < 100 && !found[0]; i++) {
                SwingUtilities.invokeAndWait(() -> {
                    check(((ONhap) get(panel[0], "oTim")).getText().isEmpty(), "Old search still hides new code");
                    JTable table = ((Bang<?>) get(panel[0], "bang")).bang();
                    for (int row = 0; row < table.getRowCount(); row++)
                        if ("REFRESH10".equals(table.getValueAt(row, 2))) found[0] = true;
                });
                if (!found[0]) Thread.sleep(50);
            }
            check(found[0], "New code missing from refreshed list");
            System.out.println("PASS: real Swing save, inline validation, uppercase, discount cards, image copy/rollback, list refresh; target/promotion-dialog.png");
        } finally {
            Files.deleteIfExists(source);
            if (copied.get() != null) Files.deleteIfExists(copied.get());
        }
    }
    private static Object get(Object target, String name) {
        try { Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    private static void set(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); f.set(target, value);
    }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
}
