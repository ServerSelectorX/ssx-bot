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

	private final Server server;
	private final String channelName;

	Ticket(final Server server, final int id){
		this.channelName = "ticket-" + id;
		this.server = server;
	}

	Ticket(final Server server, final String channelName){
		this.channelName = channelName;
		this.server = server;
	}

	ServerTextChannel getChannel() {
		return (ServerTextChannel) this.server.getChannelsByName(this.channelName).get(0);
	}

	String getChannelName() {
		return this.channelName;
	}

	void create(final ServerTextChannel originalTextChannel, final User creator) throws InterruptedException, ExecutionException {
		final ServerTextChannel channel = new ServerTextChannelBuilder(this.server)
			.setName(this.channelName)
			.setCategory(this.server.getChannelCategoriesByName("Tickets").get(0))
			.addPermissionOverwrite(this.server.getEveryoneRole(), new PermissionsBuilder().setState(PermissionType.VIEW_CHANNEL, PermissionState.DENIED).build())
			.addPermissionOverwrite(creator, new PermissionsBuilder().setState(PermissionType.VIEW_CHANNEL, PermissionState.ALLOWED).build())
			.addPermissionOverwrite(this.server.getRolesByName("Support").get(0), new PermissionsBuilder().setState(PermissionType.VIEW_CHANNEL, PermissionState.ALLOWED).build())
			.create().get();

//		channel.sendMessage(this.server.getRolesByName("Support").get(0).getMentionTag());
		final EmbedBuilder createdMessage = new EmbedBuilder()
				.addField("Ticket created", "A ticket has been created.")
				.addField("What do you need help with?", "Describe your issue as clearly as possible. Include your "
						+ "Minecraft version and ServerSelectorX version. If an issue occured after modifying configuration "
						+ "files, send them by dragging them to this channel or uploading them to https://paste.rkslot.nl");

//		if (!creator.getRoles(this.server).stream().map(Role::getId).anyMatch(Predicate.isEqual(352135799543693312L))) {
//			createdMessage.addField("Premium", "If you need support for the premium version, please verify your account using `!verify`");
//		}

		channel.sendMessage(createdMessage);

		final int openTickets = getTickets(this.server).size();

		originalTextChannel.sendMessage(new EmbedBuilder()
				.addField("Ticket created", "A ticket has been created (" + channel.getName().substring(7) + ")")
				.setFooter("There are currently " + openTickets + " open tickets.")
				);
	}

	void delete() {
		this.getChannel().sendMessage(new EmbedBuilder().addField("Ticket closed",
				"This ticket has been closed. The channel will be deleted in 1 hour."));

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				Ticket.this.getChannel().delete("Ticket has been deleted");
			}
		}, 60*60*1000);
	}

	static List<Ticket> getTickets(final Server server) {
		final ChannelCategory category = server.getChannelCategoriesByName("Tickets").get(0);

		final List<Ticket> tickets = new ArrayList<>();

		for (final ServerChannel channel : category.getChannels()) {
			tickets.add(new Ticket(server, channel.getName()));
		}

		return tickets;
	}

	static boolean channelExists(final Server server, final String channelName) {
		for (final Ticket ticket : getTickets(server)) {
			if (ticket.getChannelName().equals(channelName)) {
				return true;
			}
		}
		return false;
	}

	static int getAvailableId(final Server server) {
		while (true) {
			final int id = (int) (Math.random() * 9999);
			if (!channelExists(server, "ticket-" + id)) {
				return id;
			}
		}

	}

}
