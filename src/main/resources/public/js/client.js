const { createApp } = Vue;
const { Quasar } = window;

createApp({
  data() {
    return {
      socket: null,
      playerName: '',
      connected: false,
      ready: false,
      deckCount: 0,
      players: [],
      hand: [],
      topCard: '',
      messages: [],
      colorSelectVisible: false,
      currentPlayer: '',
      canSkip: false
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
        this.messages.push(msg);
      });
      this.socket.on('state', state => {
        this.deckCount = state.deck;
        this.topCard = state.topCard;
        this.players = state.players;
        this.hand = state.hand;
        this.currentPlayer = state.current;
      });
      this.socket.on('requestPlay', hasPlayable => {
        this.canSkip = !hasPlayable;
      });
      this.socket.on('requestColor', () => {
        this.colorSelectVisible = true;
      });
      this.socket.on('noPlayable', msg => {
        this.messages.push(msg);
        this.canSkip = true;
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
