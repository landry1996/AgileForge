export interface Invitation {
  id: string;
  organizationId: string;
  email: string;
  role: string;
  status: 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';
  invitedBy: string;
  expiresAt: string;
  createdAt: string;
}

export interface InviteRequest {
  email: string;
  role?: string;
}
