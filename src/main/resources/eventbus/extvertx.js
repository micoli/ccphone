Eu = {};
Eu.sm = {};
Eu.sm.vertx = {};
Eu.sm.vertx.eventbus = function(prm){
	var that=this;
	that.prm = prm;
	//that.eventbus = new vertx.EventBus(that.prm.url);

	return this;
}
function syntaxHighlight(json) {
	if (typeof json != 'string') {
			json = JSON.stringify(json, undefined, 2);
	}
	json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
			var cls = 'number';
			if (/^"/.test(match)) {
					if (/:$/.test(match)) {
							cls = 'key';
					} else {
							cls = 'string';
					}
			} else if (/true|false/.test(match)) {
					cls = 'boolean';
			} else if (/null/.test(match)) {
					cls = 'null';
			}
			return '<span class="' + cls + '">' + match + '</span>';
	});
}
Eu.sm.vertx.eventbus.prototype = {
	onOpen			: Ext.emptyFn,
	onClose			: Ext.emptyFn,
	publish			: function (address,text){
		if(this.eventbus){
			this.eventbus.publish(address,text);
		}
	},
	openConn		: function (address,text){
		if(!this.eventbus){
			this.eventbus = new vertx.EventBus(this.prm.url)
			this.eventbus.onopen = this.prm.onOpen;
			this.eventbus.onclose = this.prm.onClose;
		}
	},
	closeConn		: function() {
		if (this.eventbus) {
			this.eventbus.close();
			delete(this.eventbus)
		}
	},
	subscribe		: function (address,callback){
		if (this.eventbus) {
			this.eventbus.registerHandler(address, callback);
		}
	},

	debug 			: function(str,title) {
		console.log("stompClient",title,str)
	}
}


Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL='http://www.sencha.com/s.gif';
	var that = this;
	that.extEB = new Eu.sm.vertx.eventbus({
		url		: "http://localhost:8080/eventbus",
		onOpen	: function() {
			Ext.getCmp("lblstatus").setText("Connected");
			Ext.getCmp("btnconnect").setDisabled(true);
			Ext.getCmp("btndisconnect").setDisabled(false);

			that.extEB.subscribe("calls",log);
		},
		onClose	: function() {
			Ext.getCmp("lblstatus").setText("disConnected");
			Ext.getCmp("btnconnect").setDisabled(false);
			Ext.getCmp("btndisconnect").setDisabled(true);
			that.extEB.closeConn();
		}
	});

	var log = function(msg, replyTo) {
		console.log(msg);
		addToList($.toJSON(msg),'received');
		if(msg.eventName){
			switch (msg.eventName){
				case "setSipRequest" :
					Ext.getCmp('txtsipcallid').setValue(msg.callId);
				break;
				case "incomingCall" :
					Ext.getCmp('txtsipcallid').setValue(msg.callId);
					Ext.getCmp('txtsipcallidanswer').setValue(msg.callId)
					Ext.getCmp('txtsipcallidbusy').setValue(msg.callId)
					break;
			}
		}
	}

	var eventsStore = new Ext.data.Store({
		reader: new Ext.data.JsonReader({}, [
			'date',
			'title',
			'text'
		]),
		proxy : new Ext.ux.data.PagingMemoryProxy([])
	});

	var extenStore = new Ext.data.Store({
		reader: new Ext.data.JsonReader({}, [
			'exten',
			'data',
			'str',
			'timer'
		]),
		proxy : new Ext.ux.data.PagingMemoryProxy([])
	});

	new Ext.Panel({
		renderTo	: 'mainPanel',
		title		: 'main',
		layout		: 'border',
		height		: 500,
		autoScroll	: true,
		items		: [{
			region		: 'north',
			height		: 300,
			layout		: 'border',
			split		: true,
			tbar		: [{
				xtype		: 'button',
				text		: 'connect',
				id			: 'btnconnect',
				disabled	: false,
				handler		: function(){
					that.extEB.openConn.call(that.extEB);
				}
			},{
				xtype		: 'button',
				text		: 'disconnect',
				id			: 'btndisconnect',
				disabled	: true,
				handler		: function(){
					that.extEB.closeConn.call(that.extEB);
				}
			},{
				xtype		: 'label',
				id			: 'lblstatus',
				width		: 60,
				text		: 'status'
			},'-',{
				xtype		: 'label',
				text		: 'subscribe :'
			},{
				xtype		: 'textfield',
				text		: 'calls',
				id			: 'textsubscribe'
			},{
				xtype		: 'button',
				text		: 'subscribe',
				handler		: function(){
					var address = Ext.getCmp('textsubscribe').getValue();
					that.extEB.subscribe(address);
				}
			},'-',{
				xtype		: 'label',
				text		: 'Send (topic/message)'
			},{
				xtype		: 'textfield',
				value		: 'calls',
				id			: 'topictosend'
			},{
				xtype		: 'textfield',
				id			: 'messagetosend'
			},{
				xtype		: 'button',
				text		: 'send',
				handler		: function(){
					var address = Ext.getCmp('topictosend').getValue();
					var message = Ext.getCmp('messagetosend').getValue();
					if (that.extEB) {
						var json = {
							text : message
						};
						that.extEB.publish(address, json);
						addToList($.toJSON(json),'sent');
					}
				}
			}],
			items		: [{
				xtype			: 'grid',
				region			: 'center',
				ds				: eventsStore,
				autoExpandColumn: 'text',
				cm				: new Ext.grid.ColumnModel([
					{header: 'date'	, width: 100, 	dataIndex: 'date',renderer: Ext.util.Format.dateRenderer('m-d H:i:s')},
					{header: 'title', width: 100, 	dataIndex: 'title'},
					{header: 'text'	,				dataIndex: 'text',id:'text'}
				]),
				listeners :{
					rowclick:function(grid, rowIndex, e) {
						var p = Ext.getCmp('showMsg');
						var data = grid.getStore().data.items[rowIndex].data;
						var obj = JSON.parse(data.text)
						p.tpl.overwrite(p.body,{
							title	: data.title,
							text	: '<pre class="json">'+syntaxHighlight(obj)+'</pre>'
						});
					}
				},
				tbar:[{
					xtype	:'button',
					text	: 'clear',
					handler	: function(){
						eventsStore.removeAll();
					}
				}]
			},{
				xtype	: 'panel',
				id		: 'showMsg',
				region	: 'east',
				width	: "40%",
				split	: true,
				tpl		: new Ext.XTemplate(
					'<tpl for=".">',
					'<div class="search-item">',
						'<h3><span>{title}</span></h3>',
						'<div>{text}</div>',
					'</div></tpl>'
				)
			}]
		},{
			xtype		: 'panel',
			region		: 'center',
			layout		:'table',
			layoutConfig: {
				columns		: 2
			},
			items		: [{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype		: 'label',
					text		: 'call'
				},{
					xtype			: 'textfield',
					value			: '310',
					id				: 'txtcalluri',
					enableKeyEvents	: true,
					listeners		: {
						keyup			 : function(field,e){
							if(e.getCharCode()==13){
								Ext.getCmp('btncall').handler();
							}
						}
					}
				},{
					xtype		: 'button',
					text		: 'call',
					id			: 'btncall',
					handler		: function(){
						that.extEB.publish('guiaction.callAction', {
							uri	: Ext.getCmp('txtcalluri').getValue()
						});
					}
				}]
			},{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype			: 'label',
					text			: 'hangup'
				},{
					xtype			: 'textfield',
					value			: '',
					id				: 'txtsipcallid',
					enableKeyEvents	: true,
					listeners		: {
						keyup			 : function(field,e){
							if(e.getCharCode()==13){
								Ext.getCmp('btnhangup').handler();
							}
						}
					}
				},{
					xtype		: 'button',
					text		: 'hangup',
					id			: 'btnhangup',
					handler		: function(){
						that.extEB.publish('guiaction.hangupAction', {
							sipcallid	: Ext.getCmp('txtsipcallid').getValue()
						});
					}
				}]
			},{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype			: 'label',
					text			: 'answer'
				},{
					xtype			: 'textfield',
					value			: '',
					id				: 'txtsipcallidanswer',
					enableKeyEvents	: true,
					listeners		: {
						keyup			 : function(field,e){
							if(e.getCharCode()==13){
								Ext.getCmp('btnanswer').handler();
							}
						}
					}
				},{
					xtype		: 'button',
					text		: 'answer',
					id			: 'btnanswer',
					handler		: function(){
						that.extEB.publish('guiaction.pickupAction', {
							sipcallid	: Ext.getCmp('txtsipcallidanswer').getValue()
						});
					}
				}]
			},{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype			: 'label',
					text			: 'busy'
				},{
					xtype			: 'textfield',
					value			: '',
					id				: 'txtsipcallidbusy',
					enableKeyEvents	: true,
					listeners		: {
						keyup			 : function(field,e){
							if(e.getCharCode()==13){
								Ext.getCmp('btnbusy').handler();
							}
						}
					}
				},{
					xtype		: 'button',
					text		: 'busy',
					id			: 'btnanswer',
					handler		: function(){
						that.extEB.publish('guiaction.busyHereAction', {
							sipcallid	: Ext.getCmp('txtsipcallidbusy').getValue()
						});
					}
				}]
			},{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype			: 'label',
					text			: 'listCalls'
				},{
					xtype		: 'button',
					text		: 'listCalls',
					id			: 'btnanswer',
					handler		: function(){
						that.extEB.publish('guiaction.listCallsAction', {
							sipcallid	: Ext.getCmp('txtsipcallidbusy').getValue()
						});
					}
				}]
			}]
		}]
	});

	function addToList(text,title){
		eventsStore.insert(0,[new eventsStore.recordType({
			date		: new Date(),
			title		: title,
			text		: text
		})]);
		eventsStore.commitChanges();
	}

	function updateExtenTpl(dt){
		var idx = extenStore.find('exten',dt['subject']);
		if (idx==-1){
			return null;
		}
		return extenStore.getAt(idx);
	}

	that.extEB.openConn();
});
