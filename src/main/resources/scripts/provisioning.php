<?php
$output=<<<EOF
<?xml version="1.0" encoding="UTF-8" standalone="no"?><!--
		This file is part of Peers.

		This program is free software: you can redistribute it and/or modify
		it under the terms of the GNU General Public License as published by
		the Free Software Foundation, either version 3 of the License, or
		any later version.

		This program is distributed in the hope that it will be useful,
		but WITHOUT ANY WARRANTY; without even the implied warranty of
		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
		GNU General Public License for more details.

		You should have received a copy of the GNU General Public License
		along with this program.  If not, see <http://www.gnu.org/licenses/>.

		Copyright 2007, 2008, 2009, 2010, 2012 Yohann Martineau
-->
<peers xmlns="http://peers.sourceforge.net" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://peers.sourceforge.net peers.xsd">
	<!-- a specific address can be specified here, may you have several
			network interfaces, or several addresses on a specific interface,
			you can specify an address to bind on here. -->
	<!-- Example: 192.168.1.20 -->
	<ipAddress/>
	<!-- username (corresponding to the user part of your sip uri) -->
	<!-- Example: alice -->
	<userPart>6003</userPart>
	<!-- domain (corresponding to the domain part of your sip uri) -->
	<!-- Example: atlanta.com -->
	<domain>192.168.1.72</domain>
	<!-- if password is empty, no REGISTER message is sent -->
	<!-- Example: 1234 -->
	<password>ab1234</password>
	<!-- you can specify an outbound proxy for registration and calls -->
	<!-- Example: sip:192.168.1.20;lr -->
	<outboundProxy/>
	<!-- you can specify the sip listening port you want, 0 can be used to
			choose a random free port -->
	<!-- Example: 5060 -->
	<sipPort>0</sipPort>
			<!-- you can specify the rtp port to use for incoming and outgoing rtp
					traffic, 0 can be used to choose a random free port -->
	<rtpPort>0</rtpPort>
	<!-- mediaMode corresponds to the way media is managed. Three values are
			possible for this parameter:
					- captureAndPlayback: capture sound from microphone, send
					corresponding rtp packets, receive rtp packets and play those
					packets on speakers.
					- echo: receive rtp packets, do not play them on speakers and send
					those packets to remote party
					- none: no media is capture, played, send nor received
					- file: stream audio from audio file provided in mediaFile -->
	<mediaMode>captureAndPlayback</mediaMode>
	<!-- mediaDebug is a boolean parameter. If set to true, files will be
			created in a media directory in peers.home directory. Those files will
			contain raw data at input and output of microphone, encoder, rtp
			sender, rtp receiver, and speaker. -->
	<mediaDebug>false</mediaDebug>
	<!-- mediaFile file read and sent during call. This file must be a raw audio
			file with the following format: linear PCM 8kHz, 16 bits signed,
			mono-channel, little endian.  -->
	<!-- Example: media/message.raw -->
	<mediaFile/>
</peers>
EOF;
print $output;