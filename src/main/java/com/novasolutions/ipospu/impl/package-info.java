/**
 * External API adapters and shared database adapters.
 * <p>
 * Classes here implement the REQUIRED interfaces by querying the shared
 * MySQL database (IPOS-SA and IPOS-CA schemas). They also implement
 * the PROVIDED interfaces for external services (SMTP, Payment).
 * <p>
 * Cross-subsystem adapters (worth 10 demo marks):
 * - InventoryDBAdapter           (implements I_Inventory via IPOS-CA stock tables)
 * - MerchantAccountDBAdapter     (implements I_MerchantAccount via IPOS-SA catalogue tables)
 * - CommercialApprovalDBAdapter  (implements I_CommercialApproval via IPOS-SA application tables)
 * <p>
 * External service implementations:
 * - PU_SMTP_API       (simulated email - logs to email_queue table on failure)
 * - PU_PaymentAPI     (simulated payment - success/failure based on test card numbers)
 */
package com.novasolutions.ipospu.impl;
