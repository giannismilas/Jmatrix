# EShop Application
### 🏷️ Discount Codes

| Method | Endpoint                         | Description                              | Access     |
|--------|-----------------------------------|------------------------------------------|------------|
| POST   | `/discount-codes/api`             | Create discount code `{code, percent, expiresAt?}` | ADMIN only |
| GET    | `/discount-codes/api/active`      | Get the most recent active code           | Public     |
| DELETE | `/discount-codes/api/{id}`        | Delete a discount code by id              | ADMIN only |

Notes:
- `expiresAt` is an ISO local datetime string, e.g. `2025-08-30T12:00`.
- Expired codes are auto-removed hourly by a scheduler; only currently active codes are shown in the UI.

A Spring Boot-based e-commerce platform with full shopping functionality including user authentication, product management, cart system, and order processing. Supports two user roles: **regular customers** and **administrators**.

---

## 📚 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies](#technologies)
- [Setup](#setup)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [UI Components](#ui-components)
- [Error Handling](#error-handling)
- [Future Enhancements](#future-enhancements)

---

## 🧾 Overview

The EShop Application is a full-featured e-commerce platform designed using Spring Boot. It allows customers to browse products, manage shopping carts, place orders, and administrators to manage users and products.

---

## ✨ Features

### 🔐 User Authentication
- User registration and login
- Password encryption using BCrypt
- Session management

### 📦 Product Management
- CRUD operations for products
- Search products by ID
- Admin-only access for modifications

### ⭐ Product Reviews & Ratings
- Users can add, edit, and delete their own review per product (rating 1–5 and optional comment)
- Admins have read-only access (cannot create, edit, or delete reviews)
- Average rating and total review count shown next to product names in the grid and inside the product modal
- Reviews list displayed in the product modal; form visible only to authenticated non-admin users

### 🛒 Shopping Cart
- Add and remove products
- Manage product quantities
- Persistent cart per user
- Discount codes
  - Admins can create discount codes with a percentage and optional expiration
  - Admins can delete discount codes
  - Customers can apply/clear discount codes in the cart
  - Expiration is shown to customers; expired codes are automatically cleaned up
  - Cart discounts are cleared after a successful order so they don't persist to the next cart

### 📬 Order Processing
- Create orders from the cart
- Order status updates (Admin)
- View order history

### 👤 User Management
- Edit profile information
- Delete account
- Admin dashboard for user management

---

## 🛠️ Technologies

### Backend
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- Thymeleaf
- Spring Scheduling (automatic cleanup of expired discount codes)

### Frontend
- Bootstrap 5
- JavaScript
- Thymeleaf templates

### Database
- MySQL 8.0+
- Hibernate ORM

### Build Tool
- Maven 3.8+

---

## ⚙️ Setup

### 🔧 Prerequisites
- Java JDK 17+
- MySQL 8.0+
- Maven 3.8+

### 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/eshop-application.git
   cd eshop-application
   ```

2. **Create MySQL Database**
   ```sql
   CREATE DATABASE eshop_database;
   ```

3. **Configure Database Credentials**

   Update `application.properties` with your MySQL username and password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/eshop_database
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### 👤 Default Admin Account

| Username | Password |
|----------|----------|
| admin    | admin    |

---

## 🧩 Database Schema

There is a test database.sql file that imports some products that were used for testing purposes.

### Key Entities:
- **User** – Stores user credentials and profile info
- **Product** – Product catalog
- **Cart** – User’s active shopping cart
- **Order** – Records completed orders
- **CartItem / OrderItem** – Itemized product references in carts and orders
- **Review** – User review for a product with fields: rating (1–5), comment, timestamps; unique constraint `(user_id, product_id)` ensures 1 review per user per product
- **DiscountCode** – Admin-defined discount codes: `code` (unique), `percent` (1–100], `active` flag, `startsAt` (optional), `expiresAt` (optional). Expired codes are auto-deleted by a scheduled task.

---

## 📡 API Endpoints

### 🔑 Authentication

| Method | Endpoint       | Description           | Access  |
|--------|----------------|-----------------------|---------|
| GET    | `/login`       | Login page            | Public  |
| GET    | `/register`    | Registration page     | Public  |
| POST   | `/register`    | Create new account    | Public  |
| POST   | `/logout`      | Logout                | Private |

### 📦 Products

| Method | Endpoint              | Description           | Access       |
|--------|------------------------|-----------------------|--------------|
| GET    | `/products`            | View all products     | USER, ADMIN  |
| GET    | `/products/search`     | Search by product ID  | USER, ADMIN  |
| GET    | `/products/api/{id}`   | Get product by ID     | USER, ADMIN  |
| POST   | `/products/api`        | Create product        | ADMIN only   |
| PUT    | `/products/api/{id}`   | Update product        | ADMIN only   |
| DELETE | `/products/api/{id}`   | Delete product        | ADMIN only   |

### 🌟 Reviews & Ratings

| Method | Endpoint                         | Description                                  | Access        |
|--------|-----------------------------------|----------------------------------------------|---------------|
| GET    | `/reviews/api/product/{id}`       | List reviews for a product + avg + count     | USER, ADMIN   |
| POST   | `/reviews/api/product/{id}`       | Add or update current user's review          | USER only     |
| PUT    | `/reviews/api/{reviewId}`         | Update own review by id                      | USER only     |
| DELETE | `/reviews/api/{reviewId}`         | Delete own review by id                      | USER only     |

### 🛒 Cart

| Method | Endpoint                   | Description         | Access    |
|--------|----------------------------|---------------------|-----------|
| GET    | `/cart`                    | View cart           | USER only |
| POST   | `/cart/add/{productId}`    | Add to cart         | USER only |
| DELETE | `/cart/remove/{productId}` | Remove from cart    | USER only |
| POST   | `/cart/discount/apply?code=CODE` | Apply discount code to current cart | USER only |
| DELETE | `/cart/discount/clear`     | Clear discount code from current cart | USER only |

### 🧾 Orders

| Method | Endpoint                           | Description          | Access     |
|--------|------------------------------------|----------------------|------------|
| POST   | `/api/orders/create`               | Create order         | USER only  |
| PUT    | `/api/orders/{orderId}/status`     | Update order status  | ADMIN only |
| DELETE | `/api/orders/{orderId}`            | Delete order         | ADMIN only |

---

## 🔐 Security

- **Authentication:** Form-based login with Spring Security
- **Password Encryption:** BCrypt
- **CSRF Protection:** Enabled
- **Role-Based Access:**
    - `ROLE_ADMIN` – Full access
    - `ROLE_USER` – Shopping and profile access
- **Restrictions:**
    - Admins cannot use the shopping cart
    - Admins cannot create, edit, or delete reviews (read-only)
    - Only Admins can create/delete discount codes
    - Users cannot modify products
    - All endpoints are secured appropriately

---

## 🖼 UI Components

### Key Pages:

- **`products.html`**
    - Product grid with search
    - Admin product controls
    - Add-to-cart button
    - Discount codes section:
        - Shows all currently active discount codes with percentage and expiration
        - Admin-only modal to create codes (code, percent, optional expiration)
        - Admin can delete codes from the list
    - Reviews section in the product details modal:
        - Shows average rating and review count
        - Displays list of reviews with username and stars
        - Review form for authenticated non-admin users
        - Admins see read-only message; no form shown

- **`cart.html`**
    - Cart contents
    - Remove items
    - Checkout functionality
    - Discount code input to apply or clear a code
    - Displays subtotal, discount amount, and total after discount

- **`orders.html`**
    - Order history
    - Admin: status updates
    - Order details view

- **`profile.html`**
    - Profile display and editing
    - Account deletion

- **`users.html` (Admin)**
    - User list and roles management

---

## ❗ Error Handling

Robust error handling for:

- Invalid login or registration
- Unauthorized access attempts
- Entity not found errors
- Constraint violations (e.g., duplicate emails)
- Invalid form submissions

All errors provide clear, user-friendly feedback on the UI.

---

## 🚀 Future Enhancements

### 🛠 Enhanced Product Management
- Product categories and tags
- Image uploads
- Inventory tracking

### 💳 Payment Integration
- Stripe / PayPal
- Payment history dashboard

### 🎨 UI Improvements
- Advanced search filters
- Pagination
- Optional dark mode

### ⚡ Performance
- Caching strategies
- Lazy loading
- Optimized image delivery

---

## 🧪 Unit Testing
Available Unit Tests for the controllers and services used in the development in **`/com/stackpuz/example/backend`**:
- CartControllerTest
- LoginControllerTest
- OrderControllerTest
- ProductControllerTest
- ProfileControllerTest
- UserControllerTest
- CartServiceTest
- ProductServiceTest

Planned tests:
- DiscountCodeService validation and expiration handling
- CartService discount application and clearing on order
