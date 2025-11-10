// InvoiceSRPOCP.java
// Messy starter: Monolith Invoice Service (violates SRP + OCP)

import java.util.*;
import java.io.*;
import java.math.*;

class LineItem {
    String sku;
    int quantity;
    double unitPrice;

    LineItem(String sku, int quantity, double unitPrice) {
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}

class PDFService{

    Invoice invoice;

    PDFService(Invoice invoice) {
        this.invoice = invoice;
    }

    String RenderPDF() {

        StringBuilder pdf = new StringBuilder();
        pdf.append("INVOICE\n");
        for (LineItem it : invoice.getItems()) {
            pdf.append(it.sku).append(" x").append(it.quantity).append(" @ ").append(it.unitPrice).append("\n");
        }
        pdf.append("Subtotal: ").append(invoice.getSubTotal()).append("\n")
                .append("Discounts: ").append(invoice.discountTotal).append("\n")
                .append("Tax: ").append(invoice.getTax()).append("\n")
                .append("Total: ").append(invoice.getGrand()).append("\n");

        return pdf.toString();
    }

}

class EmailService {

    Invoice invoice;

    EmailService(Invoice invoice) {
        this.invoice = invoice;
    }

    void sendEmail() {
        // email I/O inline (tight coupling)
        if (invoice.email != null && !invoice.email.isEmpty()) {
            System.out.println("[SMTP] Sending invoice to " + invoice.email + "...");
        }

        // logging inline
        System.out.println("[LOG] Invoice processed for " + invoice.email + " total=" + invoice.grand);
    }

}

class Invoice {
    List<LineItem> items;
    Map<String, Double> discounts;
    String email;
    double subTotal;
    double discountTotal;
    double tax;
    double grand;

    public List<LineItem> getItems() {
        return items;
    }

    public Map<String, Double> getDiscounts() {
        return discounts;
    }

    public String getEmail() {
        return email;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public double getDiscountTotal() {
        return discountTotal;
    }

    public double getTax() {
        return tax;
    }

    public double getGrand() {
        return grand;
    }

    Invoice (List<LineItem> items, Map<String, Double> discounts, String email) {
        this.items = items;
        this.discounts = discounts;
        this.email = email;
    }

    double calculateSubtotal() {
        // pricing
        double subtotal = 0.0;
        for (LineItem it : items) subtotal += it.unitPrice * it.quantity;
        return  subtotal;
    }

    double calculateDiscount(double subtotal) {
        // discounts (tightly coupled)
        double discountTotal = 0.0;
        for (Map.Entry<String, Double> e : discounts.entrySet()) {
            String k = e.getKey();
            double v = e.getValue();
            if (k.equals("percent_off")) {
                discountTotal += subtotal * (v / 100.0);
            } else if (k.equals("flat_off")) {
                discountTotal += v;
            } else {
                // unknown ignored
            }
        }
        return discountTotal;
    }

    double calculateTax(double subtotal, double discountTotal) {
        return (subtotal - discountTotal) * 0.18;
    }

    double calculateGrand(double subtotal, double discountTotal, double tax) {
        return subtotal - discountTotal + tax;
    }


    void generateInvoice() {

        this.subTotal = calculateSubtotal();

        this.discountTotal = calculateDiscount(this.subTotal);

        this.tax = calculateTax(this.subTotal, this.discountTotal);

        grand = calculateGrand(this.subTotal, this.discountTotal, this.tax);
    }
}

class InvoiceService {

    String getInvoice(List<LineItem> items, Map<String, Double> discounts, String email) {

        Invoice invoice = new Invoice(items, discounts, email);

        invoice.generateInvoice();

        PDFService pdfService = new PDFService(invoice);

        String rendered = pdfService.RenderPDF();

        EmailService emailService = new EmailService(invoice);

        emailService.sendEmail();

        return rendered;
    }
}

public class InvoiceSRPOCP {
    public static void main(String[] args) {
        InvoiceService svc = new InvoiceService();
        List<LineItem> items = Arrays.asList(
                new LineItem("BOOK-001", 2, 500.0),
                new LineItem("USB-DRIVE", 1, 799.0)
        );
        Map<String, Double> discounts = new HashMap<>();
        discounts.put("percent_off", 10.0);

        System.out.println(svc.getInvoice(items, discounts, "customer@example.com"));
    }
}
