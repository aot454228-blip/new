package com.highlands.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// One bundled official logo for every role; only transparent padding is trimmed.
// Source: https://shop.highlandscoffee.com.vn/ (logo asset served by bizweb.dktcdn.net).
public class LogoTron extends JComponent {
    private static final BufferedImage GOC = taiLogo();
    private static final BufferedImage LOGO_DO = doiMau(GOC, Theme.BRAND);
    private final int co;

    public LogoTron(int co) {
        this.co = co;
        Dimension d = new Dimension(co, co);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setToolTipText("Highlands Coffee");
    }

    private static BufferedImage taiLogo() {
        try (InputStream in = LogoTron.class.getResourceAsStream("/static/images/highlands-coffee-logo.png")) {
            if (in == null) throw new IOException("Missing Highlands Coffee logo resource");
            BufferedImage anh = ImageIO.read(in);
            if (anh == null) throw new IOException("Invalid Highlands Coffee logo resource");
            int trai = anh.getWidth(), tren = anh.getHeight(), phai = -1, duoi = -1;
            for (int y = 0; y < anh.getHeight(); y++) {
                for (int x = 0; x < anh.getWidth(); x++) {
                    if ((anh.getRGB(x, y) >>> 24) != 0) {
                        trai = Math.min(trai, x);
                        tren = Math.min(tren, y);
                        phai = Math.max(phai, x);
                        duoi = Math.max(duoi, y);
                    }
                }
            }
            if (phai < trai) throw new IOException("Empty Highlands Coffee logo");
            return anh.getSubimage(trai, tren, phai - trai + 1, duoi - tren + 1);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load the bundled Highlands Coffee logo", e);
        }
    }

    // The official white artwork is a monochrome alpha mask: preserve its shape and transparency.
    private static BufferedImage doiMau(BufferedImage anh, Color mau) {
        BufferedImage out = new BufferedImage(anh.getWidth(), anh.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(anh, 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(mau);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.dispose();
        return out;
    }

    private static void ve(Graphics2D g, int w, int h, boolean nenToi) {
        Theme.khuDangCua(g);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Contain the complete oval logo; never stretch or crop the artwork into a circle.
        double tiLe = Math.min(w / (double) GOC.getWidth(), h / (double) GOC.getHeight());
        int rw = Math.max(1, (int) Math.round(GOC.getWidth() * tiLe));
        int rh = Math.max(1, (int) Math.round(GOC.getHeight() * tiLe));
        g.drawImage(nenToi ? GOC : LOGO_DO, (w - rw) / 2, (h - rh) / 2, rw, rh, null);
    }

    private boolean nenToi() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p.isOpaque() && p.getBackground() != null) {
                Color c = p.getBackground();
                return (0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue()) < 150;
            }
        }
        return false;
    }

    private static BufferedImage anhBieuTuong(int co) {
        BufferedImage anh = new BufferedImage(co, co, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = anh.createGraphics();
        ve(g, co, co, false);
        g.dispose();
        return anh;
    }

    static void datBieuTuong(Window cuaSo) {
        cuaSo.setIconImages(List.of(anhBieuTuong(16), anhBieuTuong(32), anhBieuTuong(64), anhBieuTuong(128)));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        ve(g, getWidth(), getHeight(), nenToi());
        g.dispose();
    }
}
