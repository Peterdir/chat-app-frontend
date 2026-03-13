# Hướng dẫn khắc phục lỗi UI Common

## 1. Lỗi AppBarLayout / Toolbar bị trắng (hoặc đổi màu) khi cuộn nội dung
**Hiện tượng:** Khi sử dụng `CoordinatorLayout` với `AppBarLayout` và `NestedScrollView`, tiêu đề hoặc nền của Toolbar bị chuyển sang màu trắng (hoặc màu Light) khi người dùng kéo xuống (scroll).

### Cách khắc phục:
Trong file XML layout, tại thẻ `AppBarLayout`, hãy thêm thuộc tính:
```xml
<com.google.android.material.appbar.AppBarLayout
    ...
    app:liftOnScroll="false"
    app:elevation="0dp"
    android:background="@color/your_dark_color">
```

**Giải thích:**
- `app:liftOnScroll="false"`: Ngăn chặn AppBarLayout tự động thay đổi trạng thái (elevation/color) khi nội dung bên dưới cuộn qua.
- `app:elevation="0dp"`: Loại bỏ bóng đổ mặc định nếu không cần thiết để giữ giao diện phẳng.
- `android:background`: Luôn chỉ định màu nền cụ thể cho AppBarLayout và Toolbar thay vì để mặc định.

---

## 2. Lỗi không hiển thị tiêu đề trên Toolbar
**Hiện tượng:** Đã đặt `app:title` trong XML hoặc gọi `setTitle()` trong Code nhưng tiêu đề vẫn không hiện hoặc bị ghi đè.

### Cách khắc phục:
Trong Activity, thay vì sử dụng `setSupportActionBar(toolbar)`, hãy thao tác trực tiếp với đối tượng Toolbar:
```java
Toolbar toolbar = findViewById(R.id.toolbar);
if (toolbar != null) {
    // Sử dụng setTitle trực tiếp trên toolbar thay vì qua ActionBar
    toolbar.setTitle("Tiêu đề của bạn");
    toolbar.setNavigationOnClickListener(v -> finish());
}
```

**Lưu ý:** 
- Nếu dùng `setSupportActionBar(toolbar)`, tiêu đề sẽ do Theme của ứng dụng quản lý, đôi khi dẫn đến việc chữ bị trắng trên nền trắng hoặc bị ẩn đi nếu `setDisplayShowTitleEnabled(false)` được gọi ở đâu đó.
- Luôn đảm bảo `app:titleTextColor="@android:color/white"` được đặt trong XML nếu dùng Dark Mode.
