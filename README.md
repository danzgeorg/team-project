# IPOS-PU — InfoPharma Online Purchasing System (Public Portal)

**Team 24/C — Nova Solutions**
IN2033 Team Project | City, St George's University of London

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture](#architecture)
4. [Project Structure](#project-structure)
5. [Domain Model](#domain-model)
6. [Interfaces & Integration](#interfaces--integration)
7. [Use Cases](#use-cases)
8. [Database Design](#database-design)
9. [GUI Screens](#gui-screens)
10. [Quick Start](#quick-start)
11. [Git Workflow](#git-workflow)
12. [Development Roadmap](#development-roadmap)
13. [Team](#team)
14. [Coding Conventions](#coding-conventions)

---

## Project Overview

IPOS-PU is the **Public Portal** subsystem of the InfoPharma Online Purchasing System (IPOS), a multi-team integrated application built as part of the IN2033 Team Project module. Nova Solutions (Team 24/C) is responsible for this subsystem.

The Public Portal is the customer-facing interface of IPOS. It enables:

- **Non-commercial and commercial members** to register, log in, browse a pharmaceutical product catalogue, manage a shopping cart, place orders, and track or refund them.
- **Admins** to create and manage promotional campaigns and generate sales, campaign, and engagement reports.

IPOS-PU integrates with two other subsystems developed by partner teams:

| Subsystem | Team | Role in Integration |
|-----------|------|---------------------|
| IPOS-SA (Sales Admin) | Team 22 | Approves commercial memberships; provides product catalogue and invoices |
| IPOS-CA (Commercial Admin) | Team 23 | Provides stock availability; deducts inventory on order confirmation |

Integration is achieved via a **shared MySQL database** — IPOS-PU queries IPOS-SA and IPOS-CA schemas directly using dedicated adapter classes.

---

## Technology Stack

| Concern | Technology | Version |
|---------|-----------|---------|
| Language | Java | 17 |
| UI Framework | JavaFX | 17.0.2 |
| Build Tool | Maven | 3.8+ |
| Database | MySQL | 8.0 |
| DB Driver | MySQL Connector/J | 8.3.0 |
| Testing | JUnit 5 (Jupiter) | 5.10.2 |
| IDE | IntelliJ IDEA | Any recent |

> **JDK 17 is required.** JavaFX 17.0.2 is not compatible with JDK 18+. Do not upgrade the Java version.

### Maven Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| maven-compiler-plugin | (default) | Java 17 compilation |
| javafx-maven-plugin | 0.0.8 | `mvn javafx:run` launcher |
| maven-surefire-plugin | 3.2.5 | JUnit 5 test runner |

---

## Architecture

IPOS-PU follows a **layered architecture** with strict separation of concerns. No layer may skip a layer below it (e.g. GUI must not call DAO directly).

```
┌──────────────────────────────────────────┐
│             GUI (JavaFX Screens)         │  ← User interaction only, no business logic
├──────────────────────────────────────────┤
│        Controller (Provided Interfaces)  │  ← Routes GUI actions to service layer
├──────────────────────────────────────────┤
│          Service (Business Logic)        │  ← Orchestrates DAOs, adapters, validation
├───────────────────────┬──────────────────┤
│    DAO (MySQL via JDBC)│ Adapters (Ext.) │  ← Data access: own DB + other subsystems
└───────────────────────┴──────────────────┘
              MySQL 8.0 (ipos_pu + ipos_sa + ipos_ca schemas)
```

### Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| Singleton | `DatabaseConnection` | Single shared JDBC connection pool |
| DAO | `MemberDAO`, `ProductDAO`, `OrderDAO`, etc. | Isolate SQL from business logic |
| Adapter | `InventoryDBAdapter`, `MerchantAccountDBAdapter`, `CommercialApprovalDBAdapter` | Wrap cross-subsystem DB queries behind interfaces |
| Service Layer | `OrderService`, `MembershipService`, etc. | Centralise business rules |
| Interface-based Design | `provided/` and `required/` packages | Decoupled multi-team integration |

---

## Project Structure

```
TeamProject-IN2033/
├── pom.xml                                 Maven build descriptor
├── README.md                               Project documentation
├── .gitignore                              Excludes IDE files, credentials, build output
│
├── sql/
│   ├── ipos_pu_schema.sql                  CREATE TABLE statements for all IPOS-PU tables
│   ├── ipos_pu_seed_data.sql               Test data (products, members, campaigns)
│   └── cross_subsystem_views.sql           GRANTs + optional VIEWs for SA/CA schema access
│
└── src/
    ├── main/
    │   ├── java/com/novasolutions/ipospu/
    │   │   ├── MainApp.java                JavaFX Application entry point
    │   │   ├── model/                      Domain/entity POJOs + enums
    │   │   ├── interfaces/
    │   │   │   ├── provided/               APIs exposed to Teams 22 & 23
    │   │   │   └── required/               APIs consumed from Teams 22 & 23
    │   │   ├── controller/                 Implements provided interfaces; routes to services
    │   │   ├── service/                    Business logic and orchestration
    │   │   ├── db/                         DAOs and DatabaseConnection singleton
    │   │   ├── impl/                       Adapter + external service implementations
    │   │   └── gui/                        JavaFX screen classes
    │   └── resources/
    │       └── db.properties.example       Database config template
    └── test/
        └── java/com/novasolutions/ipospu/  JUnit 5 tests (mirrors main structure)
```

---

## Domain Model

### Entities

| Class | Key Fields | Notes |
|-------|-----------|-------|
| `Member` | id, name, email, passwordHash, memberType, membershipStatus | Covers both NonCommercial and Commercial members |
| `Product` | id, name, description, price, stockQuantity | Sourced from IPOS-SA catalogue via adapter |
| `CartItem` | memberId, productId, quantity | Ephemeral; cleared after successful checkout |
| `Order` | id, memberId, totalAmount, status, createdAt | Status lifecycle: Received → Processing → Shipped → Delivered |
| `PromotionCampaign` | id, name, discountPercent, startDate, endDate, targetProducts | Created and managed by admins |

### Enumerations

| Enum | Values |
|------|--------|
| `MemberType` | `NonCommercial`, `Commercial` |
| `MembershipStatus` | `Pending`, `Approved`, `Rejected` |
| `OrderStatus` | `Received`, `Processing`, `Shipped`, `Delivered` |
| `ReportType` | `Sales`, `Campaign`, `Engagement` |

---

## Interfaces & Integration

### Provided Interfaces (APIs Nova Solutions exposes)

These are implemented in the `controller/` package and callable by Teams 22 & 23.

| Interface | Package | Description |
|-----------|---------|-------------|
| `SMTP_API` | `provided` | Send email notifications; available to all subsystems |
| `PaymentAPI` | `provided` | Process payments (simulated) |
| `I_PUOnlineOrderStatusAPI` | `provided` | Query order status on behalf of SA/CA |
| `I_PUOnlineSalesNotificationAPI` | `provided` | Push sales notifications to SA/CA |
| `ISalesPortal` | `provided` | Internal: catalogue, cart, checkout operations |
| `IMembershipPortal` | `provided` | Internal: registration, login, password management |
| `IPromotionsPortal` | `provided` | Internal: campaign CRUD and promotion display |
| `IReportsPortal` | `provided` | Internal: report generation |

### Required Interfaces (APIs Nova Solutions consumes)

These are implemented as DB adapters in the `impl/` package.

| Interface | Source | Description |
|-----------|--------|-------------|
| `I_CommercialApproval` | IPOS-SA (Team 22) | Submit and check status of commercial membership applications |
| `I_MerchantAccount` | IPOS-SA (Team 22) | Browse product catalogue, place orders, retrieve invoices |
| `I_Inventory` | IPOS-CA (Team 23) | Check real-time stock levels and deduct inventory on order |

### Cross-Subsystem Integration Approach

All inter-subsystem calls are routed through **database adapters** (`impl/` package) that query the `ipos_sa` and `ipos_ca` MySQL schemas directly. This approach earns the 10 cross-subsystem integration marks. The `cross_subsystem_views.sql` script sets up the necessary GRANTs and optional VIEWs.

---

## Use Cases

| Use Case | Description | Owner | Target Sprint |
|----------|-------------|-------|--------------|
| UC-01a | Non-commercial member registration | Hassan | Week 7 |
| UC-01b | Commercial member registration | Hassan / Marwan | Week 7 |
| UC-02 | Member login | Hassan | Week 7 |
| UC-03 | Password reset | Hassan | Week 7 |
| UC-04 | Browse product catalogue | Marwan | Week 8 |
| UC-05 | Search / filter products | Marwan | Week 8 |
| UC-06 | Add product to cart | Alesha | Week 8 |
| UC-07 | Manage cart (update/remove) | Alesha | Week 8 |
| UC-08 | Checkout and place order | Dan + Ergun | Week 8 |
| UC-09 | View order history | Hassan | Week 9 |
| UC-10 | Request refund | Alesha | Week 9 |
| UC-11 | Email notification | Ergun | Week 8 |
| UC-12 | Payment processing | Dan | Week 8 |
| UC-13 | Create promotion campaign | Daanish | Week 9 |
| UC-14 | Edit / deactivate campaign | Daanish | Week 9 |
| UC-15 | View active promotions (member) | Marwan | Week 9 |
| UC-16 | Apply promotion discount to order | Marwan | Week 9 |
| UC-17 | Generate sales report | Alesha | Week 10 |
| UC-18 | Generate campaign performance report | Daanish | Week 10 |
| UC-19 | Generate member engagement report | Team | Week 10 |

---

## Database Design

### IPOS-PU Tables

| Table | Purpose | Key Use Cases |
|-------|---------|--------------|
| `members` | All registered users | UC-01, UC-02, UC-03 |
| `commercial_applications` | Pending/approved commercial membership requests | UC-01b |
| `products` | Pharmaceutical product catalogue (cached from SA) | UC-04, UC-05 |
| `cart_items` | Per-member shopping cart state | UC-06, UC-07 |
| `orders` | Order records and lifecycle state | UC-08, UC-09, UC-10 |
| `order_items` | Line items belonging to each order | UC-08, UC-09 |
| `payments` | Payment records linked to orders | UC-12 |
| `refunds` | Refund requests and status | UC-10 |
| `email_queue` | Store-and-retry queue for failed SMTP sends | UC-11 |
| `promotion_campaigns` | Campaign definitions | UC-13, UC-14 |
| `campaign_products` | Products linked to each campaign | UC-13, UC-16 |
| `campaign_tracking` | Member interactions with campaigns | UC-15, UC-16, UC-19 |
| `admin_users` | Admin accounts for campaign and report management | UC-13–19 |

### Database Configuration

Copy the example config and fill in your credentials:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

`db.properties` structure:

```properties
# IPOS-PU own database
db.url=jdbc:mysql://localhost:3306/ipos_pu
db.username=your_username
db.password=your_password

# Cross-subsystem databases (coordinate URLs with Teams 22 & 23)
db.sa.url=jdbc:mysql://localhost:3306/ipos_sa
db.ca.url=jdbc:mysql://localhost:3306/ipos_ca
```

> `db.properties` is gitignored — never commit credentials.

### Setting Up the Database

```bash
mysql -u root -p < sql/ipos_pu_schema.sql
mysql -u root -p < sql/ipos_pu_seed_data.sql
mysql -u root -p < sql/cross_subsystem_views.sql   # after coordinating with Teams 22/23
```

---

## GUI Screens

All screens are implemented as JavaFX classes in the `gui/` package. The `NavigationBar` component is shared across all screens (Hassan).

| Screen Class | Use Cases | Owner | Sprint |
|-------------|-----------|-------|--------|
| `LoginScreen` | UC-02 | Hassan | Week 7 |
| `RegistrationScreen` | UC-01a, UC-01b | Hassan / Marwan | Week 7 |
| `CatalogueScreen` | UC-04, UC-05 | Marwan | Week 8 |
| `CartScreen` | UC-06, UC-07 | Alesha | Week 8 |
| `CheckoutScreen` | UC-08, UC-12 | Dan + Ergun | Week 8 |
| `OrderHistoryScreen` | UC-09 | Hassan | Week 9 |
| `RefundScreen` | UC-10 | Alesha | Week 9 |
| `CampaignAdminScreen` | UC-13, UC-14 | Daanish | Week 9 |
| `PromotionsScreen` | UC-15, UC-16 | Marwan | Week 9 |
| `ReportsScreen` | UC-17, UC-18, UC-19 | Alesha + Daanish | Week 10 |
| `NavigationBar` | All screens | Hassan | Week 7 |

**GUI rules:** Screens must not contain business logic. All user actions delegate to the relevant controller. Consistent styling (fonts, button styles, error formatting) is required for the 5 GUI consistency marks.

---

## Quick Start

### Prerequisites

- **JDK 17** (not newer — JavaFX 17.0.2 compatibility requirement)
- **Maven 3.8+** (bundled with IntelliJ IDEA)
- **MySQL 8.0**
- **IntelliJ IDEA**

### Setup

```bash
# 1. Clone the repository
git clone <repo-url>
cd TeamProject-IN2033

# 2. Open as Maven project in IntelliJ
#    File → Open → select pom.xml → Open as Project

# 3. Configure database credentials
cp src/main/resources/db.properties.example src/main/resources/db.properties
# Edit db.properties with your local MySQL username and password

# 4. Initialise the database
mysql -u root -p < sql/ipos_pu_schema.sql
mysql -u root -p < sql/ipos_pu_seed_data.sql
```

### Running

| Method | Command |
|--------|---------|
| IntelliJ | Right-click `MainApp.java` → Run |
| Maven | `mvn javafx:run` |
| Tests | `mvn test` |

---

## Git Workflow

- **Always branch from `main`** before starting any task.
- Branch naming: `feature/UC-XX-short-description`
  - Examples: `feature/UC-01a-registration`, `feature/UC-08-checkout`
- **Every team member must commit under their own GitHub account** — this is the individual contribution evidence required by the briefing.
- Open a pull request back to `main` when your feature is ready for review.
- Do not commit `db.properties` or any file containing credentials.

---

## Development Roadmap

### Week 7 — Foundation (Critical Path)

| Task | Owner | Blocks |
|------|-------|--------|
| `DatabaseConnection` singleton | Ergun | Everything else |
| `ipos_pu_schema.sql` | Dan + Daanish | All DAOs |
| `ipos_pu_seed_data.sql` | Daanish | Manual testing |
| `MemberDAO` | Alesha | LoginScreen, RegistrationScreen |
| `LoginScreen` | Hassan | All authenticated screens |
| `RegistrationScreen` | Hassan / Marwan | UC-01a, UC-01b |

### Week 8 — Core Shopping Flow

| Task | Owner |
|------|-------|
| `ProductDAO` | Marwan |
| `CatalogueScreen` | Marwan |
| `CartScreen` | Alesha |
| `OrderDAO` | Dan + Ergun |
| `CheckoutScreen` + `OrderService` | Dan + Ergun |
| `PU_SMTP_API` (email notifications) | Ergun |
| `PU_PaymentAPI` (simulated payment) | Dan |

### Week 9 — Post-Order & Promotions

| Task | Owner |
|------|-------|
| `OrderHistoryScreen` | Hassan |
| `RefundScreen` | Alesha |
| `CampaignDAO` | Daanish |
| `CampaignAdminScreen` | Daanish |
| `PromotionsScreen` | Marwan |
| Cross-subsystem adapters | Ergun (coord. w/ Teams 22/23) |

### Week 10 — Reports, Polish & Integration

| Task | Owner |
|------|-------|
| `ReportDAO` | Alesha |
| `ReportsScreen` | Alesha + Daanish |
| GUI consistency pass | Marwan |
| JUnit test coverage | Alesha + Hassan |
| End-to-end integration testing | Full team |

---

## Team

| Name | Role | Primary Responsibilities |
|------|------|--------------------------|
| **Ergun** | PM / Programmer | `DatabaseConnection`, `PU_SMTP_API`, cross-subsystem coordination, `CheckoutScreen` (DB side) |
| **Dan** | DPM / Programmer | Interface definitions, `OrderService`, `CheckoutScreen` (logic), `PU_PaymentAPI` |
| **Alesha** | Analyst / Tester | `MemberDAO`, `CartScreen`, `RefundScreen`, `ReportsScreen`, JUnit tests |
| **Daanish** | Designer / Analyst | DB schema & seed data, `CampaignDAO`, `CampaignAdminScreen`, campaign & sales reports, VP |
| **Marwan** | Designer / Tester | `CatalogueScreen`, `PromotionsScreen`, `RegistrationScreen`, GUI consistency pass |
| **Hassan** | Programmer / Tester | `LoginScreen`, `RegistrationScreen`, `OrderHistoryScreen`, `NavigationBar` |

---

## Coding Conventions

- **Package:** `com.novasolutions.ipospu`
- **Java style:** Standard Oracle Java naming conventions (camelCase methods, PascalCase classes, UPPER_SNAKE enums)
- **No business logic in GUI classes** — controllers and services only
- **No direct DB calls from controllers or GUI** — DAOs only
- **SQL:** All DDL lives in `sql/`; no inline DDL in Java code
- **Credentials:** Never hardcode database credentials; always read from `db.properties`
- **Tests:** Each service and DAO class should have a corresponding test class under `src/test/`
- **Branching:** One feature branch per use case; merge to `main` via pull request