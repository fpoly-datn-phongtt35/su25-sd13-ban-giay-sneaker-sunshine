// src/utils/ws.ts
import { Client, IMessage, StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs.js'

export type MessageHandler = (msg: any) => void

export interface WsOptions {
  baseUrl?: string            // http://localhost:8080
  endpoint?: string           // /ws
  getToken?: () => string | null | undefined
  onConnect?: () => void
  onError?: (err: any) => void
}

export class WsClient {
  private client: Client | null = null
  private subs: StompSubscription[] = []
  private custSub: StompSubscription | null = null
  private currentCustTopic: string | null = null
  private userHandler: MessageHandler | null = null
  private custHandler: MessageHandler | null = null

  constructor(private opts: WsOptions) {}

  activate() {
    if (this.client && this.client.active) return
    const base = this.opts.baseUrl || (import.meta.env.VITE_API_BASE || 'http://localhost:8080')
    const endpoint = this.opts.endpoint || '/ws'

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${base}${endpoint}`),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      connectHeaders: {},
      beforeConnect: (c) => {
        const t = this.opts.getToken?.()
        c.connectHeaders = t ? { Authorization: `Bearer ${t}` } : {}
      }
    })

    this.client.onConnect = () => {
      // sub kênh riêng tư
      if (this.userHandler) {
        const s1 = this.client!.subscribe('/user/queue/notifications', (m: IMessage) => {
          this.safePush(this.userHandler!, m.body)
        })
        this.subs.push(s1)
      }
      this.opts.onConnect?.()
    }

    this.client.onStompError = (f) => this.opts.onError?.(f)
    this.client.onWebSocketError = (e) => this.opts.onError?.(e)

    this.client.activate()
  }

  deactivate() {
    if (!this.client) return
    try { this.custSub?.unsubscribe() } catch {}
    this.custSub = null
    this.currentCustTopic = null
    for (const s of this.subs) { try { s.unsubscribe() } catch {} }
    this.subs = []
    this.client.deactivate()
    this.client = null
  }

  onUserQueue(handler: MessageHandler) {
    this.userHandler = handler
  }

  onCustomerTopic(handler: MessageHandler) {
    this.custHandler = handler
  }

  subscribeCustomerTopic(customerId?: number | string | null) {
    if (!this.client?.connected || !customerId) return
    const topic = `/topic/customer.${customerId}`
    if (this.currentCustTopic === topic) return
    try { this.custSub?.unsubscribe() } catch {}
    this.custSub = this.client.subscribe(topic, (m: IMessage) => this.safePush(this.custHandler!, m.body))
    this.currentCustTopic = topic
  }

  private safePush(handler: MessageHandler, body: any) {
    try {
      const data = typeof body === 'string' ? JSON.parse(body) : body
      handler?.(data)
    } catch (e) {
      console.warn('[WS] invalid payload', body)
    }
  }
}
