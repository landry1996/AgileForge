import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Board } from '../models/board.model';
import { Ticket } from '../models/ticket.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BoardService {
  private readonly apiUrl = `${environment.apiUrl}/board`;

  constructor(private http: HttpClient) {}

  getBoard(projectId: string): Observable<Board> {
    return this.http.get<Board>(`${this.apiUrl}/project/${projectId}`);
  }

  moveTicket(ticketId: string, status: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/tickets/${ticketId}/move?status=${status}`, {});
  }

  addColumn(projectId: string, name: string, mappedStatus: string, position: number, wipLimit?: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/project/${projectId}/columns`, { name, mappedStatus, position, wipLimit });
  }

  removeColumn(columnId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/columns/${columnId}`);
  }

  getBacklog(projectId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/project/${projectId}/backlog`);
  }
}
