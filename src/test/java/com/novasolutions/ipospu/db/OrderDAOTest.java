package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.CartItem;
import com.novasolutions.ipospu.model.Member;
import com.novasolutions.ipospu.model.Order;
import com.novasolutions.ipospu.model.Product;
import com.novasolutions.ipospu.service.MembershipService;
import com.novasolutions.ipospu.service.RegistrationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Component tests for the order-status persistence behaviour of IPOS-PU.
 *
 * These tests support the order-status behaviour used by the customer's
 * "My Orders" screen and the admin order-management screen.
 *
 * The tests run against the configured ipos_pu MySQL schema. Each test creates
 * its own timestamped member/order so that the test data is isolated and easy
 * to identify.
 */
public class OrderDAOTest {

    private final OrderDAO orderDAO = new OrderDAO();
    private final MembershipService membershipService = new MembershipService();
    private final MemberDAO memberDAO = new MemberDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private String testEmail;
    private long testMemberId;
    private long createdOrderId = -1;

    @BeforeEach
    void setUp() {
        System.out.println();
        System.out.println("Running IPOS-PU OrderDAO component test...");

        testEmail = "ordertest_" + System.currentTimeMillis() + "@example.com";

        RegistrationResult reg = membershipService.registerNonCommercial("Order Test", testEmail);
        assertTrue(reg.isSuccess(), "Test setup failed: could not register test member");

        Member testMember = memberDAO.findByEmail(testEmail);
        assertNotNull(testMember, "Test setup failed: could not find the test member after registration");

        testMemberId = testMember.id();
        assertTrue(testMemberId > 0, "Test setup failed: test member ID should be valid");

        List<Product> catalogue = productDAO.getAllProducts();
        assertNotNull(catalogue, "Test setup failed: product catalogue should not be null");
        assertFalse(catalogue.isEmpty(), "Test setup failed: product catalogue is empty");

        Product product = catalogue.get(0);
        assertNotNull(product, "Test setup failed: selected product should not be null");

        CartItem item = new CartItem(
                0L,
                testMemberId,
                null,
                product,
                1,
                LocalDateTime.now()
        );

        createdOrderId = orderDAO.createOrder(
                testMemberId,
                null,
                "1 Test Street, London",
                List.of(item),
                product.getPrice(),
                0.0
        );

        assertTrue(createdOrderId > 0, "Test setup failed: could not create seed order");
    }

    @AfterEach
    void cleanup() {
        try (Connection connection = DatabaseConnection.getInstance().getPuConnection()) {
            connection.setAutoCommit(false);

            if (createdOrderId > 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM ipos_pu.order_items WHERE order_id = ?")) {
                    ps.setLong(1, createdOrderId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM ipos_pu.orders WHERE id = ?")) {
                    ps.setLong(1, createdOrderId);
                    ps.executeUpdate();
                }
            }

            if (testEmail != null) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM ipos_pu.members WHERE email = ?")) {
                    ps.setString(1, testEmail);
                    ps.executeUpdate();
                }
            }

            connection.commit();
            System.out.println("Cleanup complete: test member and seed order deleted.");

        } catch (Exception e) {
            System.out.println("Cleanup warning: test data may remain in the database.");
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("CT-OS-01: getAllOrders returns the created order")
    void getAllOrders_returnsCreatedOrder() {
        List<Order> orders = orderDAO.getAllOrders();

        assertNotNull(orders, "getAllOrders should never return null");
        assertFalse(orders.isEmpty(), "getAllOrders should contain at least the order created in setUp");

        boolean foundCreatedOrder = orders.stream()
                .anyMatch(order -> order.getId() == createdOrderId);

        assertTrue(foundCreatedOrder, "The created test order should appear in getAllOrders");

        printPass(
                "CT-OS-01",
                "All orders list retrieved",
                "The created test order was found in the admin order list."
        );
    }

    @Test
    @DisplayName("CT-OS-02: getOrdersForMember returns orders for the correct member")
    void getOrdersForMember_returnsOrdersForThatMember() {
        List<Order> orders = orderDAO.getOrdersForMember(testMemberId);

        assertNotNull(orders, "getOrdersForMember should never return null");
        assertFalse(orders.isEmpty(), "getOrdersForMember should return the created test order");

        boolean foundCreatedOrder = orders.stream()
                .anyMatch(order -> order.getId() == createdOrderId);

        assertTrue(foundCreatedOrder, "The created test order should be returned for the test member");

        boolean allOrdersBelongToMember = orders.stream()
                .allMatch(order -> order.getMemberId() != null && order.getMemberId() == testMemberId);

        assertTrue(allOrdersBelongToMember, "Every returned order should belong to the requested member");

        printPass(
                "CT-OS-02",
                "Member order history retrieved",
                "The test member's order history included the order created during setup."
        );
    }

    @Test
    @DisplayName("CT-OS-03: updateOrderStatus persists the new status")
    void updateOrderStatus_persistsNewStatusInDatabase() {
        Order before = orderDAO.getOrdersForMember(testMemberId).stream()
                .filter(order -> order.getId() == createdOrderId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seed order missing before status update"));

        assertTrue(
                sameStatus(before.getStatus(), "RECEIVED"),
                "Newly created orders should start in RECEIVED state"
        );

        orderDAO.updateOrderStatus(createdOrderId, "PROCESSING");

        Order after = orderDAO.getOrdersForMember(testMemberId).stream()
                .filter(order -> order.getId() == createdOrderId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seed order missing after status update"));

        assertTrue(
                sameStatus(after.getStatus(), "PROCESSING"),
                "updateOrderStatus should persist the new PROCESSING status"
        );

        printPass(
                "CT-OS-03",
                "Order status updated",
                "The order status changed from RECEIVED to PROCESSING and was saved in the database."
        );
    }

    private boolean sameStatus(String actual, String expected) {
        return actual != null && actual.trim().equalsIgnoreCase(expected.trim());
    }

    private void printPass(String testId, String testName, String description) {
        System.out.println("✅ PASS " + testId + " - " + testName);
        System.out.println("  " + description);
        System.out.println("  DAO tested: OrderDAO");
        System.out.println("  Database behaviour tested: order retrieval and order status persistence");
    }
}