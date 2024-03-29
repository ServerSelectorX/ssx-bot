package xyz.derkades.ssx_bot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;

import java.awt.Color;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class App {

	private static final EmbedBuilder EMBED_HELP = new EmbedBuilder().setTitle("Command help")
			.addField("Tickets", "`!ticket` - Create a new ticket.")
			.addField("Support", "`!items`, `!error`, `!actions`, `!wiki`, `!connector`, `!heads`.")
			.addField("Premium", "`!verify` for premium verification instructions.").setColor(Color.GREEN);

	public static void main(final String[] args) {
    	final String token = System.getenv("SSXBOT_TOKEN");
    	if (token == null) {
    		System.err.println("No token provided");
    		System.exit(1);
    	}

    	System.out.println("Token: " + token);

        final DiscordApi api = new DiscordApiBuilder()
				.setToken(token)
				.addIntents(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS)
				.setUserCacheEnabled(true)
				.login()
				.join();
        final Server server = api.getServerById(338607425097695235L).get();

//        api.updateActivity(ActivityType.WATCHING, "for you to type !help");
        api.updateActivity(ActivityType.LISTENING, "hard drives clicking");

//        api.addServerMemberJoinListener(event -> {
//        	final ServerTextChannel general = (ServerTextChannel) server.getChannelById(338607425097695235L).get();
//        	final ServerTextChannel welcome = (ServerTextChannel) server.getChannelById(536534866703941652L).get();
//        	general.sendMessage(new EmbedBuilder()
//        			.addField("Welcome", String.format(
//        					"Welcome to the server, %s! Please read %s.",
//        					event.getUser().getMentionTag(), welcome.getMentionTag()))
//        			.setFooter("Please do not DM staff."));
//        });

        api.addMessageCreateListener(event -> {
        	final String message = event.getMessageContent();

        	if (message.length() < 1 || // Empty messages (e.g. attachments)
        			message.charAt(0) != '!') {
        		return;
        	}

        	switch(message) {

        	case "!ticket":
	        	if (!event.getMessageAuthor().asUser().orElseThrow(() -> new IllegalStateException("Message author is not a user?"))
	        			.getRoles(server).stream().map(Role::getId).anyMatch(Predicate.isEqual(352135799543693312L))) {
	    			event.getChannel().sendMessage("This command can only be used by users with a \"Premium\" role. If you have purchased the premium version, please run !verify to get a premium role. Otherwise, feel free to ask your question in this channel.");
	    			return;
	    		}

        		try {
					new Ticket(server, Ticket.getAvailableId(server)).create(event.getServerTextChannel().get(),
							event.getMessageAuthor().asUser().get());
				} catch (InterruptedException | ExecutionException e) {
					event.getChannel().sendMessage("An error occured while trying to create a ticket.");
					e.printStackTrace();
				}
        		break;

        	case "!close":
        		if (event.getMessageAuthor().getId() != 183954832485253121L) {
        			return;
        		}

				boolean inTicketChannel = false;

				for (final Ticket ticket : Ticket.getTickets(server)) {
					if (ticket.getChannelName().equals(event.getServerTextChannel().get().getName())) {
						inTicketChannel = true;
					}
				}

				if (inTicketChannel) {
					final Ticket ticket = new Ticket(server, event.getServerTextChannel().get().getName());
					ticket.delete();
				} else {
					event.getChannel().sendMessage("This command can only be used in a ticket channel.");
				}

				break;

        	case "!error":
            	event.getChannel().sendMessage("Please describe exactly what happens and what you expect to happen. "
            			+ "Send your /logs/latest.log file and any relevant menu files (usually just default.yml). "
        				+ "Please use https://paste.rkslot.nl or attach the files directly in Discord, avoid pastebin "
        				+ "since it has annoying captchas.");
            	break;

			case "!items":
				event.getChannel().sendMessage(
						"Item names list: https://github.com/ServerSelectorX/ServerSelectorX/wiki/Item-names");
				break;

			case "!verify":
				event.getChannel().sendMessage(new EmbedBuilder().addField("Premium verification",
						"To get a premium role, please send a message on spigot with your"
								+ " Discord username by clicking the following link:"
								+ " https://www.spigotmc.org/conversations/add?to=RobinMC&title=Premium%20verification (so enter YourName#0000 in the message body)."
								+ "\n"
								+ "Spigot only allows sending DMs if you have made at least 5 forum posts using your account. If you get a 'Permission denied' error, please send your Spigot username in this channel and we will message you instead."));
				break;

			case "!actions":
				event.getChannel()
						.sendMessage(new EmbedBuilder().setTitle("Actions list")
								.addField("Free version",
										"https://github.com/ServerSelectorX/ServerSelectorX/wiki/Actions")
								.addField("Premium version",
										"https://github.com/ServerSelectorX/ServerSelectorX/wiki/Actions-v2"));
				break;

			case "!wiki":
				event.getChannel().sendMessage("https://github.com/ServerSelectorX/ServerSelectorX/wiki");
				break;

			case "!heads":
				event.getChannel().sendMessage("https://github.com/ServerSelectorX/ServerSelectorX/wiki/Player-heads");
				break;

			case "!connector":
				event.getChannel().sendMessage(
						"https://github.com/ServerSelectorX/ServerSelectorX/wiki/Installing-SSX-Connector");
				break;

			default:
				event.getChannel().sendMessage(EMBED_HELP);
			}
		});

        System.out.println("SSX Bot Started.");
    }

}
