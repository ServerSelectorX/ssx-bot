package xyz.derkades.ssx_bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class Ticket {
	
	private Server server;
	private String channelName;
	
	Ticket(Server server, int id){
		this.channelName = "ticket-" + id;
		this.server = server;
	}
	
	Ticket(Server server, String channelName){
		this.channelName = channelName;
		this.server = server;
	}
	
	ServerTextChannel getChannel() {
		return (ServerTextChannel) server.getChannelsByName(channelName).get(0);
	}
	
	String getChannelName() {
		return channelName;
	}
	
	void create(ServerTextChannel originalTextChannel, User creator) throws InterruptedException, ExecutionException {
		
		
		ServerTextChannel channel = new ServerTextChannelBuilder(server)
			.setName(channelName)
			.setCategory(server.getChannelCategoriesByName("Tickets").get(0))
			.addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setState(PermissionType.READ_MESSAGES, PermissionState.DENIED).build())
			.addPermissionOverwrite(creator, new PermissionsBuilder().setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED).build())
			.addPermissionOverwrite(server.getRolesByName("Support").get(0), new PermissionsBuilder().setState(PermissionType.READ_MESSAGES, PermissionState.ALLOWED).build())
			.create().get();
		
		channel.sendMessage(server.getRolesByName("Support").get(0).getMentionTag());
		channel.sendMessage("Your ticket has been created. Please describe your issue as clearly as possible, then we'll get back to you as soon as possible. Attach any console errors or config files if relevant. To close the ticket, type !close");
		originalTextChannel.sendMessage("A ticket has been created, see the channel " + channel.getMentionTag());
	}
	
	void delete() {
		getChannel().sendMessage(new EmbedBuilder()
				.addField("Ticket closed", "This ticket has been closed. The channel will be deleted automatically in 12 hours. Please don't continue talking in this channel. If you need more help, create a new ticket using `!ticket`.")
				.addField("Review", "If you appreciate the support, please leave a positive review on the spigot page for the free version (https://goo.gl/HZQ2kX) or premium version (https://goo.gl/RW2W5M). Thanks!")
				);

		new Timer().schedule(new TimerTask() {
			public void run() {
				getChannel().delete("Ticket has been deleted");
			}
		}, 12*60*60*1000);
	}
	
	static List<Ticket> getTickets(Server server) {
		ChannelCategory category = server.getChannelCategoriesByName("Tickets").get(0);
		
		List<Ticket> tickets = new ArrayList<>();
		
		for (ServerChannel channel : category.getChannels()) {
			tickets.add(new Ticket(server, channel.getName()));
		}
		
		return tickets;
	}
	
	static boolean channelExists(Server server, String channelName) {
		for (Ticket ticket : getTickets(server)) {
			if (ticket.getChannelName().equals(channelName)) return true;
		}
		return false;
	}
	
	static int getAvailableId(Server server) {
		while (true) {
			int id = (int) (Math.random() * 9999);
			if (!channelExists(server, "ticket-" + id)) {
				return id;
			}
		}
		
	}

}
