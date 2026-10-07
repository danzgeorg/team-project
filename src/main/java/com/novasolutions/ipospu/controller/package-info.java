/**
 * Controller classes that implement the PROVIDED interfaces.
 * <p>
 * Controllers sit between the GUI and the Service layer. Each controller
 * implements one of the provided interfaces and delegates business logic
 * to the corresponding Service class.
 * <p>
 * Flow: GUI screen -> Controller -> Service -> DAO -> Database
 * <p>
 * Planned classes:
 * - SalesController        (implements ISalesPortal - catalogue, cart, checkout)
 * - MembershipController   (implements IMembershipPortal - registration, login, password)
 * - PromotionsController   (implements IPromotionsPortal - campaigns, discounts)
 * - ReportsController      (implements IReportsPortal - sales/campaign/engagement reports)
 */
package com.novasolutions.ipospu.controller;
