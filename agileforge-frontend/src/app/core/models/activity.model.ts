export interface Activity {
  id: string;
  ticketId: string;
  ticketKey: string;
  userId: string;
  userName: string;
  action: string;
  field: string;
  oldValue?: string;
  newValue?: string;
  createdAt: string;
}

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  displayName?: string;
  avatarUrl?: string;
  phone?: string;
  timezone?: string;
  locale?: string;
  emailVerified: boolean;
  lastLoginAt?: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  displayName?: string;
  avatarUrl?: string;
  phone?: string;
  timezone?: string;
  locale?: string;
}
