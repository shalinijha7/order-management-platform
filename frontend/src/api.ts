import { Order, Product, User, AuthUser } from './types';

// All calls go through the API Gateway at /api/* (proxied to localhost:8080 in dev).
const BASE = '/api';

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `Request failed with status ${res.status}`);
  }
  return res.json();
}

export const api = {
  getProducts: (): Promise<Product[]> =>
    fetch(`${BASE}/products`).then((r) => handle(r)),

    registerUser: (data: { email: string; name: string; address: string; password: string }): Promise<AuthUser> =>
      fetch(`${BASE}/users`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      }).then((r) => handle(r)),

  placeOrder: (data: { userId: number; productId: number; quantity: number }): Promise<Order> =>
    fetch(`${BASE}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    }).then((r) => handle(r)),

  getOrdersForUser: (userId: number): Promise<Order[]> =>
    fetch(`${BASE}/orders/user/${userId}`).then((r) => handle(r)),

  getNotifications: (): Promise<string[]> =>
      fetch(`${BASE}/notifications`).then((r) => handle(r)),
};
