// ===== Auth Models =====
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  preAuthToken: string;
  mfaRequired: boolean;
  message: string;
}

export interface MfaValidateRequest {
  preAuthToken: string;
  totpCode: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  roles: string[];
  permissions: string[];
}

export interface MfaSetupResponse {
  qrImageUri: string;
  secret: string;
  message: string;
}

export interface CurrentUser {
  id: number;
  email: string;
  roles: string[];
  permissions: string[];
}

// ===== RBAC Models =====
export interface Rol {
  id: number;
  nombre: string;
  descripcion: string;
  permisos: Permiso[];
}

export interface Permiso {
  id: number;
  nombre: string;
  descripcion: string;
}

export interface Usuario {
  id: number;
  email: string;
  mfaEnabled: boolean;
  activo: boolean;
  roles: Rol[];
}

// ===== Product Models =====
export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
  ownerId: number;
  activo: boolean;
}

export interface ProductoRequest {
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
}
