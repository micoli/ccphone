Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL='http://www.sencha.com/s.gif';
	var supported = ("WebSocket" in window);
	if(!supported) {
		var msg = "Your browser does not support Web Sockets. This example will not work properly.<br>";
		msg += "Please use a Web Browser with Web Sockets support (WebKit or Google Chrome).";
		$("#connect").html(msg);
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
				disabled	: false,
				handler		: function(){

				}
			},{
				xtype		: 'button',
				text		: 'disconnect',
				disabled	: true,
				handler		: function(){

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
						p.tpl.overwrite(p.body,grid.getStore().data.items[rowIndex].data);
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
						'<pre>{text}</pre>',
					'</div></tpl>'
				)
			}]
		},{
			xtype	: 'panel',
			id		: 'extensionpanel',
			region	: 'center',
			items	: new Ext.DataView({
				store		: extenStore,
				autoHeight	: true,
				border		: true,
				autoHeight	: true,
				multiSelect	: true,
				overClass	: 'x-view-over',
				itemSelector: 'div.thumb-wrap',
				emptyText	: 'No images to display',
				tpl			: new Ext.XTemplate(
					'<tpl for=".">',
						'<div class="thumb-wrap" style="width:160px;height:40px;float:left;border:1px solid black;font: 12px arial;" >',
						'	<h2>{exten}</h2>',
							'<span class="x-editable">{str}</span>',
							'<span class="x-editable">{time}</span>',
						'</div>',
					'</tpl>',
					'<div class="x-clear"></div>'
				)
			})
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
	var localCacheExtenTimer={};
	Ext.Ajax.request({
		url: 'localcache.txt',
		success: function(result){
			eval('var res = '+result.responseText+';');
			extenStore.removeAll();
			for (var k in res.users){
				localCacheExtenTimer[res.users[k].exten]=null;
				extenStore.add([new extenStore.recordType({
					exten		: res.users[k].exten,
					data		: {},
					str			: '-'
				})]);
			}
			extenStore.commitChanges();
			es = new Eu.sm.Stomp({
				url				: 'ws://10.33.1.221:61614/stomp',
				login			: 'guest',
				password		: 'password',
				debug			: addToList,
				subscriptions	: {
					'/topic/extensions.*' : {
						1 : {
							scope : this,
							callback : function(msg,topic){
								if(topic=='/topic/extensions.debug'){
									return;
								}
								addToList(msg.body,topic);
								eval('var dt = '+msg.body);
								console.log(dt,topic,msg);
								var record = updateExtenTpl(dt);
								record.beginEdit();
								record.set('str',dt.event);
								if(localCacheExtenTimer[dt.subject]){
									clearTimeout(localCacheExtenTimer[dt.subject]);
									localCacheExtenTimer[dt.subject]=null
									record.set('time','');
								}
								record.endEdit();
								record.commit(false);
								if(dt.timer){
									(function() {
										localCacheExtenTimer[dt.subject] = setInterval(function(){
											record.beginEdit();
											record.set('time',Ext.util.Format.dateRenderer('m-d H:i:s')(new Date()));
											record.endEdit();
											record.commit(false);
										},500);
									})()
								}else{
									(function() {
										setTimeout(function(){
											record.beginEdit();
											record.set('str','');
											record.endEdit();
											record.commit(false);
										},1500);
									})()
								}
							}
						}
					}
				}
			}).connect();
		}
	});
});