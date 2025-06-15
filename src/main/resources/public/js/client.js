const { createApp } = Vue;
const { Quasar } = window;

createApp({
  data() {
    return {
      socket: null,
      playerName: '',
      connected: false,
      ready: false,
      gameStarted: false,
      deckCount: 0,
      players: [],
      waiting: [],
      hand: [],
      topCard: '',
      messages: [],
      chats: [],
      chatInput: '',
      colorSelectVisible: false,
      currentPlayer: '',
      canSkip: false,
      autoplay: false
      };
    },
    methods: {
    connect() {
      this.socket = io('http://localhost:9092');
      this.socket.on('connect', () => {
        this.socket.emit('join', this.playerName);
        this.connected = true;
      });
      this.socket.on('message', msg => {
        if (typeof msg === 'string') {
          this.messages.push({ text: msg, cards: [] });
        } else {
          this.messages.push(msg);
        }
      });
      this.socket.on('state', state => {
        this.gameStarted = true;
        this.deckCount = state.deck;
        this.topCard = state.topCard;
        this.players = state.players;
        this.hand = state.hand;
        this.currentPlayer = state.current;
      });
      this.socket.on('waiting', info => {
        this.waiting = info;
      });
      this.socket.on('start', () => {
        this.gameStarted = true;
      });
      this.socket.on('requestPlay', hasPlayable => {
        this.canSkip = !hasPlayable;
        if (this.autoplay) {
          const idx = this.autoChooseCard();
          this.socket.emit('play', idx);
          this.canSkip = false;
        }
      });
      this.socket.on('requestColor', () => {
        this.colorSelectVisible = true;
        if (this.autoplay) {
          const color = this.autoChooseColor();
          this.socket.emit('color', color);
          this.colorSelectVisible = false;
        }
      });
      this.socket.on('noPlayable', msg => {
        this.messages.push({ text: msg, cards: [] });
        this.canSkip = true;
      });
      this.socket.on('drawnCards', payload => {
        this.messages.push({ text: payload.text, cards: payload.cards });
      });
      this.socket.on('chat', msg => {
        this.chats.push(msg);
      });
    },
    sendReady() {
      if (this.socket) {
        this.socket.emit('ready', 'ready');
        this.ready = true;
      }
    },
    playCard(idx) {
      if (this.socket) {
        this.socket.emit('play', idx);
        this.canSkip = false;
      }
    },
    skipTurn() {
      if (this.socket) {
        this.socket.emit('play', -1);
        this.canSkip = false;
      }
    },
    chooseColor(color) {
      if (this.socket) {
        this.socket.emit('color', color);
        this.colorSelectVisible = false;
      }
    },
    sendChat() {
      if (this.socket && this.chatInput.trim()) {
        this.socket.emit('chat', this.chatInput.trim());
        this.chatInput = '';
      }
    },
    toggleAutoplay() {
      this.autoplay = !this.autoplay;
    },
    autoChooseCard() {
      for (let i = 0; i < this.hand.length; i++) {
        if (this.isPlayable(this.hand[i], this.topCard)) {
          return i;
        }
      }
      return -1;
    },
    isPlayable(card, top) {
      if (!top) return true;
      const [cColor, ...cRest] = card.split('_');
      const [tColor, ...tRest] = top.split('_');
      const cValue = cRest.join('_');
      const tValue = tRest.join('_');
      if (cColor === 'WILD') return true;
      return cColor === tColor || cValue === tValue;
    },
    autoChooseColor() {
      const counts = { RED:0, BLUE:0, GREEN:0, YELLOW:0 };
      this.hand.forEach(h => {
        const col = h.split('_')[0];
        if (counts[col] !== undefined) counts[col]++;
      });
      return Object.entries(counts).sort((a,b) => b[1]-a[1])[0][0] || 'RED';
    },
    cardColor(card) {
      const color = card.split('_')[0];
      switch (color) {
        case 'RED':
        case 'BLUE':
        case 'GREEN':
        case 'YELLOW':
          return color.toLowerCase();
        default:
          return 'black';
      }
    },
    displayLabel(card) {
      const parts = card.split('_');
      return parts.slice(1).join(' ');
    },
    topCardLabel() {
      if (!this.topCard) return '';
      return this.displayLabel(this.topCard);
    },
    topCardColor() {
      if (!this.topCard) return 'grey';
      return this.cardColor(this.topCard);
    }
  }
}).use(Quasar).mount('#q-app');
