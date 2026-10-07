/**
 * Interfaces PROVIDED by IPOS-PU to other subsystems.
 * <p>
 * These are the APIs that IPOS-PU exposes. Teams 22 (IPOS-SA) and 23 (IPOS-CA)
 * can call these methods to access IPOS-PU functionality.
 * <p>
 * IMPORTANT: These interfaces must match the Week 5 deliverable EXACTLY.
 * The briefing says 'interfaces should be retained with no changes.'
 * <p>
 * Provided interfaces:
 * - SMTP_API                        (email sending - available to all subsystems)
 * - PaymentAPI                      (payment processing)
 * - I_PUOnlineOrderStatusAPI        (order status queries from SA/CA)
 * - I_PUOnlineSalesNotificationAPI  (sales notifications to SA/CA)
 * <p>
 * Internal interfaces (not cross-subsystem, but structure the app):
 * - ISalesPortal
 * - IMembershipPortal
 * - IPromotionsPortal
 * - IReportsPortal
 */
package com.novasolutions.ipospu.interfaces.provided;
