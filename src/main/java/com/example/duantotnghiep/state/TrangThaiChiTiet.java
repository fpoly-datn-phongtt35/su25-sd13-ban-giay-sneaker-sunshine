package com.example.duantotnghiep.state;

import java.util.Calendar;
import java.util.Date;

public enum TrangThaiChiTiet {
    HUY_DON(-2, "Hủy đơn hàng"),
    HUY_GIAO_DICH(-1, "Hủy giao dịch"),
    DANG_XU_LY(0, "Đang xử lý"),
    CHO_XAC_NHAN(1, "Chờ xác nhận"),
    DA_XAC_NHAN(2, "Đã xác nhận"),
    CHO_NHAP_HANG(3, "Chờ nhập hàng"),
    DANG_CHUAN_BI_HANG(4, "Đang chuẩn bị hàng"),
    DANG_GIAO_HANG(5, "Đang giao hàng"),
    GIAO_THANH_CONG(6, "Giao hàng thành công"),
    GIAO_THAT_BAI(7, "Giao hàng thất bại"),
    MAT_HANG(11, "Mất hàng"),
    DA_HOAN_TIEN(13, "Đã hoàn tiền"),
    DA_HOAN_THANH(14, "Đã hoàn thành");

    private final int ma;
    private final String moTa;

    TrangThaiChiTiet(int ma, String moTa) {
        this.ma = ma;
        this.moTa = moTa;
    }

    public int getMa() {
        return ma;
    }

    public String getMoTa() {
        return moTa;
    }

    public static TrangThaiChiTiet tuMa(int ma) {
        for (TrangThaiChiTiet t : values()) {
            if (t.ma == ma) return t;
        }
        throw new IllegalArgumentException("Mã trạng thái chi tiết không hợp lệ: " + ma);
    }

    public boolean canTransitionTo(TrangThaiChiTiet next) {
        if (next == HUY_DON) {
            return this == DANG_XU_LY || this == CHO_XAC_NHAN || this == DA_XAC_NHAN;
        }

        if (next == HUY_GIAO_DICH) {
            return this == DANG_XU_LY || this == CHO_XAC_NHAN;
        }

        return switch (this) {
            case DANG_XU_LY -> next == CHO_XAC_NHAN;
            case CHO_XAC_NHAN -> next == DA_XAC_NHAN;
            case DA_XAC_NHAN -> next == CHO_NHAP_HANG || next == DANG_CHUAN_BI_HANG;
            case CHO_NHAP_HANG, DANG_CHUAN_BI_HANG -> next == DANG_GIAO_HANG;
            case DANG_GIAO_HANG -> next == GIAO_THANH_CONG || next == GIAO_THAT_BAI || next == MAT_HANG;
            case GIAO_THAT_BAI, MAT_HANG -> next == DA_HOAN_TIEN;
            case DA_HOAN_TIEN -> next == DA_HOAN_THANH;
            case GIAO_THANH_CONG -> next == DA_HOAN_THANH;
            default -> false;
        };
    }

    public TrangThaiTong toTrangThaiTong() {
        return switch (this) {
            case HUY_DON, HUY_GIAO_DICH, MAT_HANG -> TrangThaiTong.DA_HUY;
            case DA_HOAN_TIEN, GIAO_THANH_CONG, DA_HOAN_THANH -> TrangThaiTong.THANH_CONG;
            default -> TrangThaiTong.DANG_XU_LY;
        };
    }

    /**
     * Kiểm tra xem đơn hàng có thể khiếu nại không (trong 7 ngày sau khi giao hàng thành công).
     *
     * @param deliveredAt ngày giao hàng
     * @return true nếu trong 7 ngày kể từ ngày giao hàng thành công
     */
    public boolean coTheKhieuNai(Date deliveredAt) {
        if (this != GIAO_THANH_CONG || deliveredAt == null) return false;

        Calendar cal = Calendar.getInstance();
        cal.setTime(deliveredAt);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Date hanKhieuNai = cal.getTime();

        Date now = new Date();
        return now.before(hanKhieuNai);
    }
}
