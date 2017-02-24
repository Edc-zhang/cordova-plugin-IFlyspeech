/**
 * Created by Edc.zhang on 2017/2/13.
 */
var cordova = require('cordova'),
    channel = require('cordova/channel'),
    exec = require('cordova/exec');

var Speech = function() {
    this.channels = {
        'SyncContact': channel.create('SyncContact'),
        'UpdateUserWord': channel.create('UpdateUserWord'),
        'SpeechError': channel.create('SpeechError'),
        'SpeechResults': channel.create('SpeechResults'),
        'VolumeChanged': channel.create('VolumeChanged'),
        'SpeechBegin': channel.create('SpeechBegin'),
        'SpeechEnd': channel.create('SpeechEnd'),
        'SpeechCancel': channel.create('SpeechCancel'),
        'SpeakCompleted': channel.create('SpeakCompleted'),
        'SpeakBegin': channel.create('SpeakBegin'),
        'SpeakProgress': channel.create('SpeakProgress'),
        'SpeakPaused': channel.create('SpeakPaused'),
        'SpeakResumed': channel.create('SpeakResumed'),
        'SpeakCancel': channel.create('SpeakCancel'),
        'BufferProgress': channel.create('BufferProgress')
    };
    this.voice_names = {
        'xiaoyan' : '小燕',
        'xiaoyu' : '小宇',
        'vixy' : '小研',
        'vixq' : '小琪',
        'vixf' : '小峰',
        'vixm' : '香港小梅',
        'vixl' : '台湾小莉',
        'vixr' : '四川妹纸',
        'vixyun' : '东北小芸',
        'vixk' : '河南小坤',
        'vixqa' : '湖南小强',
        'vixying' : '陕西小莹',
        'vixx' : '蜡笔小新',
        'vinn' : '楠楠',
        'vils' : '孙大爷',
        'Catherine' : '美国Catherine',
        'henry' : '美国Henry',
        'vimary' : '英国Mary',
        'Mariane' : '法国Mariane',
        'Guli' : '维族Guli',
        'Allabent' : '俄国Allabent',
        'Gabriela' : '西班牙Gabriela',
        'Abha' : '印度Abha',
        'XiaoYun' : '越南XiaoYun'
    };
    this.login();
    this.msg = "";
};

Speech.prototype = {

    _eventHandler: function(info) {
        if (info.event in this.channels) {
            this.channels[info.event].fire(info);
        }
    },

    addEventListener: function(event, f, c) {
        if (event in this.channels) {
            this.channels[event].subscribe(f, c || this);
        }
    },

    removeEventListener: function(event, f) {
        if (event in this.channels) {
            this.channels[event].unsubscribe(f);
        }
    },

    login: function() {
        // closure variable for local function to use
        var speech = this;

        // the callback will be saved in the session for later use
        var callback = function(info) {
            speech._eventHandler(info);
        };
        exec(callback, callback, 'Speech', 'login', []);

        function parseResults( e ) {
            var data = JSON.parse( e.results );
            if(data.sn == 1) speech.msg = "";
            var ws = data.ws;
            for( var i=0; i<ws.length; i++ ) {
                var word = ws[i].cw[0].w;
                speech.msg += word;
            }
            if(data.ls == true) {
                console.log( speech.msg );
                if(typeof speech.onspeakcallback === 'function') {
                    speech.onspeakcallback( speech.msg );
                }
            }
        }
        this.addEventListener('SpeechResults', parseResults );

    },


   startListen: function(func,fail,isShow,isShowPunc) {
        this.onspeakcallback = func;
               exec(null, null, 'Speech', 'startListening', [{language:'zh_cn', accent:'mandarin'},isShow,isShowPunc]);
    },

    stopListen: function() {
        exec(null, null, 'Speech', 'stopListening', []);
    },

    cancelListening: function() {
        exec(null, null, 'Speech', 'cancelListening', []);
    },

    startSpeak: function(success,error,text) {
        exec(null, null, 'Speech', 'startSpeaking', [text, {voice_name: 'xiaoyan'}]);
    },

    pauseSpeaking: function() {
        exec(null, null, 'Speech', 'pauseSpeaking', []);
    },

    resumeSpeaking: function() {
        exec(null, null, 'Speech', 'resumeSpeaking', []);
    },

    stopSpeak: function() {
        exec(null, null, 'Speech', 'stopSpeaking', []);
    }

};

module.exports = new Speech();
