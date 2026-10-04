export interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
}

export interface User {
  id: number;
  email: string;
  name: string;
  address: string;
}

export interface Order {
  id: string;
  userId: number;
  productId: number;
  quantity: number;
  status: string;
  createdAt: string;
}

export interface AuthUser {
  userId: number;
  name: string;
  token: string;
}