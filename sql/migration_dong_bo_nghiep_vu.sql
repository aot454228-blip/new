-- Chạy trên HighlandsDB sau các migration nhân viên và khách hàng.
-- Các bản ghi hiện có tiếp tục xuất hiện trong thực đơn sau khi nâng cấp.
USE HighlandsDB;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH('dbo.san_pham', 'dang_ban') IS NULL
    ALTER TABLE dbo.san_pham ADD dang_ban BIT NOT NULL CONSTRAINT df_san_pham_dang_ban DEFAULT (1);
IF COL_LENGTH('dbo.topping', 'dang_ban') IS NULL
    ALTER TABLE dbo.topping ADD dang_ban BIT NOT NULL CONSTRAINT df_topping_dang_ban DEFAULT (1);
IF COL_LENGTH('dbo.danh_muc', 'dang_ban') IS NULL
    ALTER TABLE dbo.danh_muc ADD dang_ban BIT NOT NULL CONSTRAINT df_danh_muc_dang_ban DEFAULT (1);
COMMIT;
GO

-- Lưu tên món tại thời điểm bán; không tạo bảng hoặc xóa dữ liệu.
IF COL_LENGTH('dbo.chi_tiet_don_hang', 'ten_san_pham') IS NULL
    ALTER TABLE dbo.chi_tiet_don_hang ADD ten_san_pham NVARCHAR(100) NULL;
GO
UPDATE c SET ten_san_pham = s.ten
FROM dbo.chi_tiet_don_hang c JOIN dbo.san_pham s ON s.id = c.san_pham_id
WHERE c.ten_san_pham IS NULL;
GO
