package com.highlands.ui;

import com.highlands.service.*;
import com.highlands.repository.*;
import org.springframework.context.ApplicationContext;
import javax.swing.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Runs the real statistics panel and its actual 15-second Swing timer against the connected DB. */
public class ThongKeRefreshCheck {
    public static void verify(ApplicationContext ctx) throws Exception {
        var calls = new AtomicInteger();
        var refreshed = new CountDownLatch(2);
        var original = ctx.getBean(ThongKeService.class);
        ThongKeService tracked = new ThongKeService(ctx.getBean(DonHangRepository.class),
                ctx.getBean(PhieuNhapRepository.class), ctx.getBean(ChiTietDonHangRepository.class)) {
            @Override public Map<String, Object> doanhThu(LocalDate from, LocalDate to) {
                var result = original.doanhThu(from, to);
                calls.incrementAndGet();
                refreshed.countDown();
                return result;
            }
        };
        class Panel extends TrangThongKe {
            boolean showing = true;
            Panel() { super(new DichVu(ctx.getBean(KhuyenMaiService.class), ctx.getBean(SanPhamService.class),
                    ctx.getBean(DanhMucService.class), ctx.getBean(ToppingService.class), tracked)); }
            @Override public boolean isShowing() { return showing; }
        }
        Panel[] panels = new Panel[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                panels[0] = new Panel();
                panels[0].addNotify();
                panels[0].khiMo();
            });
            if (!refreshed.await(22, TimeUnit.SECONDS))
                throw new AssertionError("Statistics did not refresh after 15 seconds");
            var busyField = TrangThongKe.class.getDeclaredField("dangTai");
            busyField.setAccessible(true);
            boolean[] idle = {false};
            for (int attempt = 0; attempt < 100 && !idle[0]; attempt++) {
                SwingUtilities.invokeAndWait(() -> {
                    try { idle[0] = !busyField.getBoolean(panels[0]); }
                    catch (IllegalAccessException e) { throw new RuntimeException(e); }
                });
                if (!idle[0]) Thread.sleep(100);
            }
            if (!idle[0]) throw new AssertionError("Statistics background query did not finish");
            var field = TrangThongKe.class.getDeclaredField("tuLamMoi");
            field.setAccessible(true);
            Timer timer = (Timer) field.get(panels[0]);
            SwingUtilities.invokeAndWait(() -> {
                panels[0].showing = false;
                int before = calls.get();
                for (var listener : timer.getActionListeners())
                    listener.actionPerformed(new java.awt.event.ActionEvent(timer, 0, "hidden"));
                if (calls.get() != before) throw new AssertionError("Hidden statistics queried DB");
                panels[0].removeNotify();
                if (timer.isRunning()) throw new AssertionError("Timer still running after disposal");
            });
            System.out.println("PASS: real Swing statistics panel, initial load, 15-second refresh, hidden/disposed lifecycle");
        } finally {
            if (panels[0] != null) SwingUtilities.invokeAndWait(() -> panels[0].removeNotify());
        }
    }
}
