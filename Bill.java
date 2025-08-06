import java.util.ArrayList;
import java.util.List;

public class Bill {
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public double getSubtotal() {
        return orderItems.stream().mapToDouble(OrderItem::getTotal).sum();
    }

    public double getVAT() {
        return getSubtotal() * 0.12; // 12% VAT
    }

    public double getTotal() {
        return getSubtotal() + getVAT();
    }
}
