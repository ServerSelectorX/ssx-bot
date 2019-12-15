package xyz.derkades.ssx_bot;

import java.awt.Color;
import java.util.concurrent.ExecutionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

public class App {

	private static final EmbedBuilder EMBED_HELP = new EmbedBuilder()
			.setTitle("Command help")
			.addField("Tickets", "`!ticket` - Create a new ticket.\n`!close` - Close a ticket.")
			.addField("Support", "`!faq <question>` to send question help or `!faq` for a list of questions\n`!items`\n`!error`.")
			.addField("Premium", "`!verify` for premium verification instructions.")
			.setColor(Color.GREEN);

    public static void main(final String[] args) {
    	System.out.println("Starting..");
    	final String token = args[0];

        final DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        final Server server = api.getServerById(338607425097695235L).get();

        api.updateActivity(ActivityType.WATCHING, "for you to type !help");

        api.addServerMemberJoinListener(event -> {
        	final ServerTextChannel general = (ServerTextChannel) server.getChannelById(338607425097695235L).get();
        	final ServerTextChannel welcome = (ServerTextChannel) server.getChannelById(536534866703941652L).get();
        	final ServerTextChannel information = (ServerTextChannel) server.getChannelById(472459616886849536L).get();
        	general.sendMessage(new EmbedBuilder()
        			.addField("Welcome", String.format(
        					"Welcome to the server, %s! Please read %s and %s. If you need help ask here or create a ticket "
        					+ "by typing `!ticket`.",
        					event.getUser().getMentionTag(), welcome.getMentionTag(), information.getMentionTag()))
        			.setFooter("Do not DM staff for support."));
        });

        api.addMessageCreateListener(event -> {
        	if (event.getMessageContent().equalsIgnoreCase("!ticket")) {
        		try {
					new Ticket(server, Ticket.getAvailableId(server)).create(event.getServerTextChannel().get(),
							event.getMessageAuthor().asUser().get());
				} catch (InterruptedException | ExecutionException e) {
					event.getChannel().sendMessage("An error occured while trying to create a ticket.");
					e.printStackTrace();
				}
        	}

        	if (event.getMessageContent().equalsIgnoreCase("!close")) {
        		boolean inTicketChannel = false;

        		for (final Ticket ticket : Ticket.getTickets(server)) {
        			if (ticket.getChannelName().equals(event.getServerTextChannel().get().getName())){
        				inTicketChannel = true;
        			}
        		}

        		if (inTicketChannel) {
        			final Ticket ticket = new Ticket(server, event.getServerTextChannel().get().getName());
        			ticket.delete();
        		} else {
        			event.getChannel().sendMessage("This command can only be used in a ticket channel.");
        		}
        	}

            if (event.getMessageContent().equalsIgnoreCase("!help")) {
                event.getChannel().sendMessage(EMBED_HELP);
                event.getMessage().delete();
            }

            if (event.getMessageContent().startsWith("!faq ")) {
            	final String askedQuestion = event.getMessageContent().substring(5);
            	System.out.println(askedQuestion);
            	for (final Question question : Question.values()) {
            		if (question.question.equalsIgnoreCase(askedQuestion)) {
            			event.getChannel().sendMessage(question.answer);
            			event.getMessage().delete();
            			return;
            		}
            	}

            	event.getChannel().sendMessage(Question.getHelpEmbed());
            	event.getMessage().delete();
            }

            if (event.getMessageContent().equalsIgnoreCase("!faq")) {
            	event.getChannel().sendMessage(Question.getHelpEmbed());
            	event.getMessage().delete();
            }

            if (event.getMessageContent().equalsIgnoreCase("!items")) {
            	final String message = "Item names list: https://github.com/ServerSelectorX/ServerSelectorX/wiki/Item-names";
            	event.getChannel().sendMessage(message);
            	event.getMessage().delete();
            }

            if (event.getMessageContent().equalsIgnoreCase("!error")) {
            	final String message = "Please describe exactly what happens and what you expect to happen. Send your /logs/latest.log file and any relevant menu files (usually just default.yml). Please use https://hastebin.com or attach the files directly in discord.";

            	event.getChannel().sendMessage(message);
            	event.getMessage().delete();
            }

            if (event.getMessageContent().equalsIgnoreCase("!verify")) {
            	event.getChannel().sendMessage(new EmbedBuilder()
            			.addField("Premium verification", "To get a premium role, send a message on spigot with your"
            					+ " discord username by clicking the following link:"
            					+ " https://www.spigotmc.org/conversations/add?to=RobinMC&title=Premium%20verification.")
            			);
            	event.getMessage().delete();
            }
        });

        System.out.println("SSX Bot Started.");
    }

}
