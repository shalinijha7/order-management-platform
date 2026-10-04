import { useEffect, useState } from 'react';
import { api } from './api';
import { Order, Product, User } from './types';

export default function App() {
  const [products, setProducts] = useState<Product[]>([]);
  const [user, setUser] = useState<User | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [status, setStatus] = useState<string>('');

  const [form, setForm] = useState({ email: '', name: '', address: '', password: '' });
  const [selectedProduct, setSelectedProduct] = useState<number | null>(null);
  const [quantity, setQuantity] = useState(1);

  const [notifications, setNotifications] = useState<string[]>([]);
  const [seenCount, setSeenCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const unreadCount = Math.max(notifications.length - seenCount, 0);

  useEffect(() => {
    api.getProducts().then(setProducts).catch((e) => setStatus(e.message));
  }, []);

  // Poll notification-service (via the gateway) every 5s so new Kafka-driven
  // notifications show up without needing a page refresh.
  useEffect(() => {
    const fetchNotifications = () => {
      api.getNotifications().then(setNotifications).catch(() => {});
    };
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 5000);
    return () => clearInterval(interval);
  }, []);

  function toggleNotifications() {
    setShowNotifications((prev) => {
      const next = !prev;
      if (next) setSeenCount(notifications.length); // mark all as read on open
      return next;
    });
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    try {
      const newUser = await api.registerUser(form);
      setUser(newUser);
      setStatus(`Registered as ${newUser.name}`);
      refreshOrders(newUser.id);
    } catch (err: any) {
      setStatus(err.message);
    }
  }

  async function handlePlaceOrder() {
    if (!user || !selectedProduct) return;
    try {
      await api.placeOrder({ userId: user.id, productId: selectedProduct, quantity });
      setStatus('Order placed successfully');
      api.getProducts().then(setProducts);
      refreshOrders(user.id);
    } catch (err: any) {
      setStatus(err.message);
    }
  }

  function refreshOrders(userId: number) {
    api.getOrdersForUser(userId).then(setOrders).catch(() => {});
  }

  return (
    <div className="page">
            <header className="header">
              <div className="header-row">
                <div>
                  <h1>Order Platform</h1>
                  <p className="subtitle">Demo storefront calling the microservices via the API Gateway</p>
                </div>

                <div className="bell-wrap">
                  <button
                    className="bell-button"
                    onClick={toggleNotifications}
                    aria-label="Notifications"
                  >
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
                      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                    </svg>
                    {unreadCount > 0 && <span className="bell-badge">{unreadCount}</span>}
                  </button>

                  {showNotifications && (
                    <div className="bell-dropdown">
                      <h3>Notifications</h3>
                      {notifications.length === 0 ? (
                        <p className="empty">No notifications yet — place an order to see one.</p>
                      ) : (
                        <ul className="notification-list">
                          {[...notifications].reverse().map((n, i) => (
                            <li key={i}>{n}</li>
                          ))}
                        </ul>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </header>

      {!user ? (
        <form className="card" onSubmit={handleRegister}>
          <h2>Register</h2>
          <label>
            Name
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </label>
          <label>
            Email
            <input
              required
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </label>
          <label>
            Address
            <input
              required
              value={form.address}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
            />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              minLength={6}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </label>
          <button type="submit">Create account</button>
        </form>
      ) : (
        <div className="card">
          <h2>Welcome, {user.name}</h2>
          <p>User ID: {user.id}</p>
        </div>
      )}

      <div className="card">
        <h2>Products</h2>
        <table>
          <thead>
            <tr>
              <th></th>
              <th>Name</th>
              <th>Price</th>
              <th>In stock</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td>
                  <input
                    type="radio"
                    name="product"
                    checked={selectedProduct === p.id}
                    onChange={() => setSelectedProduct(p.id)}
                  />
                </td>
                <td>{p.name}</td>
                <td>₹{p.price}</td>
                <td>{p.stock}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="order-form">
          <label>
            Quantity
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
            />
          </label>
          <button disabled={!user || !selectedProduct} onClick={handlePlaceOrder}>
            Place order
          </button>
        </div>
      </div>

      {status && <p className="status">{status}</p>}

      {orders.length > 0 && (
        <div className="card">
          <h2>Your orders</h2>
          <ul>
            {orders.map((o) => (
              <li key={o.id}>
                Order {o.id.slice(-6)} — product #{o.productId}, qty {o.quantity} — {o.status}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
