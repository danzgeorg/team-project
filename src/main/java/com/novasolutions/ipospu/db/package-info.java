/**
 * Database access layer (Data Access Objects).
 * <p>
 * Each DAO class handles CRUD operations for one database table.
 * All DAOs depend on DatabaseConnection to get a JDBC connection.
 * <p>
 * Planned classes:
 * - DatabaseConnection  (JDBC singleton - Ergun, Week 7, CRITICAL PATH)
 * - MemberDAO           (members table - Alesha, Week 7)
 * - ProductDAO          (products table - Marwan, Week 8)
 * - OrderDAO            (orders + order_items tables - Dan + Ergun, Week 8)
 * - CampaignDAO         (promotion_campaigns + campaign_products tables - Daanish, Week 9)
 * - ReportDAO           (read-only queries for UC-17/18/19 - Week 10)
 */
package com.novasolutions.ipospu.db;
