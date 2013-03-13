Eu = {};
Eu.sm = {};
Eu.sm.vertx = {};
Eu.sm.vertx.eventbus = function(prm){
	var that=this;
	that.prm = prm;
	that.eventbus = new vertx.EventBus(that.prm.url);

	that.eventbus.onopen = that.prm.onOpen;
	that.eventbus.onclose = that.prm.onClose;
	return this;
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
		}
	},
	closeConn		: function() {
		if (this.eventbus) {
			this.eventbus.close();
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
	var SipCallId = 0;
	var log = function(msg, replyTo) {
		console.log(msg);
		addToList($.toJSON(msg),'received');
		if(msg.eventName){
			switch (msg.eventName){
				case "setSipRequest" :
					SipCallId = msg.SipCallId
					Ext.getCmp('txtsipcallid').setValue(msg.SipCallId);
				break;
			}
		}
	}
	var extEB = new Eu.sm.vertx.eventbus({
		url		: "http://localhost:8080/eventbus",
		onOpen	: function() {
			Ext.getCmp("lblstatus").setText("Connected");
			Ext.getCmp("btnconnect").setDisabled(true);
			Ext.getCmp("btndisconnect").setDisabled(false);

			extEB.subscribe("topic",log);
			extEB.subscribe("guiaction.testClick",log);
		},
		onClose	: function() {
			Ext.getCmp("lblstatus").setText("disConnected");
			Ext.getCmp("btnconnect").setDisabled(false);
			Ext.getCmp("btndisconnect").setDisabled(true);
			extEb.eventbus = null;
		}
	});

	extEB.openConn();

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
				handler		: extEB.openConn
			},{
				xtype		: 'button',
				text		: 'disconnect',
				id			: 'btndisconnect',
				disabled	: true,
				handler		: extEB.closeConn
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
				text		: 'topic',
				id			: 'textsubscribe'
			},{
				xtype		: 'button',
				text		: 'subscribe',
				handler		: function(){
					var address = Ext.getCmp('textsubscribe').getValue();
					extEB.subscribe(address);
				}
			},'-',{
				xtype		: 'label',
				text		: 'Send (topic/message)'
			},{
				xtype		: 'textfield',
				value		: 'topic',
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
					if (extEB) {
						var json = {
							text : message
						};
						extEB.publish(address, json);
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
					{header: 'title', width: 200, 	dataIndex: 'title'},
					{header: 'text'	,				dataIndex: 'text',id:'text'}
				]),
				listeners :{
					rowclick:function(grid, rowIndex, e) {
						var p = Ext.getCmp('showMsg');
						var data = grid.getStore().data.items[rowIndex].data;
						p.tpl.overwrite(p.body,{
							title	: data.title,
							text	: data.text
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
				width	: 400,
				split	: true,
				tpl		: new Ext.XTemplate(
					'<tpl for=".">',
					'<div class="search-item">',
						'<h3><span>{title}</span></h3>',
						'<code>{text}</code>',
					'</div></tpl>'
				)
			}]
		},{
			xtype		: 'panel',
			region		: 'center',
			layout		:'table',
			layoutConfig: {
				columns		: 3
			},
			items		: [{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype		: 'label',
					text		: 'call'
				},{
					xtype		: 'textfield',
					value		: '310',
					id			: 'txtcalluri'
				},{
					xtype		: 'button',
					text		: 'call',
					handler		: function(){
						extEB.publish('guiaction.callClicked', {
							uri	:'sip:'+Ext.getCmp('txtcalluri').getValue()+'@10.33.100.221'
						});
					}
				}]
			},{
				width		: 200,
				xtype		: 'panel',
				frame		: true,
				items		: [{
					xtype		: 'label',
					text		: 'hangup'
				},{
					xtype		: 'textfield',
					value		: '',
					id			: 'txtsipcallid'
				},{
					xtype		: 'button',
					text		: 'hangup',
					handler		: function(){
						extEB.publish('guiaction.hangupClicked', {
							sipcallid	: Ext.getCmp('txtsipcallid').getValue()
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
});