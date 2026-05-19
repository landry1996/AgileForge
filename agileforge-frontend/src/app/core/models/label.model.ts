export interface Label {
  id: string;
  projectId: string;
  name: string;
  color: string;
  description?: string;
  createdAt: string;
}

export interface CreateLabelRequest {
  name: string;
  color?: string;
  description?: string;
}

export interface Attachment {
  id: string;
  ticketId: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  uploadedBy: string;
  createdAt: string;
  downloadUrl?: string;
}
