package org.micoli.phone.ccphone;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;

import org.micoli.commands.Command;
import org.micoli.commands.CommandManager;
import org.micoli.phone.ccphone.call.Call;
import org.micoli.phone.ccphone.remote.VertX;
import org.micoli.phone.tools.ProxyLogger;
import org.vertx.java.core.json.JsonObject;

public class AsyncCommandManager {
	AsyncEventManager asyncEventManager;
	ProxyLogger logger;
	public AsyncCommandManager(AsyncEventManager asyncEventManager, ProxyLogger logger){
		this.asyncEventManager = asyncEventManager;
		this.logger = logger;
		CommandManager.scan(this,logger);
	}
	/**
	 * Call action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] call(@Command("uri") String uri) {
		uri = RFC3261.SIP_SCHEME + RFC3261.SCHEME_SEPARATOR + uri + RFC3261.AT + this.asyncEventManager.main.config.getDomain();
		String callId = Utils.generateCallID(this.asyncEventManager.getUserAgent().getConfig().getLocalInetAddress());
		Call call = new Call(uri, callId, logger);
		SipRequest sipRequest;
		try {
			sipRequest = this.asyncEventManager.getUserAgent().getUac().invite(uri, callId);
			this.asyncEventManager.getCalls().put(callId, call);
			call.setSipRequest(sipRequest);
			call.callAction();
		} catch (SipUriSyntaxException e) {
			logger.error(e.getMessage(), e);
			VertX.publishGui(new JsonObject().putString("setSipRequestError", e.getMessage()));
			return new String[] { e.getMessage() };
		}
		return new String[] { "ok" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] mute() {
		// userAgent.getSoundManager().mute(true);
		return new String[] { "ok" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] unmute() {
		// userAgent.getSoundManager().mute(false);
		return new String[] { "ok" };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] listCalls() {
		String[] result = new String[this.asyncEventManager.getCalls().size()];
		int n = 0;
		JsonObject jsonList = new JsonObject();

		Iterator<Entry<String, Call>> it = this.asyncEventManager.getCalls().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Call> pair = (Map.Entry<String,Call>)it.next();
			Call call = pair.getValue();
			jsonList.putObject(pair.getKey(), new JsonObject().putString("callid", call.getCallid()).putString("state", call.getCallState()));
			result[n] = String.format("%s (%s)", call.getCallid(), call.getCallState());
			n++;
		}
		VertX.publishGui(new JsonObject().putObject("list", jsonList));
		return result;
	}

	/**
	 * Hangup action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] hangup(@Command("sipcallid") String sipCallId) {
		SipRequest sipRequest = this.asyncEventManager.getSipRequestFromCallId(sipCallId);
		this.asyncEventManager.getUserAgent().getUac().terminate(sipRequest);
		this.asyncEventManager.hangup(sipRequest);
		return new String[] { "ok" };
	}

	/**
	 * Pickup action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] pickup(@Command("sipcallid") String sipCallId) {
		//SipRequest sipRequest = new SipRequest(null, null);
		//sipRequest.getSipHeaders().add(new SipHeaderFieldName(RFC3261.HDR_CALLID),new SipHeaderFieldValue(msgCallId));
		SipRequest sipRequest = this.asyncEventManager.getSipRequestFromCallId(sipCallId);
		String callId = Utils.getMessageCallId(sipRequest);
		DialogManager dialogManager = this.asyncEventManager.getUserAgent().getDialogManager();
		Dialog dialog = dialogManager.getDialog(callId);
		this.asyncEventManager.getUserAgent().getUas().acceptCall(sipRequest, dialog);
		return new String[] { "ok" };
	}

	/**
	 * Busy here action.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] busyHere(@Command("sipcallid") String sipCallId) {
		SipRequest sipRequest = this.asyncEventManager.getSipRequestFromCallId(sipCallId);
		this.asyncEventManager.getUserAgent().getUas().rejectCall(sipRequest);
		this.asyncEventManager.busyHere(sipRequest);
		return new String[] { "ok" };
	}

	/**
	 * Dtmf.
	 *
	 * @param message the message
	 */
	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public String[] dtmf(@Command("dtmfdigit") String dtmfDigit) {
		MediaManager mediaManager = this.asyncEventManager.getUserAgent().getMediaManager();
		mediaManager.sendDtmf(dtmfDigit.charAt(0));
		return new String[] { "ok" };
	}


	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] ping1(@Command("p1") String arg1) {
		return new String[] { "Ping3 shell test " + arg1.toString() };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] ping2(@Command("p1") String arg1, @Command("p2") String arg2) {
		return new String[] { "Ping3 shell test " + arg1.toString() + " " + arg2.toString() };
	}

	@Command(type = { Command.Type.GUI, Command.Type.SHELL })
	public synchronized String[] ping3(@Command("p1") String arg1, @Command("p2") String arg2, @Command("p3") String arg3) {
		return new String[] { "Ping3 shell test " + arg1.toString() + " " + arg2.toString() + " " + arg3.toString() };
	}

}
