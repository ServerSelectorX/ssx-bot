package xyz.derkades.ssx_bot;

import java.awt.Color;

import org.javacord.api.entity.message.embed.EmbedBuilder;

public enum Question {

	ACTIONS("actions", "See this wiki page for a list of actions: <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Actions>"),
	WIKI("wiki", "<https://github.com/ServerSelectorX/ServerSelectorX/wiki>"),
	ISSUES("issues", "<https://github.com/ServerSelectorX/ServerSelectorX/issues>"),
//	VERIFY("verify", "It looks like you are asking for support for the premium version of ServerSelectorX. To receive further support, please verify that you have purchased the plugin. To do this, send a message on spigot with your discord username by clicking the following link: <https://www.spigotmc.org/conversations/add?to=RobinMC>. After doing this we will give you a \"Premium\" role and not bother you again."),

	FREE_PING("ping", "The free version of ServerSelectorX uses the \"Server Pinging\" system to get server information. See this wiki page: <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Free-%7C-Server-Pinging>"),
	FREE_PINGING("pinging", "The free version of ServerSelectorX uses the \"Server Pinging\" system to get server information. See this wiki page: <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Free-%7C-Server-Pinging>"),
	FREE_DYNAMIC("free dynamic", "See this wiki page: <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Free-%7C-Dynamic-items>"),

	PREMIUM_DYNAMIC("premium dynamic", "See this wiki page: <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Premium-%7C-Dynamic>"),
	PREMIUM_INSTALL("premium install", "You can find installation instructions for the premium version of ServerSelectorX here: <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Premium-%7C-Installation-Instructions>"),
	PREMIUM_INSTALL_CONNECTOR("connector install", "For SSX-Connector installation instructions, see <https://github.com/ServerSelectorX/ServerSelectorX/wiki/Premium-%7C-Installing-SSX-Connector>"),
	;

	String question;
	String answer;

	Question(final String question, final String answer){
		this.question = question;
		this.answer = answer;
	}

	static EmbedBuilder getHelpEmbed() {
		String helpMessage = "";
    	for (final Question question : Question.values()) {
    		helpMessage += String.format("`!faq %s`\n", question.question, question.answer);
    	}

    	return new EmbedBuilder().setTitle("FAQ Help").setColor(Color.ORANGE).addField("Commands", helpMessage);
	}

}
