# Checklist xây dựng màn hình Shop

## THỨ TỰ TRIỂN KHAI

### PHẦN 1: CƠ BẢN (BẮT BUỘC)

- [x] Tạo `ShopActivity.java` trong package `ui`
- [x] Tạo `activity_shop.xml` trong `res/layout`
- [x] Thêm `ShopActivity` vào `AndroidManifest.xml`
- [x] Tạo icons (back, home, gem, menu, arrow)
- [x] Thêm colors cho dark theme
- [x] Tạo "Popular Picks" section title
- [x] Thêm "Shop All" button

### PHẦN 2: DATA & ADAPTER (BẮT BUỘC)

- [x] Tạo layout cho item sản phẩm (`item_shop_product.xml`)
- [x] Tạo `ShopItem.java` model class
- [x] Tạo `ShopAdapter.java`
- [x] Implement ViewHolder trong ShopAdapter
- [x] Handle different item types (circular vs banner)
- [x] Add click listeners cho items
- [x] Thiết kế RecyclerView với GridLayoutManager trong ShopActivity

### PHẦN 3: NAVIGATION (BẮT BUỘC)

- [x] Thêm navigation từ MainActivity đến ShopActivity
  - **Guide:** In MainActivity, add button or menu item to open Shop

  ```java
  findViewById(R.id.btnShop).setOnClickListener(v -> {
      Intent intent = new Intent(MainActivity.this, ShopActivity.class);
      startActivity(intent);
  });
  ```

  - **Note:** Cần thêm button hoặc menu item trong MainActivity layout

- [x] Implement back navigation
  - **Guide:** In ShopActivity, handle back button (đã implement sẵn)

  ```java
  findViewById(R.id.btnBack).setOnClickListener(v -> {
      finish(); // Close activity and go back
  });
  ```

- [x] Handle item click navigation (nếu cần)
  - **Guide:** In ShopActivity, implement adapter click listener (đã có sẵn)

  ```java
  shopAdapter = new ShopAdapter(shopItems, item -> {
      // Navigate to product detail or purchase flow
      Intent intent = new Intent(ShopActivity.this, ProductDetailActivity.class);
      intent.putExtra("product_id", item.getId());
      startActivity(intent);
  });
  ```

  - **Note:** Cần tạo ProductDetailActivity nếu muốn implement chi tiết

### PHẦN 4: TESTING (BẮT BUỘC)

- [ ] Test layout trên different screen sizes
- [ ] Test RecyclerView scrolling
- [ ] Test click interactions
- [ ] Test navigation flow

### PHẦN 5: ADDITIONAL FEATURES (OPTIONAL)

- [ ] Add search functionality
- [ ] Implement filtering/sorting
- [ ] Add loading states
- [ ] Add error handling
- [ ] Implement pull-to-refresh

---

## CHI TIẾT TỪNG BƯỚC

### PHẦN 2: DATA & ADAPTER

#### BƯỚC 1: Tạo layout cho item sản phẩm (`item_shop_product.xml`)

- **Guide:** Create with rounded corners and dark background

```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="12dp"
    app:cardBackgroundColor="@color/dark_card_bg">
    <!-- Content here -->
</androidx.cardview.widget.CardView>
```

#### BƯỚC 2: Tạo `ShopItem.java` model class

- **Guide:** Right-click package `model` → New → Java Class → Name: `ShopItem`

```java
public class ShopItem {
    private String id;
    private String name;
    private String imageUrl;
    private String price;
    private String type; // "circular" or "banner"

    public ShopItem(String id, String name, String imageUrl, String price, String type) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.type = type;
    }

    // Getters and setters...
}
```

#### BƯỚC 3: Tạo `ShopAdapter.java`

- **Guide:** Right-click package `adapter` → New → Java Class → Name: `ShopAdapter`

```java
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    private List<ShopItem> shopItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }

    public ShopAdapter(List<ShopItem> shopItems, OnItemClickListener listener) {
        this.shopItems = shopItems;
        this.listener = listener;
    }
}
```

#### BƯỚC 4: Implement ViewHolder trong ShopAdapter

- **Guide:** Add ViewHolder class inside ShopAdapter

```java
public static class ShopViewHolder extends RecyclerView.ViewHolder {
    private CardView cardView;
    private ImageView imageView;
    private TextView nameText;

    public ShopViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.cardView);
        imageView = itemView.findViewById(R.id.imageView);
        nameText = itemView.findViewById(R.id.nameText);
    }

    public void bind(ShopItem item, OnItemClickListener listener) {
        nameText.setText(item.getName());
        // Load image with Glide or Picasso
        Glide.with(imageView.getContext())
             .load(item.getImageUrl())
             .into(imageView);

        itemView.setOnClickListener(v -> listener.onItemClick(item));
    }
}
```

#### BƯỚC 5: Handle different item types (circular vs banner)

- **Guide:** In onBindViewHolder, check item type and adjust layout

```java
@Override
public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
    ShopItem item = shopItems.get(position);
    holder.bind(item, listener);

    // Adjust layout based on type
    if ("banner".equals(item.getType())) {
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // Set banner-specific layout
    } else {
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        // Set circular layout
    }
}
```

#### BƯỚC 6: Add click listeners cho items

- **Guide:** Already implemented in ViewHolder bind method above

#### BƯỚC 7: Thiết kế RecyclerView với GridLayoutManager trong ShopActivity

- **Guide:** In `ShopActivity.java`:

```java
RecyclerView recyclerView = findViewById(R.id.recyclerView);
GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
recyclerView.setLayoutManager(layoutManager);
recyclerView.setAdapter(shopAdapter);
```

### PHẦN 3: NAVIGATION

#### **Bước 1:** Thêm navigation từ MainActivity đến ShopActivity

- **Guide:** Thêm button hoặc menu item trong MainActivity để mở Shop
- **Recommended Approach:** Thêm trong MainActivity.java
  ```java
  // Trong onCreate() của MainActivity
  findViewById(R.id.btnShop).setOnClickListener(v -> {
      Intent intent = new Intent(MainActivity.this, ShopActivity.class);
      startActivity(intent);
  });
  ```
- **Note:** Cần thêm button trong MainActivity layout trước

#### **Bước 2:** Implement back navigation

- **Guide:** Đã implement trong ShopActivity (btnBack click listener)
- **Code:**
  ```java
  findViewById(R.id.btnBack).setOnClickListener(v -> {
      finish(); // Đóng activity và quay lại
  });
  ```

#### **Bước 3:** Handle item click navigation

- **Guide:** Đã có sẵn trong ShopActivity với comment
- **Option 1: Hiển thị Toast message**
  ```java
  shopAdapter = new ShopAdapter(shopItems, item -> {
      Toast.makeText(this, "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show();
  });
  ```
- **Option 2: Navigate đến ProductDetailActivity**
  ```java
  shopAdapter = new ShopAdapter(shopItems, item -> {
      Intent intent = new Intent(ShopActivity.this, ProductDetailActivity.class);
      intent.putExtra("product_id", item.getId());
      intent.putExtra("product_name", item.getName());
      startActivity(intent);
  });
  ```

### PHẦN 4: TESTING

#### **Bước 1:** Test layout trên different screen sizes

- **Guide:** Use Android Studio's layout preview with different devices
- **Steps:**
  1. Mở `activity_shop.xml`
  2. Click vào "Preview" tab
  3. Chọn different devices (Pixel 6, Tablet, etc.)
  4. Check layout hiển thị đúng trên tất cả sizes

#### **Bước 2:** Test RecyclerView scrolling

- **Guide:** Add nhiều items và test smooth scrolling
- **Steps:**
  1. Thêm nhiều sample items trong `loadSampleData()`
  2. Run app trên device/emulator
  3. Scroll lên/xuống để test performance
  4. Check không có lag hay giật

#### **Bước 3:** Test click interactions

- **Guide:** Test tất cả clickable elements
- **Checklist:**
  - [ ] Back button - đóng activity
  - [ ] Menu button - hiển thị menu/action
  - [ ] Shop All button - thực thi action
  - [ ] Item cards - click listener hoạt động

#### **Bước 4:** Test navigation flow

- **Guide:** Test complete user journey
- **Flow:**
  1. Main → Shop (navigation)
  2. Shop → Back (quay lại Main)
  3. Shop → Item Click (product detail nếu có)
  4. Product Detail → Back (quay lại Shop)

### PHẦN 5: ADDITIONAL FEATURES (OPTIONAL)

#### **Bước 1:** Add search functionality

- **Guide:** Add SearchView đến header và implement filtering
- **Layout:**
  ```xml
  <!-- Thêm vào header của activity_shop.xml -->
  <androidx.appcompat.widget.SearchView
      android:id="@+id/searchView"
      android:layout_width="0dp"
      android:layout_height="wrap_content"
      app:queryHint="Search items..." />
  ```
- **Java:**

  ```java
  searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
          filterItems(query);
          return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
          filterItems(newText);
          return true;
      }
  });
  ```

#### **Bước 2:** Implement filtering/sorting

- **Guide:** Thêm filter options và sort functionality
- **Code:**

  ```java
  public void filterItems(String query) {
      List<ShopItem> filteredList = new ArrayList<>();
      for (ShopItem item : allItems) {
          if (item.getName().toLowerCase().contains(query.toLowerCase())) {
              filteredList.add(item);
          }
      }
      shopAdapter.updateList(filteredList);
  }

  public void sortItems(String sortBy) {
      switch (sortBy) {
          case "name":
              Collections.sort(allItems, Comparator.comparing(ShopItem::getName));
              break;
          case "price":
              Collections.sort(allItems, Comparator.comparing(ShopItem::getPrice));
              break;
      }
      shopAdapter.notifyDataSetChanged();
  }
  ```

#### **Bước 3:** Add loading states

- **Guide:** Sử dụng ProgressBar và handle loading visibility
- **Layout:**
  ```xml
  <ProgressBar
      android:id="@+id/progressBar"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:visibility="gone" />
  ```
- **Java:**

  ```java
  private void loadItems() {
      progressBar.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.GONE);

      // Simulate API call
      new Handler().postDelayed(() -> {
          progressBar.setVisibility(View.GONE);
          recyclerView.setVisibility(View.VISIBLE);
          shopAdapter.updateList(shopItems);
      }, 2000);
  }
  ```

#### **Bước 4:** Add error handling

- **Guide:** Hiển thị error messages khi loading thất bại
- **Layout:**
  ```xml
  <LinearLayout
      android:id="@+id/errorLayout"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:orientation="vertical"
      android:gravity="center"
      android:visibility="gone">
      <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:text="Failed to load items" />
      <Button
          android:id="@+id/btnRetry"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:text="Retry" />
  </LinearLayout>
  ```

#### **Bước 5:** Implement pull-to-refresh

- **Guide:** Sử dụng SwipeRefreshLayout với RecyclerView
- **Layout:**
  ```xml
  <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
      android:id="@+id/swipeRefreshLayout"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
      <androidx.recyclerview.widget.RecyclerView
          android:id="@+id/recyclerView"
          android:layout_width="match_parent"
          android:layout_height="match_parent" />
  </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
  ```
- **Java:**
  ```java
  swipeRefreshLayout.setOnRefreshListener(() -> {
      loadItems();
      swipeRefreshLayout.setRefreshing(false);
  });
  ```
