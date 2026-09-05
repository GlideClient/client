package me.eldodebug.soar.discord;

import java.time.OffsetDateTime;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.GlideMeta;
import me.eldodebug.soar.discord.ipc.IPCClient;
import me.eldodebug.soar.discord.ipc.IPCListener;
import me.eldodebug.soar.discord.ipc.entities.RichPresence;
import me.eldodebug.soar.discord.ipc.exceptions.NoDiscordClientException;

public class DiscordRPC {

	private IPCClient client;
	
	public void start() {
		
		client = new IPCClient(1465100974950449323L);
		client.setListener(new IPCListener() {
			@Override
			public void onReady(IPCClient client) {
				
				RichPresence.Builder builder = new RichPresence.Builder();
				
				builder.setState(String.format("Playing %s v %s", GlideMeta.CLIENT_NAME, GlideMeta.VERSION_NUMBER))
						.setStartTimestamp(OffsetDateTime.now())
						.setLargeImage("icon");
				
				client.sendRichPresence(builder.build());
			}
		});
		
		try {
			client.connect();
		} catch (NoDiscordClientException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		client.close();
	}

	public IPCClient getClient() {
		return client;
	}
	
	public boolean isStarted() {
		return client != null;
	}
}
