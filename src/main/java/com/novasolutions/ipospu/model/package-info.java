/**
 * Domain / entity classes for IPOS-PU.
 * <p>
 * Classes here represent the core business objects that map to database tables.
 * They are plain Java objects (POJOs) with fields, getters, setters, and constructors.
 * <p>
 * Planned classes:
 * - Member         (members table - UC-01a/b, UC-02, UC-03)
 * - Product        (products table - UC-04, UC-05, UC-06)
 * - CartItem       (cart_items table - UC-06, UC-07)
 * - Order          (orders table - UC-08, UC-09, UC-10)
 * - PromotionCampaign (promotion_campaigns table - UC-13/14/15/16)
 * <p>
 * Enums:
 * - OrderStatus         (Received, Processing, Shipped, Delivered)
 * - MembershipStatus    (Pending, Approved, Rejected)
 * - ReportType          (Sales, Campaign, Engagement)
 * - MemberType          (NonCommercial, Commercial)
 */
package com.novasolutions.ipospu.model;
