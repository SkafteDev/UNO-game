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
      colorSelectVisible: false
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
      });
      this.socket.on('requestPlay', () => {
        // highlight playable cards if desired
      });
      this.socket.on('requestColor', () => {
        this.colorSelectVisible = true;
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
      }
    },
    chooseColor(color) {
      if (this.socket) {
        this.socket.emit('color', color);
        this.colorSelectVisible = false;
      }
    }
  }
}).use(Quasar).mount('#q-app');
