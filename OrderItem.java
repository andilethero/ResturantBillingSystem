public class OrderItem {
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.menuItem = item;
        this.quantity = quantity;
    }

    public double getTotal() {
        return menuItem.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return quantity + " x " + menuItem.getName() + " @ $" + menuItem.getPrice() +
                " = $" + String.format("%.2f", getTotal());
    }
}
