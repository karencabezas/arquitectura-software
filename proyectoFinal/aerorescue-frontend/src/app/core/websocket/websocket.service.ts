import { Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { KafkaEvent } from '../../shared/models/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebSocketService {

  private client!: Client;
  private connected = signal(false);

  readonly emergencyEvents$ = new Subject<KafkaEvent>();
  readonly missionEvents$   = new Subject<KafkaEvent>();
  readonly droneEvents$     = new Subject<KafkaEvent>();
  readonly alertEvents$     = new Subject<KafkaEvent>();

  readonly isConnected = this.connected.asReadonly();

  connect(token: string): void {
    // Use relative URL when running through nginx proxy (production)
    // Use absolute URL with wsUrl when running in dev mode
    const wsBase = environment.wsUrl || window.location.origin;
    const wsEndpoint = `${wsBase}/ws`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsEndpoint),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.connected.set(true);
        this.subscribeTopics();
      },
      onDisconnect: () => this.connected.set(false),
      onStompError: (frame) => console.error('WebSocket STOMP error:', frame)
    });
    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.connected.set(false);
  }

  private subscribeTopics(): void {
    this.client.subscribe('/topic/emergencies', (msg: IMessage) => {
      this.emergencyEvents$.next(JSON.parse(msg.body));
    });
    this.client.subscribe('/topic/missions', (msg: IMessage) => {
      this.missionEvents$.next(JSON.parse(msg.body));
    });
    this.client.subscribe('/topic/drones', (msg: IMessage) => {
      this.droneEvents$.next(JSON.parse(msg.body));
    });
    this.client.subscribe('/topic/alerts', (msg: IMessage) => {
      this.alertEvents$.next(JSON.parse(msg.body));
    });
  }
}
