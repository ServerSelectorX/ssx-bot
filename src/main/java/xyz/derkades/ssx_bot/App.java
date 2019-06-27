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
			.addField("Support", "`!faq <question>` to send question help or `!faq` for a list of questions\n`!items`\n`!error`")
			.setColor(Color.GREEN);

    public static void main(final String[] args) {
    	System.out.println("Starting..");
    	final String token = args[0];

        final DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        final Server server = api.getServerById(338607425097695235L).get();

        api.updateActivity(ActivityType.WATCHING, "for you to type !help");

        api.addServerMemberJoinListener(event -> {
        	final ServerTextChannel general = (ServerTextChannel) server.getChannelById(338607425097695235L).get();
        	final ServerTextChannel welcome = (ServerTextChannel) server.getChannelById(341568814078754816L).get();
        	final ServerTextChannel information = (ServerTextChannel) server.getChannelById(472459616886849536L).get();
        	general.sendMessage(String.format(
        			"Welcome to the server, %s! Please read %s and %s. If you need help, create a ticket by typing `!ticket`.",
        			event.getUser().getMentionTag(), welcome.getMentionTag(), information.getMentionTag()));
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
            	final String message = "You must use item names that are listed in the links below:\n" +
            			"New version: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            			"Legacy version: http://docs.codelanx.com/Bukkit/1.8/org/bukkit/Material.html\n\n" +
            			"New version = Free-3.4.0+ or Premium-2.0.0 (non legacy)\n" +
            			"Legacy version = Free-3.1.1, Premium-1.4.4 or Premium-2.0.0-legacy";
            	event.getChannel().sendMessage(message);
            	event.getMessage().delete();
            }

            if (event.getMessageContent().equalsIgnoreCase("!error")) {
            	final String message = "It looks like you need help with an error. Please describe exactly what happens and what you expect to happen. Send your /logs/latest.log file and any relevant menu files (usually just default.yml). Please use https://hastebin.com or attach the files directly in discord.";

            	event.getChannel().sendMessage(message);
            	event.getMessage().delete();
            }
        });

        System.out.println("SSX Bot Started.");
    }

}
